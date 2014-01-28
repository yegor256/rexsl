/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.jcabi.http.request.ApacheRequest
import com.jcabi.http.response.RestResponse
import com.jcabi.http.response.XmlResponse
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

Manifests.append(new File(rexsl.basedir, 'src/test/resources/META-INF/MANIFEST.MF'))
def version = Manifests.read('Example-Version')
def revision = Manifests.read('Example-Revision')
def date = Manifests.read('Example-Date')

new ApacheRequest(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .fetch()
    .as(RestResponse.class)
    .assertStatus(HttpURLConnection.HTTP_OK)
    .as(XmlResponse.class)
    .assertXPath("/page/version[name='${version}']")
    .assertXPath("/page/version[revision='${revision}']")
    .assertXPath("/page/version[date='${date}']")
