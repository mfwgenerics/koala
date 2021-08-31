package mfwgenerics.kotq.expr

interface SelectArgument {
    fun buildIntoSelection(selection: SelectionBuilder)
}