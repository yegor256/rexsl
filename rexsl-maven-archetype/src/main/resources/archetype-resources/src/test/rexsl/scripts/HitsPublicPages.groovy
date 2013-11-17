/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.rexsl.test.JdkRequest
import com.rexsl.test.RestResponse

[
    '/',
    '/robots.txt',
    '/xsl/layout.xsl',
    '/xsl/index.xsl',
    '/css/screen.css',
].each {
    new JdkRequest(rexsl.home)
        .uri().path(it).back()
        .fetch()
        .as(RestResponse.class)
        .assertStatus(HttpURLConnection.HTTP_OK)
}
