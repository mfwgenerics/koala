import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun `select version`() = withCxn { cxn, logs ->
        val rs = cxn
            .jdbc
            .prepareStatement("SELECT VERSION()")
            .executeQuery()

        while (rs.next()) {
            println(rs.getString(1))
        }
    }
}