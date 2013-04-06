import org.hamcrest.Matchers
import org.hamcrest.MatcherAssert
MatcherAssert.assertThat(
    rexsl.document,
    Matchers.containsString('hello')
)
