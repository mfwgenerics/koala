package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.Queryable
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.RowSequence
import java.sql.Connection

class ConnectionWithDialect(
    val dialect: SqlDialect,
    val jdbc: Connection
) {
    fun query(queryable: Queryable): RowSequence {
        val built = queryable.buildQuery()

        val sql = dialect.compile(built)

        val prepared = jdbc.prepareStatement(sql.sql)

        sql.parameters.forEachIndexed { ix, it ->
            prepared.setObject(ix + 1, it)
        }

        return object : RowSequence {
            override val columns: LabelList = built.columns

            override fun rowIterator(): RowIterator {
                val results = prepared.executeQuery()

                return AdaptedResultSet(columns, results)
            }
        }
    }
}