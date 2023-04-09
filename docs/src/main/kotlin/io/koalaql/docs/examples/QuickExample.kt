package io.koalaql.docs.examples

import io.koalaql.ddl.DATE
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.docs.KOALA_PUBLISHED_VERSION
import io.koalaql.docs.execBlock
import io.koalaql.dsl.*
import io.koalaql.h2.H2DataSource
import io.koalaql.kapshot.CaptureSource
import io.koalaql.kapshot.sourceOf
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.sql.DriverManager
import java.time.LocalDate

@CaptureSource
object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val name = column("name", VARCHAR(256))
    val address = column("address", VARCHAR(512))

    val established = column("established", DATE.nullable())
}

@CaptureSource
fun main() {
    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
        }
    )

    db.declareTables(ShopTable)

    val id = ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Helen's Hardware",
                ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                ShopTable.established setTo LocalDate.parse("1991-02-20")
            )
        ))
        .generatingKey(ShopTable.id)
        .perform(db)
        .single()

    val row = ShopTable
        .where(ShopTable.id eq id)
        .perform(db)
        .single()

    check("Helen's Hardware" == row[ShopTable.name])
}

fun DocusaurusMarkdownFile.quickExample() {
    h1("Quick example")

    h2("Under construction")

    main()

    code("kotlin", "Main.kt", "${sourceOf<ShopTable>()}\n\n${sourceOf(::main)}")
}