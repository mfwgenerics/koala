import io.koalaql.DeclareStrategy
import io.koalaql.test.AppliedDdlListener
import kotlin.test.assertEquals
import kotlin.test.Test

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
}