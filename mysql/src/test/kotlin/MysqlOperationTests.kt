import kotlin.test.Test

class MysqlOperationTests: OperationTests(), MysqlTestProvider {
    override val REQUIRES_MYSQL_WORKAROUND = true

    @Test
    fun empty() { }
}