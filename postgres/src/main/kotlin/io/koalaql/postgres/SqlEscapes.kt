package io.koalaql.postgres

import io.koalaql.expr.Literal
import io.koalaql.identifier.Named
import io.koalaql.sql.SqlEscapes
import org.postgresql.core.Utils

object PostgresDdlEscapes: SqlEscapes {
    override fun identifier(sql: StringBuilder, identifier: Named) {
        Utils.escapeIdentifier(sql, identifier.name)
    }

    override fun literal(sql: StringBuilder, params: MutableList<Literal<*>>, literal: Literal<*>) {
        when (literal.type) {
            Byte::class -> sql.append("${literal.value as Byte}")
            Short::class -> sql.append("${literal.value as Short}")
            Int::class -> sql.append("${literal.value as Int}")
            Long::class -> sql.append("${literal.value as Long}")
            Float::class -> sql.append("${literal.value as Float}")
            Double::class -> sql.append("${literal.value as Double}")
            String::class -> {
                sql.append("'")
                Utils.escapeLiteral(sql, literal.value as String, true)
                sql.append("'")
            }
            else -> error("${literal.type.simpleName} literals are not supported in Postgres DDL")
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