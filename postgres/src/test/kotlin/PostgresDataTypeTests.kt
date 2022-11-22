import io.koalaql.DeclareStrategy
import io.koalaql.ddl.*
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test

class PostgresDataTypeTests: DataTypesTest() {
    override fun connect(db: String, declareBy: DeclareStrategy): JdbcDataSource =
        PgTestDatabase(db, declareBy)

    override fun compatibilityAdjustment(values: DataTypeValuesMap) {
        /* these are not supported by postgres */
        values.remove(TINYINT)
        values.remove(TINYINT.UNSIGNED)
        values.remove(SMALLINT.UNSIGNED)
        values.remove(INTEGER.UNSIGNED)
        values.remove(BIGINT.UNSIGNED)
        values.remove(VARBINARY(200))
    }

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}