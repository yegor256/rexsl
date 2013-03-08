/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.rexsl.test.RestTester
import javax.ws.rs.core.UriBuilder

[
    '/',
    '/robots.txt',
    '/xsl/layout.xsl',
    '/xsl/index.xsl',
    '/css/screen.css',
].each {
    RestTester.start(UriBuilder.fromUri(rexsl.home).path(it))
        .get('hits existing page')
        .assertStatus(HttpURLConnection.HTTP_OK)
}
