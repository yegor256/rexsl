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
package com.rexsl.core;

import com.rexsl.test.XhtmlMatchers;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link XslResolver}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class XslResolverTest {

    /**
     * XslResolver can instantiate a marshaller.
     * @throws Exception If something goes wrong
     */
    @Test
    public void instantiatesMarshaller() throws Exception {
        final ContextResolver<Marshaller> resolver = new XslResolver();
        final Marshaller mrsh = resolver.getContext(XslResolverTest.Page.class);
        MatcherAssert.assertThat(mrsh, Matchers.notNullValue());
    }

    /**
     * XslResolvers can avoid duplicated creation of marshaller.
     * @throws Exception If something goes wrong
     * @todo #3 This test is not working at the moment, but it should. We have
     *  to confirm that Marshaller is created only once per class.
     */
    @Ignore
    @Test
    public void avoidsDuplicatedMarshallerCreation() throws Exception {
        // not implemented yet
    }

    /**
     * XslResolver can handle dynamically extendable objects.
     * @throws Exception If something goes wrong
     */
    @Test
    public void handlesDynamicallyExtendableObject() throws Exception {
        final XslResolver resolver = new XslResolver();
        resolver.add(XslResolverTest.Injectable.class);
        final Marshaller mrsh = resolver.getContext(Page.class);
        final Page page = new XslResolverTest.Page();
        page.inject(new XslResolverTest.Injectable());
        final StringWriter writer = new StringWriter();
        mrsh.marshal(page, writer);
        MatcherAssert.assertThat(
            writer,
            XhtmlMatchers.hasXPath("/page/injectable/name")
        );
    }

    /**
     * XslResolver injects xml-stylesheet processing instruction.
     * @throws Exception If something goes wrong
     */
    @Test
    public void injectsDefaultStylesheetPi() throws Exception {
        final XslResolver resolver = new XslResolver();
        final Marshaller mrsh = resolver.getContext(XslResolverTest.Page.class);
        final Page page = new XslResolverTest.Page();
        final StringWriter writer = new StringWriter();
        mrsh.marshal(page, writer);
        MatcherAssert.assertThat(
            writer,
            XhtmlMatchers.hasXPath(
                // @checkstyle LineLength (1 line)
                "/processing-instruction('xml-stylesheet')[contains(.,\"href='/xsl/Page.xsl'\")]"
            )
        );
        MatcherAssert.assertThat(
            writer,
            XhtmlMatchers.hasXPath(
                // @checkstyle LineLength (1 line)
                "/processing-instruction('xml-stylesheet')[contains(.,\"type='text/xsl'\")]"
            )
        );
    }

    /**
     * XslResolver can inject absolute URLs.
     * @throws Exception If something goes wrong
     */
    @Test
    public void injectsAbsolutePath() throws Exception {
        final XslResolver resolver = new XslResolver();
        final ServletContext context = Mockito.mock(ServletContext.class);
        resolver.setServletContext(context);
        final HttpServletRequest request = new HttpServletRequestMocker()
            .mock();
        Mockito.doReturn("http").when(request).getScheme();
        Mockito.doReturn("localhost").when(request).getServerName();
        Mockito.doReturn("/sample").when(request).getContextPath();
        final int port = 8080;
        Mockito.doReturn(port).when(request).getServerPort();
        resolver.setHttpServletRequest(request);
        final Marshaller mrsh = resolver.getContext(XslResolverTest.Page.class);
        final Page page = new XslResolverTest.Page();
        final StringWriter writer = new StringWriter();
        mrsh.marshal(page, writer);
        MatcherAssert.assertThat(
            writer,
            XhtmlMatchers.hasXPath(
                // @checkstyle LineLength (1 line)
                "/processing-instruction('xml-stylesheet')[contains(.,\"href='http://localhost:8080/sample/xsl/Page.xsl'\")]"
            )
        );
    }

    /**
     * XslResolver understands stylesheet annotation.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testStylesheetAnnotation() throws Exception {
        final XslResolver resolver = new XslResolver();
        final Marshaller mrsh = resolver.getContext(XslResolverTest.Bar.class);
        final Bar bar = new XslResolverTest.Bar();
        final StringWriter writer = new StringWriter();
        mrsh.marshal(bar, writer);
        MatcherAssert.assertThat(
            writer,
            XhtmlMatchers.hasXPath(
                // @checkstyle LineLength (1 line)
                "/processing-instruction('xml-stylesheet')[contains(.,\"href='test'\")]"
            )
        );
    }

    /**
     * XslResolver checks passed servlet context.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setServletContext() {
        final XslResolver resolver = new XslResolver();
        resolver.setServletContext(null);
    }

    /**
     * Sample page for testing.
     */
    @XmlRootElement(name = "page")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Page {
        /**
         * Injected object.
         */
        private transient Object injected;
        /**
         * Inject an object.
         * @param obj The object to inject
         */
        public void inject(final Object obj) {
            this.injected = obj;
        }
        /**
         * Simple name.
         * @return The name
         */
        @XmlElement
        public String getName() {
            return "some name";
        }
        /**
         * Injected object.
         * @return The object
         */
        @XmlAnyElement(lax = true)
        public Object getInjected() {
            return this.injected;
        }
    }

    /**
     * Injectable element.
     */
    @XmlRootElement(name = "injectable")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Injectable {
        /**
         * Simple name.
         * @return The name
         */
        @XmlElement
        public String getName() {
            return "some foo name";
        }
    }

    /**
     * Just a dummy object.
     */
    @XmlRootElement(name = "page")
    @XmlAccessorType(XmlAccessType.NONE)
    @Stylesheet("test")
    public static final class Bar {
        /**
         * Simple name.
         * @return The name
         */
        @XmlElement
        public String getName() {
            return "another name";
        }
    }

}
