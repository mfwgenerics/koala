package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.Updated
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Queryable
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
    fun prepareWithGeneratedKeys(sql: SqlText): PreparedStatement {
        val result = jdbc.prepareStatement(sql.sql, Statement.RETURN_GENERATED_KEYS)

        sql.parameters.forEachIndexed { ix, it ->
            result.setObject(ix + 1, it)
        }

        return result
    }

    fun prepare(sql: SqlText): PreparedStatement {
        val result = jdbc.prepareStatement(sql.sql)

        sql.parameters.forEachIndexed { ix, it ->
            result.setObject(ix + 1, it)
        }

        return result
    }

    fun ddl(diff: SchemaDiff) {
        dialect.ddl(diff).forEach {
            prepare(it).execute()
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

        prepare(sql).execute()
    }

    fun query(queryable: Queryable): RowSequence {
        val built = queryable.buildQuery()

        when (built) {
            is BuiltReturningInsert -> {
                fun err(): Nothing = error("RETURNING must expose a single auto-generated key")

                val sql = dialect.compile(built.insert)

                val prepared = prepareWithGeneratedKeys(sql)

                val keys = built.returning
                if (keys.size != 1) err()

                val column = keys[0]
                if (column !is TableColumn) err()

                if (!column.builtDef.autoIncrement) err()

                prepared.execute()

                return object : RowSequence {
                    override val columns: LabelList = LabelList(built.returning)

                    override fun rowIterator(): RowIterator {
                        return AdaptedResultSet(columns, prepared.generatedKeys)
                    }
                }
            }
            is BuiltSubquery -> {
                val sql = dialect.compile(built)

                val prepared = prepare(sql)

                val results = prepared.executeQuery()

                return object : RowSequence {
                    override val columns: LabelList = built.columns

                    override fun rowIterator(): RowIterator {
                        return AdaptedResultSet(columns, results)
                    }
                }
            }
        }
    }
}