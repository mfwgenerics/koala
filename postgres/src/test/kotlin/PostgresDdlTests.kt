import io.koalaql.ddl.UnmappedDataType
import kotlin.test.Test

class PostgresDdlTests: DdlTests(), PostgresTestProvider {
    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return true
    }

    @Test
    fun empty() { }
}