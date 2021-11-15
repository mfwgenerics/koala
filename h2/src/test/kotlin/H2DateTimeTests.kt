import io.koalaql.DeclareStrategy
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import kotlin.test.Test

class H2DateTimeTests: DateTimeTests() {
    override fun connect(db: String, declareBy: DeclareStrategy): JdbcDataSource =
        H2Database(db, declareBy = declareBy)

    @Test
    fun empty() { }
}