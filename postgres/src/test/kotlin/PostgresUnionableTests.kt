import org.junit.Test

class PostgresUnionableTests: UnionableTests() {
    override fun connect(db: String) = PgTestDatabase(db)

    @Test
    fun empty() { }
}