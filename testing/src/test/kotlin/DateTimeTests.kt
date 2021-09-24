import io.koalaql.data.INSTANT
import io.koalaql.data.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.keys
import io.koalaql.dsl.values
import io.koalaql.jdbc.performWith
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
    fun `test insert and order by instant`() = withCxn(EventTable) { cxn, _ ->
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