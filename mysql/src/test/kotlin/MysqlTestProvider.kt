import io.koalaql.DeclareStrategy
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource

interface MysqlTestProvider: ProvideTestDatabase {
    override fun connect(
        db: String,
        declareBy: DeclareStrategy,
        events: DataSourceEvent
    ): JdbcDataSource = MysqlTestDatabase(db,
        declareBy = declareBy,
        events = events
    )
}