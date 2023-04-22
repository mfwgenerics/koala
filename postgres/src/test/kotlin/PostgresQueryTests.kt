import io.koalaql.Isolation
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.iLike
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.value
import kotlin.test.Test
import kotlin.test.assertContentEquals
import io.koalaql.expr.Expr
import io.koalaql.expr.ExtendedOperationType
import io.koalaql.expr.OperationFixity

class PostgresQueryTests: QueryTests(), PostgresTestProvider {
    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    private fun iLikeTest(iLike: (Expr<String>, String) -> Expr<Boolean>) = withDb { db ->
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
            .where(iLike(myTable.name, "_a%"))
            .perform(db)
            .map { it[myTable.name] }
            .toList()

        assertContentEquals(result, listOf("Jase", "James"))
    }

    @Test
    fun `built-in ilike operator`() = iLikeTest { x, y -> x iLike y }

    private val customIlikeOp = ExtendedOperationType("ILIKE", OperationFixity.INFIX)

    @Test
    fun `custom ilike operator`() = iLikeTest { x, y -> customIlikeOp(x, value(y)) }
}