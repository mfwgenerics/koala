import kotlin.test.assertContains
import kotlin.test.assertEquals

// TODO use an assertion library

fun assertListEquals(expected: List<Any?>, actual: List<Any?>, message: String? = null) {
    assertEquals(expected.size, actual.size, message)

    repeat(expected.size) { ix ->
        assertEquals(expected[ix], actual[ix], "at index $ix${message?.let { ": $it" }?:""}")
    }
}

fun assertSetEquals(expected: Set<Any?>, actual: Set<Any?>) {
    assertEquals(expected.size, actual.size)

    expected.forEach { assertContains(actual, it) }
}