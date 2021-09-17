package io.koalaql

inline fun <T, B> B.unfoldBuilder(result: T, unfold: B.(T) -> B?): T {
    var next = unfold(result)

    while (next != null) next = next.unfold(result)

    return result
}