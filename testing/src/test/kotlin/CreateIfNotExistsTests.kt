import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.keys

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

        val asInt = column("asInt", INTEGER.mapToEnum<ExampleEnum> { it.ordinal }
            .default(ExampleEnum.CASE_A))
        val asString = column("asString", INTEGER.mapToEnum<ExampleEnum> { it.ordinal }
            .default(ExampleEnum.CASE_B))
    }
}