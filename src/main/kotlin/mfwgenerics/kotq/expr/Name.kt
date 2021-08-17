package mfwgenerics.kotq.expr

class Name<T : Any>(
    val identifier: String? = null
): Named<T> {
    override val name: Name<T> get() = this

    override fun equals(other: Any?): Boolean {
        if (other !is Name<*>) return false

        if (identifier == null && other.identifier == null) return this === other

        return identifier == other.identifier
    }

    override fun hashCode(): Int =
        identifier?.hashCode()?:System.identityHashCode(this)

    override fun toString(): String = if (identifier != null) "$identifier" else "name"
}