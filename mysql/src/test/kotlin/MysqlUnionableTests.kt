import io.koalaql.CreateIfNotExists
import kotlin.test.Test

class MysqlUnionableTests: UnionableTests() {
    override fun connect(db: String) = MysqlTestDatabase(db, declareBy = CreateIfNotExists)

    @Test
    fun empty() { }
}