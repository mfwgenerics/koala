import io.koalaql.ddl.*
import kotlin.test.Test

class PostgresDdlTests: DdlTests(), PostgresTestProvider {
    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return when (type) {
            TINYINT, TINYINT.UNSIGNED, SMALLINT.UNSIGNED, INTEGER.UNSIGNED, BIGINT.UNSIGNED, is VARBINARY -> false
            else -> true
        }
    }

    @Test
    fun empty() { }
}