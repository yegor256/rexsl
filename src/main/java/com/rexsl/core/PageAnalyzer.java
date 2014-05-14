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
package com.rexsl.core;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Makes a decision whether page should be transformed to HTML or returned
 * to the user as untouched XML (or anything else).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = { "page", "request" })
@Loggable(Loggable.DEBUG)
final class PageAnalyzer {

    /**
     * The page.
     */
    private final transient String page;

    /**
     * The request.
     */
    private final transient HttpServletRequest request;

    /**
     * Public ctor.
     * @param text The text of response
     * @param rqst The request
     */
    PageAnalyzer(final String text, final HttpServletRequest rqst) {
        this.page = text;
        this.request = rqst;
    }

    /**
     * Do we need filtering?
     * @return Do we need to transform to XHTML?
     */
    public boolean needsTransformation() {
        final UserAgent agent = new UserAgent(
            this.request.getHeader(HttpHeaders.USER_AGENT)
        );
        final TypesMatcher accept = new TypesMatcher(
            this.request.getHeader(HttpHeaders.ACCEPT)
        );
        // @checkstyle BooleanExpressionComplexity (1 line)
        final boolean needs = this.notEmpty() && this.containsXml()
            && !this.xmlDemanded(accept)
            && !this.xslAccepted(agent, accept);
        Logger.debug(
            this,
            // @checkstyle LineLength (1 line)
            "#needsTransformation('%s': '%[text]s'): User-Agent='%s', Accept='%s', %B",
            this.request.getRequestURI(), this.page,
            agent, accept, needs
        );
        return needs;
    }

    /**
     * Page require transformation, since it's not an empty page?
     * @return TRUE if the page requires transformation
     */
    private boolean notEmpty() {
        return !this.page.isEmpty();
    }

    /**
     * Page require transformation, since it contains XML and XSL stylesheet?
     * @return TRUE if the page requires transformation
     */
    private boolean containsXml() {
        return this.page.startsWith("<?xml ")
            && this.page.contains("<?xml-stylesheet ");
    }

    /**
     * Page requires transformation, since XML media type is not required?
     * @param types Media types
     * @return TRUE if the page requires transformation
     */
    private boolean xmlDemanded(final TypesMatcher types) {
        return types.explicit(MediaType.APPLICATION_XML)
            || types.explicit(MediaType.TEXT_XML);
    }

    /**
     * Page requires transformation, since XSL is not welcome by the client?
     * @param agent User agent of the client
     * @param types Media types
     * @return TRUE if the page requires transformation
     */
    private boolean xslAccepted(final UserAgent agent,
        final TypesMatcher types) {
        return agent.isXsltCapable()
            && (types.accepts(MediaType.APPLICATION_XML)
            || types.accepts(MediaType.TEXT_XML));
    }

}
