/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.http.request.ApacheRequest
import com.jcabi.http.response.RestResponse
import com.jcabi.http.response.XmlResponse

new ApacheRequest(rexsl.home)
    .uri().path('/trap').back()
    .fetch()
    .as(RestResponse.class)
    .assertStatus(HttpURLConnection.HTTP_OK)
    .as(XmlResponse.class)
    .assertXPath('//xhtml:title[.="Internal application error"]')
