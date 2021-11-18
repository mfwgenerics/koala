package io.koalaql.window

import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.fluent.Partitionable

class LabeledWindow(
    val window: Window,
    val label: WindowLabel
): Partitionable, WindowBuilder by label