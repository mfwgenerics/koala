package io.koalaql.jdbc

import io.koalaql.DataConnection
import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.TableColumn
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.expr.Column
import io.koalaql.query.LabelListOf
import io.koalaql.query.built.*
import io.koalaql.sql.SqlText
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence
import io.koalaql.values.emptyRowSequence
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class JdbcConnection(
    val jdbc: Connection,
    private val dialect: SqlDialect,
    private val typeMappings: JdbcTypeMappings,
    private val events: ConnectionEventWriter
): DataConnection {
    fun prepare(sql: SqlText, generatedKeys: Boolean): PreparedStatement {
        val result = if (generatedKeys) {
            jdbc.prepareStatement(sql.parameterizedSql, Statement.RETURN_GENERATED_KEYS)
        } else {
            jdbc.prepareStatement(sql.parameterizedSql)
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

    inline fun <R> prepareAndThen(
        sql: SqlText,
        generatedKeys: Boolean = false,
        block: PreparedStatement.() -> R
    ): R {
        return try {
            prepare(sql, generatedKeys).block()
        } catch (ex: Exception) {
            throw JdbcException(sql, ex)
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
                event.finished(Result.failure(ex))
                throw ex
            }

            event.finished(Result.success(rows))

            rows
        }
    }

    private fun execute(insert: BuiltInsert): Int {
        val sql = dialect.compile(insert) ?: return 0

        return prepareAndUpdate(ConnectionQueryType.INSERT, sql)
    }

    private fun execute(update: BuiltUpdate): Int {
        val sql = dialect.compile(update) ?: return 0

        return prepareAndUpdate(ConnectionQueryType.UPDATE, sql)
    }

    /*
    JDBC's getGeneratedKeys is poorly standardized.
    some drivers return all inserted columns for new rows by name
    others return a single auto-generated key but under a generic name e.g. "GENERATED_KEY"
    */
    private fun positionOf(column: Column<*>, rs: ResultSet): Int {
        val md = rs.metaData
        val generatedColumns = md.columnCount

        if (generatedColumns == 1) return 1

        repeat(md.columnCount) { ix ->
            if (md.getColumnName(ix + 1) == column.symbol) return ix + 1
        }

        error("unable to fetch generated key $column")
    }

    override fun query(query: BuiltQuery): RowSequence<RawResultRow> {
        return when (query) {
            is BuiltGeneratesKeysInsert -> {
                fun err(): Nothing = error("must select a single auto-generated key")

                val sql = dialect.compile(query.insert)
                    ?: return emptyRowSequence(listOf(query.returning))

                val column = query.returning
                if (column !is TableColumn) err()

                if (!column.builtDef.autoIncrement) err()

                prepareAndThen(sql, true) {
                    val event = events.perform(ConnectionQueryType.INSERT, sql)

                    val rows = try {
                        executeUpdate()
                    } catch (ex: Exception) {
                        event.finished(Result.failure(ex))
                        throw ex
                    }

                    event.finished(Result.success(rows))

                    val rs = generatedKeys

                    val ix = positionOf(query.returning, rs)

                    return ResultSetRowSequence(
                        LabelListOf(listOf(query.returning)),
                        offset = ix,
                        event,
                        typeMappings,
                        generatedKeys
                    )
                }
            }
            is BuiltFullQuery -> {
                val sql = dialect.compile(query)
                    ?: return emptyRowSequence(query.columns)

                prepareAndThen(sql) {
                    val event = events.perform(ConnectionQueryType.QUERY, sql)

                    val results = try {
                        executeQuery()
                    } catch (ex: Exception) {
                        event.finished(Result.failure(ex))
                        throw ex
                    }

                    event.finished(Result.success(null))

                    ResultSetRowSequence(
                        LabelListOf(query.columns),
                        offset = 1,
                        event,
                        typeMappings,
                        results
                    )
                }
            }
        }
    }

    private fun execute(built: BuiltDelete): Int {
        val sql = dialect.compile(built) ?: return 0

        return prepareAndUpdate(ConnectionQueryType.DELETE, sql)
    }

    override fun statement(statement: BuiltStatement): Int = when (statement) {
        is BuiltInsert -> execute(statement)
        is BuiltUpdate -> execute(statement)
        is BuiltDelete -> execute(statement)
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

    override fun generateSql(dml: BuiltDml): SqlText? = dialect.compile(dml)
}