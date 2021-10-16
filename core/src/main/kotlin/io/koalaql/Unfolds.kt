package io.koalaql

/*
Builders define a receiver method which is used in `unfold` here. we choose
this slightly indirect method so we can keep builder interfaces public without
polluting subtype scopes with "buildX" methods

for example typing MyTable. in the IDE should never auto-suggest `.buildIntoInsert()`

dialect and middleware client code can still call BuiltInsert.from(MyTable)
*/

inline fun <T, B> unfoldBuilder(builder: B, result: T, unfold: B.(T) -> B?): T {
    var next = unfold(builder, result)

    while (next != null) next = next.unfold(result)

    return result
}