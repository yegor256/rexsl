/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.jcabi.manifests.Manifests;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.Resource;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract RESTful resource.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Resource.Forwarded
@Inset.Default({ LinksInset.class, FlashInset.class })
public class BaseRs extends BaseResource {

    /**
     * Inset with a version of the product.
     * @return The inset
     */
    @Inset.Runtime
    public Inset ver() {
        return new VersionInset(
            Manifests.read("Example-Version"),
            Manifests.read("Example-Revision"),
            Manifests.read("Example-Date")
        );
    }

    /**
     * Supplementary inset.
     * @return The inset
     */
    @Inset.Runtime
    public Inset supplementary() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
            }
        };
    }

}
