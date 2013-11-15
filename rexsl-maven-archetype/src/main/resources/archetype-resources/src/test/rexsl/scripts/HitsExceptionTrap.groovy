/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.rexsl.test.ApacheRequest
import com.rexsl.test.RestResponse
import com.rexsl.test.XmlResponse

new ApacheRequest(rexsl.home)
    .uri().path('/trap').back()
    .fetch()
    .as(RestResponse.class)
    .assertStatus(HttpURLConnection.HTTP_OK)
    .as(XmlResponse.class)
    .assertXPath('//xhtml:title[.="Internal application error"]')
