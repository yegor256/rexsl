/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

RestTester.start(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('reading basic links')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath("/page/links/link[rel='self']")
    .assertXPath("/page/links/link[rel='home']")
