package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoUpdate {
    fun buildUpdate() = unfoldBuilder(BuiltUpdate()) {
        buildsIntoUpdate(it)
    }

    fun buildsIntoUpdate(out: BuiltUpdate): BuildsIntoUpdate?
}