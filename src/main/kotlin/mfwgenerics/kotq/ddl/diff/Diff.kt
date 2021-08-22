package mfwgenerics.kotq.ddl.diff

data class Diff<K, C, A>(
    val created: MutableMap<K, C> = hashMapOf(),
    val altered: MutableMap<K, A> = hashMapOf(),
    val dropped: MutableSet<K> = hashSetOf()
)
