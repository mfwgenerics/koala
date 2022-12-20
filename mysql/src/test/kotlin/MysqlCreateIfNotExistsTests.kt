import io.koalaql.DeclareStrategy
import io.koalaql.test.AppliedDdlListener
import kotlin.test.Test
import kotlin.test.assertEquals

class MysqlCreateIfNotExistsTests: CreateIfNotExistsTests(), MysqlTestProvider {
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
                CREATE TABLE IF NOT EXISTS `Customer`(
                `id` INTEGER NOT NULL,
                `firstName` VARCHAR(101) NOT NULL,
                `lastName` VARCHAR(100) NOT NULL,
                CONSTRAINT `Customer_id_pkey` PRIMARY KEY (`id`),
                CONSTRAINT UNIQUE KEY `Customer_firstName_lastName_key`(`firstName`, `lastName`),
                INDEX `reverseNameIndex`(`lastName`, `firstName`)
                )
            """.trimIndent(),
            appliedDdl.single().toAbridgedSql()
        )
    }
}