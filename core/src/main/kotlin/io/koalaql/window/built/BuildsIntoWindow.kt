package io.koalaql.window.built

import io.koalaql.unfoldBuilder

interface BuildsIntoWindow {
    fun buildWindow(): BuiltWindow =
        unfoldBuilder(BuiltWindow()) { buildIntoWindow(it) }

    fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow?
}