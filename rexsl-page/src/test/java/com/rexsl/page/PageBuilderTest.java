/**
 * Copyright (c) 2011-2014, ReXSL.com
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
package com.rexsl.page;

import com.rexsl.core.annotations.Stylesheet;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link PageBuilder}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class PageBuilderTest {

    /**
     * Object can be converted to XML through JAXB.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsJaxbPageConvertableToXml() throws Exception {
        final String stylesheet = "test-stylesheet";
        final Object page = new PageBuilder()
            .stylesheet(stylesheet)
            .build(BasePageMocker.class)
            .init(new ResourceMocker().mock());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XhtmlMatchers.hasXPath("/foo/message[contains(.,'hello')]")
        );
    }

    /**
     * Child class can be converted to XML through JAXB.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsJaxbPageFromBareClass() throws Exception {
        new PageBuilder().build(BarePage.class);
    }

    /**
     * PageBuilder can add correct annotations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsCorrectAnnotations() throws Exception {
        final String xsl = "/some/path/test.xsl";
        final Object page = new PageBuilder()
            .stylesheet(xsl)
            .build(PageBuilderTest.BarPage.class);
        MatcherAssert.assertThat(
            page.getClass().getAnnotation(XmlType.class).name(),
            Matchers.equalTo(
                "com.rexsl.page.PageBuilderTest$BarPage$somepathtestxsl"
            )
        );
        MatcherAssert.assertThat(
            page.getClass().getAnnotation(Stylesheet.class).value(),
            Matchers.equalTo(xsl)
        );
    }

    /**
     * Sample dummy page.
     */
    @XmlRootElement(name = "bar")
    public static class BarPage {
    }

    /**
     * Sample dummy page.
     */
    public static class BarePage extends BasePageMocker {
    }

}
