package mfwgenerics.kotq.window

import mfwgenerics.kotq.IdentifierName

class WindowLabel(
    val identifier: IdentifierName = IdentifierName()
): Window {
    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow? {
        window.partitions.from = this
        return null
    }

    override fun equals(other: Any?): Boolean =
        other is WindowLabel && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}