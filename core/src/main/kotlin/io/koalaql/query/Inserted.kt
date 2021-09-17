package io.koalaql.query

import io.koalaql.query.built.BuildsIntoInsert

interface Inserted: PerformableStatement, BuildsIntoInsert