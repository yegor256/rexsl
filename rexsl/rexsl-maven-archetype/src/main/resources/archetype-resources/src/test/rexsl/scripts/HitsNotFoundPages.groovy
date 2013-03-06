/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.rexsl.test.RestTester
import javax.ws.rs.core.UriBuilder

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
