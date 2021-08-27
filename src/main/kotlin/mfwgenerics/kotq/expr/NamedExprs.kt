package mfwgenerics.kotq.expr

interface NamedExprs {
    fun buildIntoSelection(selection: SelectionBuilder)
}