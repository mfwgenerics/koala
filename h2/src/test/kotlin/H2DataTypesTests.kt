import io.koalaql.data.*
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test

class H2DataTypesTests: DataTypesTest() {
    override fun connect(db: String): JdbcDataSource = H2Database(db)

    @Test
    fun empty() { }

    override fun compatibilityAdjustment(values: DataTypeValuesMap) {
        /* H2 doesn't have UNSIGNED support - aliases the syntax to underlying int type */
        values.remove(TINYINT.UNSIGNED)
        values.remove(SMALLINT.UNSIGNED)
        values.remove(INTEGER.UNSIGNED)
        values.remove(BIGINT.UNSIGNED)
    }
}