import io.koalaql.ddl.UnmappedDataType
import kotlin.test.Test

class MysqlDdlTests: DdlTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return true
    }

    @Test
    fun empty() { }
}