package mfwgenerics.kotq.sql

import mfwgenerics.kotq.window.WindowLabel

class WindowLabelRegistry {
    private val registered = hashMapOf<WindowLabel, String>()
    private var generated: Int = 0

    private fun generate(): String = "w${generated++}"

    operator fun get(label: WindowLabel): String =
        registered.getOrPut(label) { label
            .identifier.asString
            ?: generate()
        }
}