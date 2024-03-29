package io.koalaql.postgres

import io.koalaql.expr.Literal
import io.koalaql.identifier.Named
import io.koalaql.sql.SqlEscapes
import org.postgresql.core.Utils
import kotlin.reflect.typeOf

object PostgresDdlEscapes: SqlEscapes {
    override fun identifier(sql: StringBuilder, identifier: Named) {
        Utils.escapeIdentifier(sql, identifier.name)
    }

    override fun literal(sql: StringBuilder, params: MutableList<Literal<*>>, literal: Literal<*>) {
        if (literal.value == null) {
            sql.append("NULL")
            return
        }

        when (literal.type) {
            typeOf<Byte>() -> sql.append("${literal.value as Byte}")
            typeOf<Short>() -> sql.append("${literal.value as Short}")
            typeOf<Int>() -> sql.append("${literal.value as Int}")
            typeOf<Long>() -> sql.append("${literal.value as Long}")
            typeOf<Float>() -> sql.append("${literal.value as Float}")
            typeOf<Double>() -> sql.append("${literal.value as Double}")
            typeOf<String>() -> {
                sql.append("'")
                Utils.escapeLiteral(sql, literal.value as String, true)
                sql.append("'")
            }
            else -> error("${literal.type} literals are not supported in Postgres DDL")
        }
    }
}

object PostgresDmlEscapes: SqlEscapes {
    override fun identifier(sql: StringBuilder, identifier: Named) {
        Utils.escapeIdentifier(sql, identifier.name)
    }

    override fun literal(sql: StringBuilder, params: MutableList<Literal<*>>, literal: Literal<*>) {
        sql.append("?")
        params.add(literal)
    }
}