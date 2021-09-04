package mfwgenerics.kotq.ddl.diff

data class Diff<K, C, A>(
    val created: MutableMap<K, C> = hashMapOf(),
    val altered: MutableMap<K, Alteration<C, A>> = hashMapOf(),
    val dropped: MutableSet<K> = hashSetOf()
)
