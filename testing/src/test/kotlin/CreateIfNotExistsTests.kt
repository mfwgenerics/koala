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
}