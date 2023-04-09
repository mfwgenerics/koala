package io.koalaql.docs.start

import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.kapshot.Capturable
import io.koalaql.kapshot.Source

data class InstantiationContext(
    val jdbcUrl: String
)

fun interface Instantiation: Capturable<Instantiation> {
    operator fun InstantiationContext.invoke(): JdbcDataSource

    override fun withSource(source: Source): Instantiation {
        return object : Instantiation by this {
            override val source: Source = source
        }
    }
}