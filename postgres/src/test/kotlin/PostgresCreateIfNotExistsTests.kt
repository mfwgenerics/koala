import io.koalaql.DeclareStrategy
import io.koalaql.test.AppliedDdlListener
import kotlin.test.Test
import kotlin.test.assertEquals

class PostgresCreateIfNotExistsTests: CreateIfNotExistsTests(),  PostgresTestProvider {
    @Test
    fun `emit create if not exists for tables and indices`() {
        val appliedDdl = AppliedDdlListener()

        withDb(
            declareBy = DeclareStrategy.CreateIfNotExists,
            events = appliedDdl
        ) { db ->
            db.declareTables(CustomerTable)
        }

        assertEquals(
            """
                CREATE TABLE IF NOT EXISTS "Customer"(
                "id" INTEGER NOT NULL,
                "firstName" VARCHAR(101) NOT NULL,
                "lastName" VARCHAR(100) NOT NULL,
                CONSTRAINT "Customer_id_pkey" PRIMARY KEY ("id"),
                CONSTRAINT "Customer_firstName_lastName_key" UNIQUE ("firstName", "lastName")
                )
            """.trimIndent(),
            appliedDdl[0].toAbridgedSql()
        )

        assertEquals(
            """
                CREATE INDEX IF NOT EXISTS "reverseNameIndex" ON "Customer" ("lastName", "firstName")
            """.trimIndent(),
            appliedDdl[1].toAbridgedSql()
        )
    }

    @Test
    fun `table with mapped defaults`() {
        val appliedDdl = AppliedDdlListener()

        withDb(
            declareBy = DeclareStrategy.CreateIfNotExists,
            events = appliedDdl
        ) { db ->
            createAndCheckExample(db)
        }

        assertEquals(
            """
                CREATE TABLE IF NOT EXISTS "Example"(
                "id" INTEGER NOT NULL,
                "asString" VARCHAR(100) NOT NULL DEFAULT 'CASE_B',
                "trickyDefault" VARCHAR(100) NOT NULL DEFAULT '''"\$`''"$',
                CONSTRAINT "Example_id_pkey" PRIMARY KEY ("id")
                )
            """.trimIndent(),
            appliedDdl.single().toAbridgedSql()
        )
    }
}