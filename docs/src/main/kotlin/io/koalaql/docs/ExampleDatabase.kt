package io.koalaql.docs

import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.event.DataSourceEvent
import io.koalaql.event.QueryEventWriter
import io.koalaql.h2.H2DataSource
import io.koalaql.sql.CompiledSql
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.LocalDate
import kotlin.random.Random

private class SqlLogger: DataSourceEvent by DataSourceEvent.DISCARD {
    val logged: MutableList<CompiledSql> = arrayListOf()

    private val connection = object : ConnectionEventWriter {
        override fun perform(type: ConnectionQueryType, sql: CompiledSql): QueryEventWriter {
            logged.add(sql)
            return QueryEventWriter.Discard
        }

        override fun committed(failed: Throwable?) { }
        override fun rollbacked(failed: Throwable?) { }

        override fun closed() { }
    }

    override fun connect(): ConnectionEventWriter = connection
}

fun ExampleDatabase(): ExampleData {
    val name = "test${Random.nextInt()}"

    val logger = SqlLogger()

    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1")
        },
        events = logger
    )

    db.declareTables(ShopTable, CustomerTable)

    val ids = ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Helen's Hardware",
                ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                ShopTable.established setTo LocalDate.parse("1991-02-20")
            ),
            rowOf(
                ShopTable.name setTo "24 Hr Groceries",
                ShopTable.address setTo "1/144 Ronda Drive, Newhaven, 226E",
                ShopTable.established setTo LocalDate.parse("2007-08-02")
            ),
            rowOf(
                ShopTable.name setTo "Permanently Closed Shop",
                ShopTable.address setTo "200 Omar Circle, Xanadu, 91A",
                ShopTable.established setTo LocalDate.parse("2007-08-02")
            )
        ))
        .generatingKey(ShopTable.id)
        .perform(db)
        .toList()

    CustomerTable
        .insert(values(
            rowOf(
                CustomerTable.shop setTo ids[0],
                CustomerTable.name setTo "Michael M. Michael",
                CustomerTable.spent setTo BigDecimal("125.00")
            ),
            rowOf(
                CustomerTable.shop setTo ids[0],
                CustomerTable.name setTo "Maria Robinson",
                CustomerTable.spent setTo BigDecimal("20.50")
            ),
            rowOf(
                CustomerTable.shop setTo ids[1],
                CustomerTable.name setTo "Angela Abara",
                CustomerTable.spent setTo BigDecimal("79.99")
            )
        ))
        .perform(db)

    logger.logged.clear()

    return ExampleData(
        db = db,
        hardwareStoreId = ids[0],
        groceryStoreId = ids[1],
        logged = logger.logged
    )
}

inline fun testExampleDatabase(block: ExampleData.() -> Unit) = with(ExampleDatabase()) {
    block()
}