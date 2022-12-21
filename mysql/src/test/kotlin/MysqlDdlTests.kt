import io.koalaql.ddl.UnmappedDataType
import kotlin.test.Test

class MysqlDdlTests: DdlTests(), MysqlTestProvider {
    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return true
    }

    @Test
    fun empty() { }
}