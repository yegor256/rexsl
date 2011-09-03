import static com.rexsl.test.TestClient
import static com.rexsl.test.XmlConverter.the
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.*
import static org.xmlmatchers.XmlMatchers.hasXPath

def r1 = TestClient
  .header('Accept', 'application/xml')
  .header('User-agent', 'Safari')
  .get('/')
assertThat(the(r1), hasXPath("//div[contains(.,'world')]"))
assertThat(r1.status, equalTo(200))

def r2 = TestClient.get('/strange-address')
assertThat(r2.body, containsString('Page not found'))
assertThat(r2.status, equalTo(404))
