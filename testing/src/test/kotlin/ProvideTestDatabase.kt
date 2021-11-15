import io.koalaql.*
import io.koalaql.ddl.Table
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.logging.SqlTestLintingLogger
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String, declareBy: DeclareStrategy): JdbcDataSource

    fun withDb(declareBy: DeclareStrategy = CreateIfNotExists, block: (JdbcDataSource) -> Unit) {
        val testDb = connect("db${SecureRandom().nextLong().absoluteValue}", declareBy)

        try {
            block(testDb)
        } finally {
            testDb.close()
        }
    }

    fun withCxn(vararg tables: Table, block: (DataConnection) -> Unit) = withDb { db ->
        db.declareTables(*tables)

        val events = SqlTestLintingLogger

        db.transact(events = events) { block(it) }
    }
}