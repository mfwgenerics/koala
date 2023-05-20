package io.koalaql.data

import io.koalaql.ddl.ExtendedDataType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class JdbcExtendedDataType<T : Any>(
    type: KType,
    override val sql: String,
    val jdbc: JdbcMappedType<T>
): ExtendedDataType<T>(type, sql) {
    companion object {
        inline operator fun <reified T : Any> invoke(sql: String, jdbc: JdbcMappedType<T>) = JdbcExtendedDataType<T>(
            typeOf<T>(),
            sql,
            jdbc
        )
    }
}