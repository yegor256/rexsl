/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.jcabi.aspects.Loggable;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Index resource.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@Path("/")
public final class IndexRs extends BaseRs {

    /**
     * Get entrance page.
     * @return The JAX-RS response
     * @throws IOException If some IO problem inside
     */
    @GET
    @Path("/")
    @Loggable(Loggable.DEBUG)
    public Response index() throws IOException {
        return new PageBuilder()
            .stylesheet("/xsl/index.xsl")
            .build(BasePage.class)
            .init(this)
            .append(new JaxbBundle("message", "hello, world!"))
            .render()
            .build();
    }

}
