package io.koalaql.mysql

import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import java.sql.DatabaseMetaData

fun MysqlDataSource(provider: JdbcProvider): JdbcDataSource = JdbcDataSource(
    object : JdbcSchemaDetection {
        override fun detectChanges(dbName: String, metadata: DatabaseMetaData, tables: List<Table>): SchemaChange =
            MysqlSchemaDiff(dbName, metadata).detectChanges(tables)
    },
    MysqlDialect(),
    provider,
    MysqlTypeMappings(),
    DeclareStrategy.Change
)