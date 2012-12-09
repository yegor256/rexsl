/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.jcabi.manifests.Manifests;
import com.rexsl.page.JaxbBundle;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base RESTful page.
 *
 * <p>All other JAXB pages are inherited from this class, in runtime,
 * by means of {@link com.rexsl.page.PageBuilder}.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class BasePage extends com.rexsl.page.BasePage<BasePage, BaseRs> {

    /**
     * Render it.
     * @return JAX-RS response
     */
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
