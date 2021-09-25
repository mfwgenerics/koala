import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.mysql.MysqlSchemaDiff
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.mysql.MysqlDialect
import io.koalaql.mysql.MysqlTypeMappings
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DriverManager

fun MysqlTestDatabase(db: String): JdbcDataSource {
    val outerCxn = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root","my-secret-pw")

    outerCxn.prepareStatement("CREATE DATABASE $db").execute()

    return JdbcDataSource(
        object : JdbcSchemaDetection {
            override fun detectChanges(dbName: String, metadata: DatabaseMetaData, tables: List<Table>): SchemaChange =
                MysqlSchemaDiff(dbName, metadata).detectChanges(tables)
        },
        MysqlDialect(),
        object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:mysql://localhost:3306/$db", "root", "my-secret-pw")

            override fun close() {
                outerCxn.prepareStatement("DROP DATABASE $db").execute()
            }
        },
        MysqlTypeMappings(),
        DeclareStrategy.Change
    )
}