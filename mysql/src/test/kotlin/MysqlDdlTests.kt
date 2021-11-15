import io.koalaql.DeclareStrategy
import io.koalaql.ddl.UnmappedDataType
import kotlin.test.Test

class MysqlDdlTests: DdlTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) = MysqlTestDatabase(db,
        declareBy = declareBy
    )

    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return true
    }

    @Test
    fun empty() { }
}