import io.koalaql.Isolation
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.iLike
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PostgresQueryTests: QueryTests(), PostgresTestProvider {
    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    @Test
    fun `ilike operator`() = withDb { db ->
        db.connect(Isolation.READ_COMMITTED).use { cxn ->
            cxn.jdbc.prepareStatement("CREATE TABLE \"MyNames\" (name VARCHAR (72))").execute()
            cxn.jdbc.commit()
        }

        val myTable = object : Table("MyNames") {
            val name = column("name", VARCHAR(72).primaryKey())
        }

        listOf("Jo", "Jon", "Joe", "Jase", "James").forEach {
            myTable.insert(rowOf(myTable.name setTo it)).perform(db)
        }

        val result = myTable
            .where(myTable.name iLike "_a%")
            .perform(db)
            .map { it[myTable.name] }
            .toList()

        assertContentEquals(result, listOf("Jase", "James"))
    }
}