import static com.rexsl.test.XhtmlConverter.the
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.containsString
import static org.xmlmatchers.XmlMatchers.hasXPath

assertThat(document, containsString('say hello'))
assertThat(the(document), hasXPath("//div[contains(.,'say hello')]"))
