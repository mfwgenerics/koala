import io.koalaql.DeclareStrategy
import org.junit.Test

class MysqlDateTimeTests: DateTimeTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) = MysqlTestDatabase(db,
        declareBy = declareBy
    )

    @Test
    fun empty() { }
}