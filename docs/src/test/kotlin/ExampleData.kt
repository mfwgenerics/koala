import io.koalaql.docs.ExampleData
import kotlin.test.assertEquals

fun ExampleData.assertGeneratedSql(sql: String) {
    val actual = logged.asSequence()
        .map { it.parameterizedSql }
        .joinToString("\n\n")

    assertEquals(sql.trimIndent(), actual)

    logged.clear()
}