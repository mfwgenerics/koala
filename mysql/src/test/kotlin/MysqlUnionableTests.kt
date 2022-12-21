import kotlin.test.Test

class MysqlUnionableTests: UnionableTests(), MysqlTestProvider {
    override fun `order by on values`() {
        /* as of mysql 8.0.31 there is a regression that breaks usage of ORDER BY on VALUES clauses */
        try {
            super.`order by on values`()
            assert(false)
        } catch (_: AssertionError) {

        }
    }

    @Test
    fun empty() { }
}