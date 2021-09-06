import kotlin.test.Test

class MysqlDdlTests: DdlTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}