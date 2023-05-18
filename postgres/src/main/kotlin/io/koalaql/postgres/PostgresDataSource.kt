package io.koalaql.postgres

import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import java.sql.DatabaseMetaData

fun PostgresDataSource(
    provider: JdbcProvider,
    declareBy: DeclareStrategy = DeclareStrategy.DoNothing,
    events: DataSourceEvent = DataSourceEvent.DISCARD,
    typeMappings: JdbcTypeMappings = PostgresTypeMappings()
): JdbcDataSource = JdbcDataSource(
    object : JdbcSchemaDetection {
        override fun detectChanges(dbName: String, metadata: DatabaseMetaData, tables: List<Table>): SchemaChange {
            return PostgresSchemaDiff(dbName, metadata).detectChanges(tables)
        }
    },
    PostgresDialect(),
    provider,
    typeMappings,
    declareBy,
    events
)
