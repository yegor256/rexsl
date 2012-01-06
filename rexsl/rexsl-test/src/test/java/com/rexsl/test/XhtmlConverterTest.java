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
package com.rexsl.test;

import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link XhtmlConverter}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlConverterTest {

    /**
     * XhtmlConverter can convert text to XML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void convertsTextToXml() throws Exception {
        final String text = "<html><body><p>\u0443</p></body></html>";
        MatcherAssert.assertThat(
            XhtmlConverter.the(text),
            XhtmlMatchers.hasXPath("/html/body/p[.='\u0443']")
        );
    }

    /**
     * XhtmlConverter can convert text to XML, with Unicode inside.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void convertsTextToXmlWithUnicode() throws Exception {
        final String text = "<a>\u8514  &#8250;</a>";
        MatcherAssert.assertThat(
            XhtmlConverter.the(text),
            XhtmlMatchers.hasXPath("/a")
        );
    }

    /**
     * XhtmlConverter can handle processing instructions.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void preservesProcessingInstructions() throws Exception {
        final String text = "<?xml version='1.0'?><?pi name='foo'?><a/>";
        MatcherAssert.assertThat(
            XhtmlConverter.the(text),
            XhtmlMatchers.hasXPath(
                "/processing-instruction('pi')[contains(.,'foo')]"
            )
        );
    }

    /**
     * XhtmlConverter can handle DOCTYPE, which potentially may cause troubles.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void processesDocumentsWithDoctype() throws Exception {
        final String text =
            // @checkstyle StringLiteralsConcatenation (6 lines)
            "<?xml version='1.0'?>"
            + "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'"
            + " 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>"
            + "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>"
            + "<body><p>\u0443\u0440\u0430!</p></body>"
            + "</html>";
        MatcherAssert.assertThat(
            XhtmlConverter.the(text),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/*"),
                XhtmlMatchers.hasXPath("//*"),
                XhtmlMatchers.hasXPath(
                    "/xhtml:html/xhtml:body/xhtml:p[.='\u0443\u0440\u0430!']"
                ),
                XhtmlMatchers.hasXPath("//xhtml:p[contains(., '\u0443')]")
            )
        );
    }

    /**
     * XhtmlConverter can convert W3C Node to XML.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void convertsNodeToXml() throws Exception {
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        final Element root = doc.createElement("foo");
        root.appendChild(doc.createTextNode("bar"));
        doc.appendChild(root);
        MatcherAssert.assertThat(
            XhtmlConverter.the(doc),
            XhtmlMatchers.hasXPath("/foo[.='bar']")
        );
    }

}
