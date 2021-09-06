package mfwgenerics.kotq

import mfwgenerics.kotq.query.Performable

interface KotqConnection {
    fun <T> perform(performable: Performable<T>): T

    fun commit()
    fun rollback()

    /* should guarantee changes are not committed */
    fun close()
}