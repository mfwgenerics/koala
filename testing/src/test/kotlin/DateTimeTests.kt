import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.TIMESTAMP
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.test.Test

abstract class DateTimeTests: ProvideTestDatabase {
    object EventTable: Table("Event") {
        val id = column("id", INTEGER.autoIncrement())

        val at = column("at", TIMESTAMP)

        init {
            primaryKey(keys(id))
        }
    }

    @Test
    fun `test insert and order by big instants`() = withCxn(EventTable) { cxn, _ ->
        val instants = (1..20L).map {
            Instant.EPOCH.plusSeconds(it*619315217)
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

    @Test
    fun `current timestamp is roughly the current time`() = withDb { db ->
        val timeExpr = currentTimestamp() as_ label()

        val currentTimeByDb = select(timeExpr)
            .performWith(db).single().first()

        assert(Duration.between(currentTimeByDb, Instant.now()).toMinutes().absoluteValue < 5)
    }
}