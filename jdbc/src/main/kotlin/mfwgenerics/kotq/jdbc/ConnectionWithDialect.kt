package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.MappedDataType
import mfwgenerics.kotq.data.TypeMappings
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Queryable
import mfwgenerics.kotq.query.Updated
import mfwgenerics.kotq.query.built.BuiltReturningInsert
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.query.fluent.Inserted
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.RowSequence
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class ConnectionWithDialect(
    val dialect: SqlDialect,
    val jdbc: Connection
) {
    private val typeMappings = TypeMappings()

    fun createTable(vararg tables: Table) {
        ddl(createTables(*tables))

        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }
    }

    fun <F : Any, T : Any> registerType(mapping: MappedDataType<T, F>) {
        typeMappings.register(mapping)
    }

    private fun prepare(sql: SqlText, generatedKeys: Boolean = false): PreparedStatement {
        val result = if (generatedKeys) {
            jdbc.prepareStatement(sql.sql, Statement.RETURN_GENERATED_KEYS)
        } else {
            jdbc.prepareStatement(sql.sql)
        }

        sql.parameters.forEachIndexed { ix, it ->
            result.setObject(ix + 1, typeMappings.unconvert(it).value)
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

    fun execute(insert: Inserted) {
        val built = insert.buildInsert()

        val sql = dialect.compile(built)

        prepare(sql).execute()
    }

    fun execute(updated: Updated) {
        val built = updated.buildUpdate()

        val sql = dialect.compile(built)

        try {
            prepare(sql).execute()
        } catch (ex: Exception) {
            throw GeneratedSqlException(sql, ex)
        }
    }

    fun query(queryable: Queryable): RowSequence {
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
}