import io.koalaql.docs.ExampleData
import kotlin.reflect.full.isSubclassOf
import kotlin.test.assertEquals

fun ExampleData.assertGeneratedSql(sql: String) {
    val actual = logged.asSequence()
        .map {
            val splat = it.parameterizedSql
                .split('?')

            val rebuilt = StringBuilder()

            val parameters = it.parameters

            splat.forEachIndexed { ix, part ->
                val unsafeRawSql = if (ix == 0) {
                    ""
                } else {
                    val param = parameters[ix - 1]
                    val type = param.type
                    val value = param.value

                    when {
                        type.isSubclassOf(Number::class) -> {
                            "$value"
                        }
                        type.isSubclassOf(CharSequence::class) -> {
                            "'${value.toString().replace("'", "\\'")}'"
                        }
                        else -> error("can't unparameterize $it")
                    }
                }

                rebuilt.append(unsafeRawSql)
                rebuilt.append(part)
            }

            "$rebuilt"
        }
        .joinToString("\n\n")

    assertEquals(sql.trimIndent(), actual)

    logged.clear()
}