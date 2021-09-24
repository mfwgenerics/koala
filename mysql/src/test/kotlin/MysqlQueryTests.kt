import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}