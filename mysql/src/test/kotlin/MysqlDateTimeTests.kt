import io.koalaql.CreateIfNotExists
import org.junit.Test

class MysqlDateTimeTests: DateTimeTests() {
    override fun connect(db: String) = MysqlTestDatabase(db,
        declareBy = CreateIfNotExists
    )

    @Test
    fun empty() { }
}