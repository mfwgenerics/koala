package io.koalaql.test

inline fun <T> retrying(attempts: Int = 6000, block: () -> T): T {
    lateinit var failedWith: Exception

    repeat(attempts) {
        try {
            return block()
        } catch (ex: Exception) {
            failedWith = ex
        }

        Thread.sleep(10)
    }

    throw failedWith
}