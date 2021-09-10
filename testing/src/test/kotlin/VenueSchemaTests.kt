import mfwgenerics.kotq.test.table.UserTable
import mfwgenerics.kotq.test.table.UserVenueTable
import mfwgenerics.kotq.test.table.VenueTable
import kotlin.test.Test

abstract class VenueSchemaTests: ProvideTestDatabase {
    @Test
    fun `create schema`() = withDb { db ->
        db.createTables(VenueTable, UserTable, UserVenueTable)
        db.createTables(VenueTable, UserTable, UserVenueTable)
    }
}