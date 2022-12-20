import io.koalaql.ddl.FLOAT
import io.koalaql.ddl.JSON
import io.koalaql.ddl.JsonData
import io.koalaql.test.data.DataTypeValuesMap
import kotlin.test.Test

class MysqlDataTypeTests: DataTypesTest(), MysqlTestProvider {
    override fun compatibilityAdjustment(values: DataTypeValuesMap) {
        /*
        float MAX values are incorrectly treated as doubles by mysql connector.
        it seems to be arbitrary which float values can be read back exactly
        */
        values[FLOAT] = listOf(-482824.0f, 0.0f, Float.MIN_VALUE, 1.4322f, 2.0f, 4853.0f)

        /* MYSQL canonical formatting */
        values[JSON] = listOf(JsonData("""{"items": [{"test": {}}]}"""))
    }

    @Test
    fun empty() { }
}