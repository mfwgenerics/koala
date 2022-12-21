import io.koalaql.DataConnection
import io.koalaql.DeclareStrategy
import io.koalaql.ddl.Table
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.logging.SqlTestLintingLogger
import io.koalaql.transact
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(
        db: String,
        declareBy: DeclareStrategy,
        events: DataSourceEvent
    ): JdbcDataSource

    fun withDb(
        declareBy: DeclareStrategy = DeclareStrategy.CreateIfNotExists,
        events: DataSourceEvent = DataSourceEvent.DISCARD,
        block: (JdbcDataSource) -> Unit
    ) {
        connect("db${SecureRandom().nextLong().absoluteValue}", declareBy, events).use { testDb ->
            block(testDb)
        }
    }

    fun withCxn(vararg tables: Table, block: (DataConnection) -> Unit) = withDb { db ->
        db.declareTables(*tables)

        val events = SqlTestLintingLogger

        db.transact(events = events) { block(it) }
    }
}