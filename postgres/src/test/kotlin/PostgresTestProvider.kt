import io.koalaql.DeclareStrategy
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource

interface PostgresTestProvider: ProvideTestDatabase {
    override fun connect(
        db: String,
        declareBy: DeclareStrategy,
        events: DataSourceEvent
    ): JdbcDataSource = PgTestDatabase(db, declareBy, events)
}