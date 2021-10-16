package io.koalaql.data

import io.koalaql.ddl.*

fun <T : Any> computeWithColumnDefaults(type: UnmappedDataType<*>, block: (type: UnmappedDataType<*>) -> T?): T {
    var nextType = type

    while (true) {
        block(nextType)?.let { return it }

        nextType = when (nextType) {
            BOOLEAN -> TINYINT
            TINYINT -> SMALLINT
            TINYINT.UNSIGNED -> TINYINT
            SMALLINT.UNSIGNED -> SMALLINT
            INTEGER.UNSIGNED -> INTEGER
            BIGINT.UNSIGNED -> BIGINT
            else -> error("no mapping for $nextType")
        }
    }
}