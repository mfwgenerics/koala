package io.koalaql.sql.token

import io.koalaql.identifier.SqlIdentifier

data class IdentifierToken(
    val identifier: SqlIdentifier
): SqlToken
