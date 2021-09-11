import mfwgenerics.kotq.data.INSTANT
import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dsl.keys
import mfwgenerics.kotq.dsl.values
import mfwgenerics.kotq.jdbc.performWith
import java.time.Instant
import kotlin.test.Test

abstract class DateTimeTests: ProvideTestDatabase {
    object EventTable: Table("Event") {
        val id = column("id", INTEGER.autoIncrement())

        val at = column("at", INSTANT)

        init {
            primaryKey(keys(id))
        }
    }

    @Test
    fun `test insert and order by instant`() = withCxn { cxn ->
        cxn.createTable(EventTable)

        val instants = (1000..1010L).map {
            Instant.EPOCH.plusSeconds(it*24*60*60*7 + it)
        }

        EventTable
            .insert(values(
                instants.asSequence()
            ) {
                set(EventTable.at, it)
            })
            .performWith(cxn)

        EventTable
            .orderBy(EventTable.at.desc())
            .selectAll()
            .performWith(cxn)
            .forEachIndexed { ix, it ->
                val t = it.getOrNull(EventTable.at)!!

                assert(t == instants[instants.size - ix - 1])
            }
    }
}