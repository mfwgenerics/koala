import io.koalaql.docs.ExampleData
import io.koalaql.sql.CompiledSql
import kotlin.reflect.full.isSubclassOf
import kotlin.test.assertEquals

fun ExampleData.assertGeneratedSql(sql: String) {
    val actual = popGenerated()

    assertEquals(sql.trimIndent(), actual)

    logged.clear()
}