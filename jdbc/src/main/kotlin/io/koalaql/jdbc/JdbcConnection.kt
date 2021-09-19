package io.koalaql.jdbc

import io.koalaql.KotqConnection
import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.createTables
import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.query.*
import io.koalaql.query.built.BuiltReturningInsert
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.sql.SqlText
import io.koalaql.values.RowSequence
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class JdbcConnection(
    val jdbc: Connection,
    private val dialect: SqlDialect,
    private val typeMappings: JdbcTypeMappings,
    private val events: ConnectionEventWriter
): KotqConnection {
    fun createTable(vararg tables: Table) {
        ddl(createTables(*tables))

        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }
    }

    private fun prepare(sql: SqlText, generatedKeys: Boolean): PreparedStatement {
        val result = if (generatedKeys) {
            jdbc.prepareStatement(sql.sql, Statement.RETURN_GENERATED_KEYS)
        } else {
            jdbc.prepareStatement(sql.sql)
        }

        sql.parameters.forEachIndexed { ix, literal ->
            @Suppress("unchecked_cast")
            val mapping = typeMappings.mappingFor(literal.type) as JdbcMappedType<Any>

            literal.value
                ?.let { mapping.writeJdbc(result, ix + 1, it) }
                ?: result.setObject(ix + 1, null)
        }

        return result
    }

    private inline fun <R> prepareAndThen(
        sql: SqlText,
        generatedKeys: Boolean = false,
        block: PreparedStatement.() -> R
    ): R {
        return try {
            prepare(sql, generatedKeys).block()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    private fun prepareAndUpdate(
        type: ConnectionQueryType,
        sql: SqlText
    ): Int = prepareAndThen(sql) {
        use {
            val event = events.perform(type, sql)

            val rows = try {
                executeUpdate()
            } catch (ex: Exception) {
                event.failed(ex)
                throw ex
            }

            event.succeeded(rows)

            rows
        }
    }

    fun ddl(diff: SchemaDiff) {
        dialect.ddl(diff).forEach {
            prepareAndThen(it) {
                use {
                    execute()
                }
            }
        }
    }

    private fun execute(insert: Inserted): Int {
        val built = insert.buildInsert()

        val sql = dialect.compile(built)

        return prepareAndUpdate(ConnectionQueryType.INSERT, sql)
    }

    private fun execute(updated: Updated): Int {
        val built = updated.buildUpdate()

        val sql = dialect.compile(built)

        return prepareAndUpdate(ConnectionQueryType.UPDATE, sql)
    }

    private fun query(queryable: Queryable): RowSequence {
        return when (val built = queryable.buildQuery()) {
            is BuiltReturningInsert -> {
                fun err(): Nothing = error("RETURNING must expose a single auto-generated key")

                val sql = dialect.compile(built.insert)

                val keys = built.returning
                if (keys.size != 1) err()

                val column = keys[0]
                if (column !is TableColumn) err()

                if (!column.builtDef.autoIncrement) err()

                prepareAndThen(sql, true) {
                    val event = events.perform(ConnectionQueryType.INSERT, sql)

                    val rows = try {
                        executeUpdate()
                    } catch (ex: Exception) {
                        event.failed(ex)
                        throw ex
                    }

                    event.succeeded(rows)

                    ResultSetRowSequence(
                        LabelListOf(built.returning),
                        event,
                        typeMappings,
                        generatedKeys
                    )
                }
            }
            is BuiltSubquery -> {
                val sql = dialect.compile(built)

                prepareAndThen(sql) {
                    val event = events.perform(ConnectionQueryType.QUERY, sql)

                    val results = try {
                        executeQuery()
                    } catch (ex: Exception) {
                        event.failed(ex)
                        throw ex
                    }

                    event.succeeded(null)

                    ResultSetRowSequence(
                        built.columns,
                        event,
                        typeMappings,
                        results
                    )
                }
            }
        }
    }

    private fun execute(deleted: Deleted): Int {
        val built = deleted.buildDelete()

        val sql = dialect.compile(built)

        return prepareAndUpdate(ConnectionQueryType.DELETE, sql)
    }

    override fun query(query: PerformableQuery): RowSequence = when (query) {
        is Queryable -> query(query)
    }

    override fun statement(statement: PerformableStatement): Int = when (statement) {
        is Inserted -> execute(statement)
        is Updated -> execute(statement)
        is Deleted -> execute(statement)
    }

    override fun commit() {
        try {
            jdbc.commit()
        } catch (ex: Exception) {
            events.committed(ex)
            throw ex
        }

        events.committed(null)
    }

    override fun rollback() {
        try {
            jdbc.rollback()
        } catch (ex: Exception) {
            events.rollbacked(ex)
            throw ex
        }

        events.rollbacked(null)
    }

    override fun close() {
        events.closed()
        jdbc.close()
    }
}