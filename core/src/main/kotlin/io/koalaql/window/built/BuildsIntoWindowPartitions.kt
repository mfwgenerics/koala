package io.koalaql.window.built

interface BuildsIntoWindowPartitions: BuildsIntoWindow {
    fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): BuildsIntoWindowPartitions?

    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow? =
        buildIntoWindowPartitions(window.partitions)
}