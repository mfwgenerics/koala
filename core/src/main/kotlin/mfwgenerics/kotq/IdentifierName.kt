package mfwgenerics.kotq

class IdentifierName(
    val asString: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is IdentifierName) return false

        if (asString == null && other.asString == null) return this === other

        return asString == other.asString
    }

    override fun hashCode(): Int =
        asString?.hashCode()?:System.identityHashCode(this)

    override fun toString(): String =
        if (asString != null) "$asString" else "<anonymous-${System.identityHashCode(this)}>"
}