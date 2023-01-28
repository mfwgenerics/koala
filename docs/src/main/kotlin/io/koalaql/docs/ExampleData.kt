package io.koalaql.docs

import io.koalaql.DataSource
import io.koalaql.sql.CompiledSql
import kotlin.reflect.full.isSubclassOf

class ExampleData(
    val db: DataSource,

    val hardwareStoreId: Int,
    val groceryStoreId: Int,

    val logged: MutableList<CompiledSql>
) {
    private fun CompiledSql.unparameterize(): String {
        val splat = parameterizedSql
            .split('?')

        val rebuilt = StringBuilder()

        val parameters = parameters

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
                    else -> error("can't unparameterize $this")
                }
            }

            rebuilt.append(unsafeRawSql)
            rebuilt.append(part)
        }

        return "$rebuilt"
    }

    fun popGeneratedSql(): String {
        val result = logged
            .asSequence()
            .map { it.unparameterize() }
            .joinToString("\n\n")

        logged.clear()

        return result
    }
}
