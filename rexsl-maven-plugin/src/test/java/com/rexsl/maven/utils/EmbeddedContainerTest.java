/**
 * Copyright (c) 2011-2013, ReXSL.com
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

import com.jcabi.log.Logger;
import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Grizzly test case.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class EmbeddedContainerTest {

    /**
     * Validate correct XML+XSL transformation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testEmbeddedDeployment() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile(
                "target/webdir/WEB-INF/web.xml",
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
                + "</filter-mapping>"
                + "</web-app>"
        )
            .withTextFile("target/webdir/file.txt", "some test data")
            .withTextFile("src/test/rexsl/setup/test.groovy", "assert true")
            .mock();
        final EmbeddedContainer container = EmbeddedContainer.start(env);
        final URI home = new URI(
            Logger.format("http://localhost:%d/file.txt", env.port())
        );
        RestTester.start(home)
            .get("get test data file")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.containsString("data"));
        container.stop();
        final String[] messages = new String[] {
            "runtime filter initialized",
            "XSLT filter initialized",
            "#doFilter(/file.txt)",
            "#filter(/file.txt): no files found",
            "runtime filter destroyed",
            "XSLT filter destroyed",
        };
    }

}
