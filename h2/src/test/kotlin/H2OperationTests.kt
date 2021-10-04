import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import org.junit.Test

class H2OperationTests: OperationTests() {
    override fun connect(db: String): JdbcDataSource = H2Database(db)

    override val windowRequiresOrderBy: Boolean get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}