package io.koalaql.query.built

import io.koalaql.query.WithType

interface BuiltWithable {
    var withType: WithType
    var withs: List<BuiltWith>
}