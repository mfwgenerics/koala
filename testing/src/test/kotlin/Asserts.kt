import kotlin.test.assertContains
import kotlin.test.assertEquals

// TODO use an assertion library

fun assertListEquals(expected: List<Any?>, actual: List<Any?>, message: String? = null) {
    assertEquals(expected.size, actual.size, message)

    repeat(expected.size) {
        assertEquals(expected[it], actual[it], message)
    }
}

fun assertSetEquals(expected: Set<Any?>, actual: Set<Any?>) {
    assertEquals(expected.size, actual.size)

    expected.forEach { assertContains(actual, it) }
}