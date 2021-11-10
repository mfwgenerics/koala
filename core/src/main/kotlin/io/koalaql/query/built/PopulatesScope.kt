package io.koalaql.query.built

import io.koalaql.sql.Scope

interface PopulatesScope {
    fun populateScope(scope: Scope)
}