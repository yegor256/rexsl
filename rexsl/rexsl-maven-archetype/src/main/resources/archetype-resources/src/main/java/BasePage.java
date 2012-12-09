/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.rexsl.page.BasePage;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base RESTful page.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class BasePage extends com.rexsl.page.BasePage<CommonPage, BaseRs> {

    /**
     * Render it.
     * @return JAX-RS response
     */
    @Loggable(Loggable.DEBUG)
    public final Response.ResponseBuilder render() {
        final Response.ResponseBuilder builder = Response.ok();
        this.append(
            new JaxbBundle("version")
                .add("name", Manifests.read("Example-Version"))
                .up()
                .add("revision", Manifests.read("Example-Revision"))
                .up()
                .add("date", Manifests.read("Example-Date"))
                .up()
        );
        builder.entity(this);
        builder.type(MediaType.TEXT_XML);
        builder.header(HttpHeaders.VARY, "Cookie");
        return builder;
    }

}
