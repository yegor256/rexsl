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
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
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

    private StringWriter writer;

    private Appender appender;

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void addAppender() {
        this.writer = new StringWriter();
        this.appender = new WriterAppender(new SimpleLayout(), this.writer);
        Logger.getLogger("com.rexsl").addAppender(this.appender);
    }

    @After
    public void removeAppender() {
        Logger.getLogger(RuntimeFilter.class).removeAppender(this.appender);
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
            "<web-app version='3.0'"
            + " xmlns='http://java.sun.com/xml/ns/javaee'>"
            + "<filter>"
            + " <filter-name>XsltFilter</filter-name>"
            + " <filter-class>com.rexsl.core.XsltFilter</filter-class>"
            + " </filter>"
            + "<filter-mapping>"
            + " <filter-name>XsltFilter</filter-name>"
            + " <url-pattern>/*</url-pattern>"
            + " <dispatcher>REQUEST</dispatcher>"
            + "</filter-mapping>"
            + "</web-app>"
        );
        final Environment env = Mockito.mock(Environment.class);
        Mockito.doReturn(webdir).when(env).webdir();
        Mockito.doReturn(this.getClass().getClassLoader())
            .when(env).classloader();
        final Integer port = new PortReserver().port();
        final EmbeddedContainer container = EmbeddedContainer.start(port, env);
        final HttpURLConnection conn = (HttpURLConnection)
            new URL("http://localhost:" + port).openConnection();
        conn.connect();
        MatcherAssert.assertThat(
            conn.getResponseCode(),
            Matchers.describedAs(
                IOUtils.toString(conn.getInputStream()),
                Matchers.equalTo(HttpURLConnection.HTTP_OK)
            )
        );
        container.stop();
        final String[] messages = new String[] {
            "runtime filter initialized",
            "XSLT filter initialized",
            "XSLT filter destroyed"
        };
        for (String message : messages) {
            MatcherAssert.assertThat(
                this.writer.toString(),
                Matchers.containsString(message)
            );
        }
    }

}
