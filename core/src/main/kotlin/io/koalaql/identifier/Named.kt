package io.koalaql.identifier

class Named(
    val name: String
): LabelIdentifier {
    override fun equals(other: Any?): Boolean =
        other is Named && name == other.name

    override fun hashCode(): Int = name.hashCode()
    override fun toString(): String = name
}