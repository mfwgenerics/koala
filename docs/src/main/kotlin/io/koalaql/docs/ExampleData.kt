package io.koalaql.docs

import io.koalaql.DataSource
import io.koalaql.sql.CompiledSql
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf

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

                when (type) {
                    typeOf<Byte>(),
                    typeOf<Short>(),
                    typeOf<Int>(),
                    typeOf<Long>(),
                    typeOf<Float>(),
                    typeOf<Double>(),
                    typeOf<BigInteger>(),
                    typeOf<BigDecimal>() -> "$value"
                    typeOf<String>() -> "'${value.toString().replace("'", "\\'")}'"
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
