/**
 * Copyright (c) 2011-2012, ReXSL.com
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
package com.rexsl.test;

import com.ymock.util.Logger;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Node;

/**
 * Test case for {@link StringSource}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class StringSourceTest {

    /**
     * StringSource can format XML text propertly.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void formatsIncomingXmlDocument() throws Exception {
        final String xml = "<a><b>\u0443\u0440\u0430!</b></a>";
        final StringSource source = new StringSource(xml);
        MatcherAssert.assertThat(
            source.toString(),
            Matchers.containsString("&#443;")
        );
    }

    /**
     * Check convert node to string.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void formatIncomingNode() throws Exception {
        final DocumentBuilder builder = DocumentBuilderFactory.
            newInstance().
            newDocumentBuilder();
        final String xmlString = Logger.format(
            "<nodeName>%s%s%s<a/><a withArg=\"%s\"/></nodeName>",
            "<?some instruction?>",
            "<!--comment-->",
            "<a>withText</a>",
            "value"
        );
        final Node node = builder.parse(
            new ByteArrayInputStream(xmlString.getBytes())
        );
        final StringSource source = new StringSource(node);
        final String actual = source.toString();
        MatcherAssert.assertThat(
            actual, Matchers.containsString("nodeName")
        );
        MatcherAssert.assertThat(
            actual, Matchers.containsString("<a/>")
        );
        MatcherAssert.assertThat(
            actual, Matchers.containsString("withText")
        );
        MatcherAssert.assertThat(
            actual, Matchers.containsString("<a withArg=\"value\"/>")
        );
        MatcherAssert.assertThat(
            actual, Matchers.containsString("some instruction")
        );
        MatcherAssert.assertThat(
            actual, Matchers.containsString("comment")
        );
    }
}
