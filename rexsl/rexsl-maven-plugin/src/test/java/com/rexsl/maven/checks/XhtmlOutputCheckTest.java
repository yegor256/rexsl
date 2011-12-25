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
package com.rexsl.maven.checks;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link XhtmlOutputCheck}.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class XhtmlOutputCheckTest {

    /**
     * XhtmlOutputCheck can validate correct XML+XSL transformation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesPositiveSituation() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withFile("target/webdir/xsl/layout.xsl")
            .withFile("target/webdir/xsl/Home.xsl")
            .withFile("src/test/rexsl/xml/index.xml")
            .withFile("src/test/rexsl/xhtml/index.groovy")
            .mock();
        MatcherAssert.assertThat(
            "all tests pass fine",
            new XhtmlOutputCheck().validate(env)
        );
    }

    /**
     * XhtmlOutputCheck can validate incorrect XML+XSL transformation
     * (layout file is missed).
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesWithMissedLayoutFile() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withFile("target/webdir/xsl/Home.xsl")
            .withFile("src/test/rexsl/xml/index.xml")
            .mock();
        MatcherAssert.assertThat(
            "missed layout file situation is detected",
            !new XhtmlOutputCheck().validate(env)
        );
    }

    /**
     * XhtmlOutputCheck can validate incorrect XHTML.
     * @throws Exception If something goes wrong
     * @todo #11 Implement XHTML validation in XhtmlOutputCheck class.
     */
    @org.junit.Ignore
    @Test
    public void validatesIncorrectXhtml() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withFile("target/webdir/xsl/layout.xsl")
            .withFile("target/webdir/xsl/Home.xsl")
            .withFile("src/test/rexsl/xml/invalid-index.xml")
            .withFile("src/test/rexsl/xhtml/invalid-index.groovy")
            .mock();
        MatcherAssert.assertThat(
            "non-valid XHTML situation is detected",
            !new XhtmlOutputCheck().validate(env)
        );
    }

}
