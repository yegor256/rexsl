/**
 * Copyright (c) 2011, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.logging.Log;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Grizzly test case.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class GrizzlyTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    /**
     * Initialize SLF4J bridge.
     */
    @BeforeClass
    public static void initSlf4jBridge() {
        org.slf4j.impl.StaticLoggerBinder.getSingleton()
            .setMavenLog(Mockito.mock(Log.class));
    }

    /**
     * Validate correct XML+XSL transformation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testGrizzlyDeployment() throws Exception {
        final File webdir = this.temp.newFolder("webdir");
        FileUtils.writeStringToFile(
            new File(webdir, "WEB-INF/web.xml"),
            "<web-app version='3.0' metadata-complete='false'"
            + " xmlns='http://java.sun.com/xml/ns/javaee'>"
            + "<description>test</description>"
            + "</web-app>"
        );
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(webdir).when(env).webdir();
        final Integer port = new PortReserver().port();
        final Grizzly grizzly = Grizzly.start(port, env);
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response =
            client.execute(new HttpGet("http://localhost:" + port));
        MatcherAssert.assertThat(
            response.getStatusLine().getStatusCode(),
            Matchers.equalTo(HttpStatus.SC_OK)
        );
    }

}
