package io.koalaql.identifier

class Unnamed: LabelIdentifier {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)

    override fun toString(): String = "unnamed"
}