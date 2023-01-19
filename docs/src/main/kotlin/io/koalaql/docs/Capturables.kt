package io.koalaql.docs

import io.koalaql.kapshot.CapturedBlock

fun execBlock(block: CapturedBlock<*>): String {
    block.invoke()

    return block.source.text
}