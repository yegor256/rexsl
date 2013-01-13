/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import org.hamcrest.Matchers

RestTester.start(UriBuilder.fromUri(rexsl.home).path('/trap'))
    .get('hits exception trap')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('//xhtml:title[.="Internal application error"]')
