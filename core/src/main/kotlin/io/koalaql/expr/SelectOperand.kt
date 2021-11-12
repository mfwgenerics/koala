package io.koalaql.expr

interface SelectOperand<T : Any>: AsReference<T>, SelectArgument {
    override fun MutableSet<Reference<*>>.enforceUniqueReference() {
        val ref = asReference()

        check(add(ref)) {
            "duplicate reference: $ref"
        }
    }
}