import org.junit.Test

class H2UnionableTests: UnionableTests(), H2TestProvider {
    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}