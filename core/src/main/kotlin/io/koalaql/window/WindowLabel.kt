package io.koalaql.window

import io.koalaql.identifier.LabelIdentifier
import io.koalaql.identifier.Unnamed
import io.koalaql.window.built.BuiltWindow
import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.fluent.Partitionable

class WindowLabel(
    val identifier: LabelIdentifier = Unnamed()
): Partitionable {
    override fun BuiltWindow.buildIntoWindow(): WindowBuilder? {
        partitions.from = this@WindowLabel
        return null
    }

    override fun equals(other: Any?): Boolean =
        other is WindowLabel && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}