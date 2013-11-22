/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.rexsl.test.request.JdkRequest
import com.rexsl.test.response.RestResponse
import com.rexsl.test.response.XmlResponse

[
    '/page-doesnt-exist',
    '/xsl/xsl-stylesheet-doesnt-exist.xsl',
    '/css/stylesheet-is-absent.css',
].each {
    new JdkRequest(rexsl.home)
        .uri().path(it).back()
        .fetch()
        .as(RestResponse.class)
        .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
        .as(XmlResponse.class)
        .assertXPath('//xhtml:h1[contains(.,"Page not found")]')
}
