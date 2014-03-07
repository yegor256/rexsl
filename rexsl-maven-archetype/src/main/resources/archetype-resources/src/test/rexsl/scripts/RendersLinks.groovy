/**
 * Copyright (c) 2011-2014, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.jcabi.http.request.ApacheRequest
import com.jcabi.http.response.RestResponse
import com.jcabi.http.response.XmlResponse
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

new ApacheRequest(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .fetch()
    .as(RestResponse.class)
    .assertStatus(HttpURLConnection.HTTP_OK)
    .as(XmlResponse.class)
    .assertXPath("/page/links/link[@rel='self']")
    .assertXPath("/page/links/link[@rel='home']")
