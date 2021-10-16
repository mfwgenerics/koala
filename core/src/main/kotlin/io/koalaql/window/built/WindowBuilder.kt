package io.koalaql.window.built

interface WindowBuilder {
    fun BuiltWindow.buildIntoWindow(): WindowBuilder?
}