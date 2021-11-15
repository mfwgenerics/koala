import io.koalaql.DeclareStrategy
import kotlin.test.Test

class MysqlOperationTests: OperationTests() {
    override val REQUIRES_MYSQL_WORKAROUND = true

    override fun connect(db: String, declareBy: DeclareStrategy) = MysqlTestDatabase(db,
        declareBy = declareBy
    )

    @Test
    fun empty() { }
}