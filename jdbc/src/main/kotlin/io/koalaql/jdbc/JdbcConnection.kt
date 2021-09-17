package io.koalaql.jdbc

import io.koalaql.KotqConnection
import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.createTables
import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.dialect.SqlDialect
import io.koalaql.query.*
import io.koalaql.query.built.BuiltReturningInsert
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.sql.SqlText
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class JdbcConnection(
    val jdbc: Connection,
    private val dialect: SqlDialect,
    private val typeMappings: JdbcTypeMappings = JdbcTypeMappings()
): KotqConnection {
    fun createTable(vararg tables: Table) {
        ddl(createTables(*tables))

        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }
    }

    private fun prepare(sql: SqlText, generatedKeys: Boolean = false): PreparedStatement {
        val result = try {
            if (generatedKeys) {
                jdbc.prepareStatement(sql.sql, Statement.RETURN_GENERATED_KEYS)
            } else {
                jdbc.prepareStatement(sql.sql)
            }
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
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

    fun ddl(diff: SchemaDiff) {
        dialect.ddl(diff).forEach {
            try {
                prepare(it).execute()
            } catch (ex: Exception) {
                throw GeneratedSqlException(it, ex)
            }
        }
    }

    private fun execute(insert: Inserted): Int {
        val built = insert.buildInsert()

        val sql = dialect.compile(built)

        try {
            return prepare(sql).executeUpdate()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    private fun execute(updated: Updated): Int {
        val built = updated.buildUpdate()

        val sql = dialect.compile(built)

        try {
            return prepare(sql).executeUpdate()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    private fun query(queryable: Queryable): RowSequence {
        val built = queryable.buildQuery()

        when (built) {
            is BuiltReturningInsert -> {
                fun err(): Nothing = error("RETURNING must expose a single auto-generated key")

                val sql = dialect.compile(built.insert)

                val prepared = prepare(sql, true)

                val keys = built.returning
                if (keys.size != 1) err()

                val column = keys[0]
                if (column !is TableColumn) err()

                if (!column.builtDef.autoIncrement) err()

                try {
                    prepared.execute()
                } catch (ex: Exception) {
                    throw GeneratedSqlException(sql, ex)
                }

                return object : RowSequence {
                    override val columns: LabelList = LabelList(built.returning)

                    override fun rowIterator(): RowIterator {
                        return AdaptedResultSet(typeMappings, columns, prepared.generatedKeys)
                    }
                }
            }
            is BuiltSubquery -> {
                val sql = dialect.compile(built)

                val prepared = prepare(sql)

                val results = try {
                    prepared.executeQuery()
                } catch (ex: Exception) {
                    throw GeneratedSqlException(sql, ex)
                }

                return object : RowSequence {
                    override val columns: LabelList = built.columns

                    override fun rowIterator(): RowIterator {
                        return AdaptedResultSet(typeMappings, columns, results)
                    }
                }
            }
        }
    }

    private fun execute(deleted: Deleted): Int {
        val built = deleted.buildDelete()

        val sql = dialect.compile(built)

        try {
            return prepare(sql).executeUpdate()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
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
        jdbc.commit()
    }

    override fun rollback() {
        jdbc.rollback()
    }

    override fun close() {
        jdbc.close()
    }
}