/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import org.hamcrest.Matchers

[
    '/page-doesnt-exist',
    '/xsl/xsl-stylesheet-doesnt-exist.xsl',
    '/css/stylesheet-is-absent.css',
].each {
    RestTester.start(UriBuilder.fromUri(rexsl.home).path(it))
        .get('hits non-found page')
        .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
        .assertXPath('//xhtml:h1[contains(.,"Page not found")]')
}
