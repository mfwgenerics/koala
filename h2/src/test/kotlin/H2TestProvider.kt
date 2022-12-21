import io.koalaql.DeclareStrategy
import io.koalaql.event.DataSourceEvent
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource

interface H2TestProvider: ProvideTestDatabase {
    override fun connect(
        db: String,
        declareBy: DeclareStrategy,
        events: DataSourceEvent
    ): JdbcDataSource =
        H2Database(db, declareBy = declareBy, events = events)
}