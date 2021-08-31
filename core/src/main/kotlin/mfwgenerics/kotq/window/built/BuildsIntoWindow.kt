package mfwgenerics.kotq.window.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoWindow {
    fun buildWindow(): BuiltWindow =
        unfoldBuilder(BuiltWindow()) { buildIntoWindow(it) }

    fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow?
}