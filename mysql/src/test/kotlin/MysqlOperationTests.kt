import kotlin.test.Test

class MysqlOperationTests: OperationTests() {
    override val REQUIRES_MYSQL_WORKAROUND = true

    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}