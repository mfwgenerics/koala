import io.koalaql.Isolation
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import kotlin.test.Test

class MysqlSchemaSupportTests: MysqlTestProvider {
    @Test
    fun `can select from an existing table in a schema`() = withDb { db ->
        db.connect(Isolation.READ_COMMITTED).use { cxn ->
            cxn.jdbc.prepareStatement("CREATE SCHEMA `test_Schema`").execute()
            cxn.jdbc.prepareStatement("CREATE TABLE `test_Schema`.`MyTable`(test int)").execute()
            cxn.jdbc.commit()
        }

        val myTable = object : Table("test_Schema", "MyTable") {
            val test = column("test", INTEGER.default(5))
        }

        myTable.insert(rowOf(myTable.test setTo 5)).perform(db)

        Unit
    }
}