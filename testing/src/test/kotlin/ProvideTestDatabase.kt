import io.koalaql.*
import io.koalaql.ddl.Table
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.logging.SqlTestLintingLogger
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String, declareBy: DeclareStrategy = DeclareStrategy.CreateIfNotExists): JdbcDataSource

    fun withDb(declareBy: DeclareStrategy = DeclareStrategy.CreateIfNotExists, block: (JdbcDataSource) -> Unit) {
        connect("db${SecureRandom().nextLong().absoluteValue}", declareBy).use { testDb ->
            block(testDb)
        }
    }

    fun withCxn(vararg tables: Table, block: (DataConnection) -> Unit) = withDb { db ->
        db.declareTables(*tables)

        val events = SqlTestLintingLogger

        db.transact(events = events) { block(it) }
    }
}