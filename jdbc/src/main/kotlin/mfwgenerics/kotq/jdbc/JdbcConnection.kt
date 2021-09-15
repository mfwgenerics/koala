package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.KotqConnection
import mfwgenerics.kotq.data.JdbcMappedType
import mfwgenerics.kotq.data.JdbcTypeMappings
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.built.BuiltReturningInsert
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.RowSequence
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

    private fun execute(insert: Inserted) {
        val built = insert.buildInsert()

        val sql = dialect.compile(built)

        try {
            prepare(sql).execute()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    private fun execute(updated: Updated) {
        val built = updated.buildUpdate()

        val sql = dialect.compile(built)

        try {
            prepare(sql).execute()
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

    private fun execute(deleted: Deleted) {
        val built = deleted.buildDelete()

        val sql = dialect.compile(built)

        try {
            prepare(sql).execute()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    /* can't correctly type this without something like GADTs */
    @Suppress("unchecked_cast", "implicit_cast_to_any")
    override fun <T> perform(performable: Performable<T>): T = when (performable) {
        is Inserted -> execute(performable)
        is Queryable -> query(performable)
        is Updated -> execute(performable)
        is Deleted -> execute(performable)
    } as T

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