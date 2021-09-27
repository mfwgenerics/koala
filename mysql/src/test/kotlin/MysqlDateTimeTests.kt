import org.junit.Test

class MysqlDateTimeTests: DateTimeTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}