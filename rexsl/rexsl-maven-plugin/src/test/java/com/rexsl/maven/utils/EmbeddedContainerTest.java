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
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Grizzly test case.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class EmbeddedContainerTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Mocked Maven log.
     */
    private transient Log log;

    /**
     * Add LOG4J appender.
     * @throws Exception If something is wrong inside
     */
    @Before
    public void prepareLog() throws Exception {
        this.log = Mockito.mock(Log.class);
        org.slf4j.impl.StaticLoggerBinder.getSingleton().setMavenLog(this.log);
    }

    /**
     * Validate correct XML+XSL transformation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testEmbeddedDeployment() throws Exception {
        final File webdir = this.temp.newFolder("webdir");
        FileUtils.writeStringToFile(
            new File(webdir, "WEB-INF/web.xml"),
            // @checkstyle StringLiteralsConcatenation (15 lines)
            "<web-app version='3.0'"
            + " xmlns='http://java.sun.com/xml/ns/javaee'>"
            + "<filter>"
            + " <filter-name>XsltFilter</filter-name>"
            + " <filter-class>com.rexsl.core.XsltFilter</filter-class>"
            + "</filter>"
            + "<filter-mapping>"
            + " <filter-name>XsltFilter</filter-name> "
            + " <url-pattern>/*</url-pattern>"
            + " <dispatcher>REQUEST</dispatcher>"
            + " <dispatcher>ERROR</dispatcher>"
            + "</filter-mapping>"
            + "</web-app>"
        );
        FileUtils.writeStringToFile(
            new File(webdir, "file.txt"),
            "some test data"
        );
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(webdir).when(env).webdir();
        Mockito.doReturn(webdir).when(env).basedir();
        Mockito.doReturn(new PortReserver().port()).when(env).port();
        final EmbeddedContainer container = EmbeddedContainer.start(env);
        final HttpURLConnection conn = (HttpURLConnection)
            new URL("http", "localhost", env.port(), "/file.txt")
                .openConnection();
        conn.connect();
        final String content = IOUtils.toString(conn.getInputStream());
        MatcherAssert.assertThat(
            conn.getResponseCode(),
            Matchers.describedAs(
                content,
                Matchers.equalTo(HttpURLConnection.HTTP_OK)
            )
        );
        MatcherAssert.assertThat(content, Matchers.containsString("data"));
        container.stop();
        final String[] messages = new String[] {
            "runtime filter initialized",
            "XSLT filter initialized",
            "#doFilter(/file.txt)",
            "#filter(/file.txt): no files found",
            "runtime filter destroyed",
            "XSLT filter destroyed",
        };
        for (String message : messages) {
            // Mockito.verify(this.log, Mockito.atLeastOnce())
            //     .info(Mockito.eq(message));
            Mockito.verify(this.log, Mockito.atLeastOnce())
                .info(Mockito.any(String.class));
        }
    }

}
