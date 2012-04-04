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

import javax.xml.transform.Source;
import org.w3c.dom.Node;

/**
 * Convenient converter of text/node to XML source.
 *
 * <p>It's a convenient utility class to convert a string to XML DOM source,
 * which can be used then in assertions. For example:
 *
 * <pre>
 * import com.rexsl.test.XhtmlConverter;
 * import com.rexsl.test.XhtmlMatchers;
 * import org.junit.Assert;
 * import org.junit.Test;
 * public final class EmployeeTest {
 *   &#64;Test
 *   public void testXmlContent() throws Exception {
 *     String xml = ... // get it somewhere
 *     Assert.assertThat(
 *       XhtmlConverter.the(xml),
 *       XhtmlMatchers.hasXPath("/employee/name[.='John Doe']")
 *     );
 *   }
 * }
 * </pre>
 *
 * <p>The class is similar to {@code org.xmlmatchers.XmlConverters}, but it
 * doesn't crash on XML documents with links to external DTD's.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class XhtmlConverter {

    /**
     * Private ctor.
     */
    private XhtmlConverter() {
        // intentionally empty
    }

    /**
     * Convert it to XML.
     *
     * <p>The name of the method is motivated by
     * <a href="http://code.google.com/p/xml-matchers/">xmlatchers</a> project
     * and their {@code XmlMatchers.the(String)} method. Looks like this name
     * is short enough and convenient for unit tests.
     *
     * @param text The text to convert
     * @return DOM source/document
     */
    public static Source the(final String text) {
        return new StringSource(text);
    }

    /**
     * Convert provided DOM node to XML.
     * @param node The node to convert
     * @return DOM source/document
     */
    public static Source the(final Node node) {
        return new StringSource(node);
    }

}
