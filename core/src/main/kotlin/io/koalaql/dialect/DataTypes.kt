package io.koalaql.dialect

import io.koalaql.data.*
import io.koalaql.sql.SqlTextBuilder

fun SqlTextBuilder.compileDataType(type: UnmappedDataType<*>) {
    when (type) {
        DATE -> addSql("DATE")
        DATETIME -> addSql("TIMESTAMP WITHOUT TIME ZONE")
        is DECIMAL -> {
            addSql("DECIMAL")
            parenthesize {
                addSql("${type.precision},${type.scale}")
            }
        }
        DOUBLE -> addSql("DOUBLE")
        FLOAT -> addSql("REAL")
        INSTANT -> addSql("TIMESTAMP WITH TIME ZONE")
        TINYINT -> addSql("TINYINT")
        SMALLINT -> addSql("SMALLINT")
        INTEGER -> addSql("INTEGER")
        BIGINT -> addSql("BIGINT")
        is RAW -> addSql(type.sql)
        TIME -> addSql("TIME WITHOUT TIME ZONE")
        is VARBINARY -> {
            addSql("VARBINARY")
            parenthesize { addSql("${type.maxLength}") }
        }
        is VARCHAR -> {
            addSql("VARCHAR")
            parenthesize { addSql("${type.maxLength}") }
        }
        BOOLEAN -> addSql("BOOL")
        TEXT -> addSql("TEXT")
        TINYINT.UNSIGNED -> addSql("TINYINT UNSIGNED")
        SMALLINT.UNSIGNED -> addSql("SMALLINT UNSIGNED")
        INTEGER.UNSIGNED -> addSql("INTEGER UNSIGNED")
        BIGINT.UNSIGNED -> addSql("BIGINT UNSIGNED")
    }
}