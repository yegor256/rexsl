/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.WebApplicationException;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

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
        final JaxbPage page = res.index();
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XhtmlMatchers.hasXPaths(
                "/page/message[.='Hello, world!']",
                "/page/version[name='1.0-SNAPSHOT']"
            )
        );
    }

}
