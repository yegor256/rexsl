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

import org.junit.Assert;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Test XHTML converter.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlConverterTest {

    /**
     * Test simple conversion.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTextToXmlConversion() throws Exception {
        final String text = "<html><body><p>test</p></body></html>";
        Assert.assertThat(
            XhtmlConverter.the(text),
            XmlMatchers.hasXPath("/html/body/p[.='test']")
        );
    }

    /**
     * Let's use DOCTYPE, which may cause troubles.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTextXhtmlWithDoctype() throws Exception {
        final String text =
            "<?xml version='1.0'?>"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
            + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
            + "<body><p>test</p></body>"
            + "</html>";
        final String[] paths = new String[] {
            "/*",
            "//*",
            "/x:html/x:body/x:p[.='test']",
            "//x:p[contains(., 't')]",
        };
        for (String path : paths) {
            Assert.assertThat(
                XhtmlConverter.the(text),
                XmlMatchers.hasXPath(
                    path,
                    new SimpleNamespaceContext().withBinding(
                        "x", "http://www.w3.org/1999/xhtml"
                    )
                )
            );
        }
    }

}
