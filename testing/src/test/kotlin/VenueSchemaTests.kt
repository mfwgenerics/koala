import mfwgenerics.kotq.test.models.UserTable
import mfwgenerics.kotq.test.models.VenueTable
import kotlin.test.Test

abstract class VenueSchemaTests: ProvideTestDatabase {
    @Test
    fun `create schema`() = withDb { db ->
        db.createTables(VenueTable, UserTable)

        db.createTables(VenueTable, UserTable)
    }
}