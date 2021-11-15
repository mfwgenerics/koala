import io.koalaql.DeclareStrategy
import org.junit.Test

class PostgresUnionableTests: UnionableTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) =
        PgTestDatabase(db, declareBy)

    @Test
    fun empty() { }
}