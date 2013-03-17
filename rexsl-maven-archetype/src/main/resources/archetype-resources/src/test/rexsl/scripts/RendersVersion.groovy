/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

Manifests.append(new File(rexsl.basedir, 'src/test/resources/META-INF/MANIFEST.MF'))
def version = Manifests.read('Example-Version')
def revision = Manifests.read('Example-Revision')
def date = Manifests.read('Example-Date')

RestTester.start(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('reading version')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath("/page/version[name='${version}']")
    .assertXPath("/page/version[revision='${revision}']")
    .assertXPath("/page/version[date='${date}']")
