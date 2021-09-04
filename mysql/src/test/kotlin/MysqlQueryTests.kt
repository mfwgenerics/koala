import mfwgenerics.kotq.test.TestDatabase
import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String): TestDatabase = MysqlTestDatabase(db)

    @Test
    fun `select version`() = withCxn { cxn ->
        val rs = cxn
            .jdbc
            .prepareStatement("SELECT VERSION()")
            .executeQuery()

        while (rs.next()) {
            println(rs.getString(1))
        }
    }
}