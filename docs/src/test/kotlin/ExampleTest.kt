import io.koalaql.docs.quickExample
import io.koalaql.docs.usage.rowExamples
import kotlin.test.Test

class ExampleTest {
    @Test
    fun `quick example works`() {
        quickExample()
    }

    @Test
    fun `results examples`() {
        rowExamples()
    }
}