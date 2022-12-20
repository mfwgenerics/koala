import org.junit.Test

class H2OperationTests: OperationTests(), H2TestProvider {
    override val windowRequiresOrderBy: Boolean get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}