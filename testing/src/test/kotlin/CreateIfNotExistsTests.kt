import io.koalaql.DataSource
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.keys
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import kotlin.test.assertEquals

abstract class CreateIfNotExistsTests: ProvideTestDatabase {
    object CustomerTable : Table("Customer") {
        val id = column("id", INTEGER.primaryKey())

        val firstName = column("firstName", VARCHAR(101))
        val lastName = column("lastName", VARCHAR(100))

        val namesKey = uniqueKey(keys(firstName, lastName))

        init {
            index("reverseNameIndex", lastName, firstName)
        }
    }

    enum class ExampleEnum {
        CASE_A,
        CASE_B
    }

    object ExampleTable : Table("Example") {
        val id = column("id", INTEGER.primaryKey())

        val asString = column("asString", VARCHAR(100).mapToEnum<ExampleEnum> { it.name }
            .default(ExampleEnum.CASE_B))

        val trickyDefault = column("trickyDefault", VARCHAR(100).default("""'"\$`'"$"""))
    }

    fun createAndCheckExample(db: DataSource) {
        db.declareTables(ExampleTable)

        ExampleTable
            .insert(values(rowOf(ExampleTable.id setTo 1)))
            .perform(db)

        val default = ExampleTable
            .perform(db)
            .single()[ExampleTable.trickyDefault]

        assertEquals("""'"\$`'"$""", default)
    }
}