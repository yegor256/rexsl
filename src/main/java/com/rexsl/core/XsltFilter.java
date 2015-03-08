/**
 * Copyright (c) 2011-2015, ReXSL.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.ServletMfs;
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
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * Converts XML to XHTML, if necessary.
 *
 * <p>You don't need to instantiate this class directly. It is instantiated
 * by servlet container according to configuration from {@code web.xml}.
 * Should be used in {@code web.xml} (together with {@link RestfulServlet})
 * like this:
 *
 * <pre> &lt;filter>
 *  &lt;filter-name>XsltFilter&lt;/filter-name>
 *  &lt;filter-class>com.rexsl.core.XsltFilter&lt;/filter-class>
 * &lt;/filter>
 * &lt;filter-mapping>
 *  &lt;filter-name>XsltFilter&lt;/filter-name>
 *  &lt;servlet-name>RestfulServlet&lt;/servlet-name>
 *  &lt;dispatcher>REQUEST&lt;/dispatcher>
 *  &lt;dispatcher>ERROR&lt;/dispatcher>
 * &lt;/filter-mapping></pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.2
 * @checkstyle ClassDataAbstractionCoupling (300 lines)
 */
@ToString
@EqualsAndHashCode(of = "tfactory")
@Loggable(Loggable.DEBUG)
public final class XsltFilter implements Filter {

    /**
     * XSLT factory.
     */
    private transient TransformerFactory tfactory;

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public void init(@NotNull final FilterConfig config)
        throws ServletException {
        this.tfactory = TransformerFactory.newInstance();
        if (this.tfactory == null) {
            throw new ServletException(
                "failed to make a new instance of TransformerFactory"
            );
        }
        if (!"net.sf.saxon.TransformerFactoryImpl"
            .equals(this.tfactory.getClass().getName())) {
            Logger.warn(
                this,
                // @checkstyle LineLength (1 line)
                "Be aware that Saxon implementation of %s is replaced with %s, which may lead to unexpected problems",
                this.tfactory, TransformerFactory.class.getName()
            );
        }
        final ServletContext context = config.getServletContext();
        this.tfactory.setURIResolver(new ContextResourceResolver(context));
        try {
            Manifests.DEFAULT.append(new ServletMfs(context));
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     * @checkstyle ThrowsCount (7 lines)
     * @checkstyle RedundantThrows (7 lines)
     */
    @Override
    @Loggable(value = Loggable.DEBUG, ignore = WebApplicationException.class)
    public void doFilter(final ServletRequest req, final ServletResponse res,
        final FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest
            && res instanceof HttpServletResponse) {
            this.filter(
                HttpServletRequest.class.cast(req),
                HttpServletResponse.class.cast(res),
                chain
            );
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    /**
     * Make filtering (when necessary) or pass through if it's not an XML
     * document.
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
        if (!response.isCommitted()) {
            byte[] data = wrapper.getByteArray();
            String page = new String(data, CharEncoding.UTF_8);
            final PageAnalyzer analyzer = new PageAnalyzer(page, request);
            if (analyzer.needsTransformation()) {
                page = this.transform(page);
                data = page.getBytes(CharEncoding.UTF_8);
                response.setContentType(MediaType.TEXT_HTML);
                response.setCharacterEncoding(CharEncoding.UTF_8);
                response.setContentLength(data.length);
            }
            response.getOutputStream().write(data);
        }
    }

    /**
     * Transform XML into HTML.
     * @param xml XML page to be transformed.
     * @return Resulting HTML page.
     * @throws ServletException If some problem inside
     * @checkstyle RedundantThrows (2 lines)
     */
    private String transform(final String xml) throws ServletException {
        final StringWriter writer = new StringWriter();
        try {
            this.transformer(this.stylesheet(xml)).transform(
                this.source(xml),
                new StreamResult(writer)
            );
        } catch (final TransformerException ex) {
            throw new ServletException(
                Logger.format(
                    "Failed to transform XML to XHTML: '%s'",
                    xml
                ),
                ex
            );
        }
        return writer.toString();
    }

    /**
     * Transform XML into DOM source.
     * @param xml XML page to be transformed.
     * @return Source
     */
    private Source source(final String xml) {
        return new StreamSource(new StringReader(xml));
    }

    /**
     * Retrieve a stylesheet from this XML (throws an exception if
     * no stylesheet is attached).
     * @param xml The XML
     * @return Stylesheet found
     * @throws ServletException If fails
     * @checkstyle RedundantThrows (3 lines)
     */
    private Source stylesheet(final String xml) throws ServletException {
        final Source stylesheet;
        try {
            stylesheet = this.tfactory.getAssociatedStylesheet(
                this.source(xml), null, null, null
            );
        } catch (final TransformerConfigurationException ex) {
            throw new ServletException(
                Logger.format(
                    "Failed to configure XSL transformer: '%[text]s'",
                    xml
                ),
                ex
            );
        }
        if (stylesheet == null) {
            throw new ServletException(
                Logger.format(
                    "No associated stylesheet found at: '%[text]s'",
                    xml
                )
            );
        }
        Logger.debug(
            this,
            "#transform(%d chars): found '%s' associated stylesheet by %s",
            xml.length(),
            stylesheet.getSystemId(),
            this.tfactory.getClass().getName()
        );
        return stylesheet;
    }

    /**
     * Make a transformer from this stylesheet.
     * @param stylesheet The stylesheet
     * @return Transformer
     * @throws ServletException If fails
     * @checkstyle RedundantThrows (3 lines)
     */
    private Transformer transformer(final Source stylesheet)
        throws ServletException {
        final Transformer tran;
        try {
            tran = this.tfactory.newTransformer(stylesheet);
        } catch (final TransformerConfigurationException ex) {
            throw new ServletException(
                Logger.format(
                    "Failed to create an XSL transformer for '%s'",
                    stylesheet.getSystemId()
                ),
                ex
            );
        }
        if (tran == null) {
            throw new ServletException(
                Logger.format(
                    "%[type]s failed to create new XSL transformer for '%s'",
                    this.tfactory,
                    stylesheet.getSystemId()
                )
            );
        }
        return tran;
    }

}
