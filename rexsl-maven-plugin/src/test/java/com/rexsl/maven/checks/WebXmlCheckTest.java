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
package com.rexsl.maven.checks;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import com.rexsl.test.RestTester;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Test case for {@link WebXmlCheck}.
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id$
 */
public final class WebXmlCheckTest {

    /**
     * Location of web.xml.
     */
    private static final String FILE = "src/main/webapp/WEB-INF/web.xml";

    /**
     * WebXmlCheck can validate correct web.xml file.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesCorrectWebXmlFile() throws Exception {
        Assume.assumeTrue(WebXmlCheckTest.online());
        final Environment env = new EnvironmentMocker()
            .withFile(WebXmlCheckTest.FILE, "valid-web.xml")
            .mock();
        MatcherAssert.assertThat(
            "valid web.xml passes without problems",
            new WebXmlCheck().validate(env)
        );
    }

    /**
     * WebXmlCheck can validate incorrect web.xml file.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesIncorrectWebXmlFile() throws Exception {
        Assume.assumeTrue(WebXmlCheckTest.online());
        final Environment env = new EnvironmentMocker()
            .withFile(WebXmlCheckTest.FILE, "invalid-web.xml")
            .mock();
        MatcherAssert.assertThat(
            "invalid web.xml is caught",
            !new WebXmlCheck().validate(env)
        );
    }

    /**
     * Are we onine?
     * @return TRUE if we're online
     */
    private static boolean online() {
        boolean online;
        try {
            online = RestTester.start(
                URI.create("http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd")
            ).get("validate it").getStatus() == HttpURLConnection.HTTP_OK;
        } catch (AssertionError ex) {
            online = false;
        }
        return online;
    }

}
