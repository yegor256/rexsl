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
package com.rexsl.core;

import com.ymock.util.Logger;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Converts XML to XHTML, if necessary.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class XsltFilter implements Filter {

    /**
     * Character encoding of the page.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Context for the filter.
     */
    private ServletContext context;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig config) {
        this.context = config.getServletContext();
        Logger.debug(
            this,
            "#init(%s): XSLT filter initialized",
            config.getClass().getName()
        );
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (5 lines)
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res,
        final FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest
            && res instanceof HttpServletResponse) {
            this.filter(
                (HttpServletRequest) req,
                (HttpServletResponse) res,
                chain
            );
        } else {
            chain.doFilter(req, res);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        this.context = null;
        Logger.debug(
            this,
            "#destroy(): XSLT filter destroyed"
        );
    }

    /**
     * Make filtering.
     * @param request The request
     * @param response The response
     * @param chain Filter chain
     * @throws IOException If something goes wrong
     * @throws ServletException If something goes wrong
     * @checkstyle ThrowsCount (6 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    private void filter(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        final ByteArrayResponseWrapper wrapper =
            new ByteArrayResponseWrapper(response);
        chain.doFilter(request, wrapper);
        if (response.isCommitted()) {
            // we can't change response that is already finished
            return;
        }
        final String agent = request.getHeader("User-Agent");
        final String accept = request.getHeader("Accept");
        String page = wrapper.getByteStream().toString(this.ENCODING);
        // let's check whether we should transform or not
        // @checkstyle BooleanExpressionComplexity (1 line)
        final boolean dontTouch =
            // page is empty
            page.isEmpty()
            // it doesn't look like XML
            || !page.startsWith("<?xml ")
            // it doesn't refer to any stylesheet
            || !page.contains("<?xml-stylesheet ")
            // it's a pure XML client, requesting XML format
            || this.isXmlExplicitlyRequested(accept)
            // the browser supports XSTL 2.0
            || (this.isXsltCapable(agent) && this.acceptsXml(accept));
        if (dontTouch) {
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#filter('%s': %d chars): User-Agent='%s', Accept='%s', no need to transform",
                request.getRequestURI(),
                page.length(),
                agent,
                accept
            );
        } else {
            response.setContentType("text/html");
            page = this.transform(page);
        }
        response.getOutputStream().write(page.getBytes(this.ENCODING));
    }

    /**
     * Check if the XSLT transformation is required on the server side.
     * @param agent User agent string from the request.
     * @return If the transformation is needed.
     * @todo #7 The implementation is very preliminary and should be refined.
     *  Not all Chrome or Safari versions support XSLT 2.0. We should properly
     *  parse the "Agent" header and understand versions.
     */
    private Boolean isXsltCapable(final String agent) {
        final Boolean cap = (agent != null)
            && agent.matches(".*(Chrome|Safari).*");
        Logger.debug(this, "#isXsltCapable('%s'): %b", agent, cap);
        return cap;
    }

    /**
     * Check if the application/xml MIME type is the only one there.
     * @param header Accept header string from the request.
     * @return If the application/XML MIME type is the one
     */
    private Boolean isXmlExplicitlyRequested(final String header) {
        final Boolean requested = (header != null)
            && (MediaType.APPLICATION_XML.equals(header));
        Logger.debug(
            this,
            "#isXmlExplicitlyRequested('%s'): %b",
            header,
            requested
        );
        return requested;
    }

    /**
     * Check if the "application/xml" MIME type is accepted.
     * @param header Accept header string from the request.
     * @return If the application/XML MIME type is present
     * @todo #7 This implemetation is very very preliminary and should
     *  be replaced with something more decent. I don't like the idea
     *  of implementing this parsing functionality here. We should better
     *  use some library: http://stackoverflow.com/questions/7705979
     */
    private Boolean acceptsXml(final String header) {
        final Boolean accepts = (header != null)
            && (header.contains(MediaType.APPLICATION_XML));
        Logger.debug(
            this,
            "#acceptsXml('%s'): %b",
            header,
            accepts
        );
        return accepts;
    }

    /**
     * Transform XML into HTML.
     * @param xml XML page to be transformed.
     * @return Resulting HTML page.
     * @throws ServletException If some problem inside
     * @checkstyle RedundantThrows (2 lines)
     */
    private String transform(final String xml) throws ServletException {
        final long start = System.nanoTime();
        final StringWriter writer = new StringWriter();
        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(
                new ContextResourceResolver(this.context)
            );
            final Source stylesheet = factory.getAssociatedStylesheet(
                new StreamSource(new StringReader(xml)),
                null, null, null
            );
            final Transformer trans = factory.newTransformer(stylesheet);
            trans.setURIResolver(new ContextResourceResolver(this.context));
            trans.transform(
                new StreamSource(new StringReader(xml)),
                new StreamResult(writer)
            );
        } catch (TransformerConfigurationException ex) {
            throw new ServletException(
                "Failed to configure XSL transformer",
                ex
            );
        } catch (TransformerException ex) {
            throw new ServletException(
                "Failed to transform XML document to XHTML",
                ex
            );
        }
        final String output = writer.toString();
        Logger.debug(
            this,
            "#tranform(%d chars): produced %d chars, %.2f sec",
            xml.length(),
            output.length(),
            // @checkstyle MagicNumber (1 line)
            (double) (System.nanoTime() - start) / (1000 * 1000 * 1000)
        );
        return output;
    }

}
