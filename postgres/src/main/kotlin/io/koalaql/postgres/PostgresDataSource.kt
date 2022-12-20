package io.koalaql.postgres

import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider

fun PostgresDataSource(
    provider: JdbcProvider,
    declareBy: DeclareStrategy = DeclareStrategy.DoNothing,
    events: DataSourceEvent = DataSourceEvent.DISCARD
): JdbcDataSource = JdbcDataSource(
    JdbcSchemaDetection.NotSupported,
    PostgresDialect(),
    provider,
    PostgresTypeMappings(),
    declareBy,
    events
)