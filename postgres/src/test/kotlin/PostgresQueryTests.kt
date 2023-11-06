import io.koalaql.Isolation
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import io.koalaql.expr.Expr
import io.koalaql.expr.ExtendedOperationType
import io.koalaql.expr.OperationFixity
import io.koalaql.postgres.generatePostgresSql
import kotlin.test.assertEquals

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

    private object OnConflictTable: Table("OnConflictWhere") {
        val column1 = column("column_1", INTEGER)
        val column2 = column("column_2", INTEGER)

        val c1c2 = uniqueKey(column1, column2)
    }

    @Test
    fun `on conflict where`() = withCxn(OnConflictTable) { cxn ->
        val baseInsert = OnConflictTable
            .insert(rowOf(
                OnConflictTable.column1 setTo 1,
                OnConflictTable.column2 setTo 2
            ))

        val indexCase = baseInsert
            .onConflict(OnConflictTable.c1c2)
            .ignore()
            .apply { perform(cxn) }
            .generatePostgresSql()

        val doubleColumn = baseInsert
            .onConflict(OnConflictTable.column1, OnConflictTable.column2)
            .ignore()
            .apply { perform(cxn) }
            .generatePostgresSql()

        val withWhere = baseInsert
            .onConflict(OnConflictTable.column1, OnConflictTable.column2)
            .where(OnConflictTable.column2 greater 0)
            .ignore()
            .apply { perform(cxn) }
            .generatePostgresSql()

        assertEquals("""
            INSERT INTO "OnConflictWhere" AS T0("column_1", "column_2")
            VALUES (?, ?)
            ON CONFLICT ON CONSTRAINT "OnConflictWhere_column_1_column_2_key" DO NOTHING
        """.trimIndent(), indexCase?.parameterizedSql)

        assertEquals("""
            INSERT INTO "OnConflictWhere" AS T0("column_1", "column_2")
            VALUES (?, ?)
            ON CONFLICT ("column_1", "column_2") DO NOTHING
        """.trimIndent(), doubleColumn?.parameterizedSql)

        assertEquals("""
            INSERT INTO "OnConflictWhere" AS T0("column_1", "column_2")
            VALUES (?, ?)
            ON CONFLICT ("column_1", "column_2")
            WHERE "column_2" > ? DO NOTHING
        """.trimIndent(), withWhere?.parameterizedSql)
    }
}