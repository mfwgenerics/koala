import mfwgenerics.kotq.test.TestDatabase
import kotlin.test.Test

class MysqlDdlTests: DdlTests() {
    override fun connect(db: String): TestDatabase = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}