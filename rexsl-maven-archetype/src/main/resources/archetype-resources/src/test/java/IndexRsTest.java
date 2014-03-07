/**
 * Copyright (c) 2011-2014, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.UriInfoMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link IndexRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class IndexRsTest {

    /**
     * IndexRs can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersFrontPage() throws Exception {
        final IndexRs res = new IndexRs();
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        final Response response = res.index();
        MatcherAssert.assertThat(
            JaxbConverter.the(response.getEntity()),
            XhtmlMatchers.hasXPaths(
                "/page/message[.='Hello, world!']",
                "/page/version[name='1.0-SNAPSHOT']"
            )
        );
    }

}
