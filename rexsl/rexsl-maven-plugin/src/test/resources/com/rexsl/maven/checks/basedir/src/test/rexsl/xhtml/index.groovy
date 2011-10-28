import org.junit.Assert
import org.junit.matchers.JUnitMatchers
Assert.assertThat(
    rexsl.document,
    JUnitMatchers.containsString('hello')
)
