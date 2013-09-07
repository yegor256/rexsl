/**
 * Copyright (c) 2011-2013, ReXSL.com
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
package com.rexsl.w3c;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import com.rexsl.test.XmlDocument;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * Abstract implementation of (X)HTML validator.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
class BaseValidator {

    /**
     * Boundary for HTTP POST form data (just some random data).
     */
    protected static final String BOUNDARY = "vV9olNqRj00PC4OIlM7";

    /**
     * Send request and return response.
     * @param uri The URI to fetch
     * @param entity The entity to POST
     * @return The response
     */
    protected final TestResponse send(final URI uri, final String entity) {
        final int len = entity.getBytes().length;
        return RestTester.start(uri)
            .header(HttpHeaders.USER_AGENT, "ReXSL-W3C")
            .header(HttpHeaders.ACCEPT, "application/soap+xml")
            .header(HttpHeaders.CONTENT_LENGTH, len)
            .header(
                HttpHeaders.CONTENT_TYPE,
                Logger.format(
                    "%s; boundary=%s",
                    MediaType.MULTIPART_FORM_DATA,
                    BaseValidator.BOUNDARY
                )
            )
            .post(
                String.format(
                    "validating %d bytes through W3C validator",
                    len
                ),
                entity
            );
    }

    /**
     * Convert HTML to HTTP FORM entity.
     * @param name Name of HTTP form field
     * @param content The content of it
     * @param type Media type of it
     * @return The HTTP post body
     */
    protected String entity(final String name, final String content,
        final String type) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            final MultipartEntity entity = new MultipartEntity(
                HttpMultipartMode.STRICT,
                BaseValidator.BOUNDARY,
                Charset.forName(CharEncoding.UTF_8)
            );
            entity.addPart(
                name,
                new InputStreamBody(
                    IOUtils.toInputStream(content, CharEncoding.UTF_8),
                    type, "file"
                )
            );
            entity.addPart("output", new StringBody("soap12"));
            entity.writeTo(stream);
            return stream.toString(CharEncoding.UTF_8);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Build response from XML.
     * @param soap The response
     * @return The validation response just built
     */
    protected final DefaultValidationResponse build(final XmlDocument soap) {
        final DefaultValidationResponse resp = new DefaultValidationResponse(
            "true".equals(this.textOf(soap.xpath("//m:validity/text()"))),
            UriBuilder.fromUri(
                this.textOf(soap.xpath("//m:checkedby/text()"))
            ).build(),
            this.textOf(soap.xpath("//m:doctype/text()")),
            this.charset(this.textOf(soap.xpath("//m:charset/text()")))
        );
        for (XmlDocument node : soap.nodes("//m:error")) {
            resp.addError(this.defect(node));
        }
        for (XmlDocument node : soap.nodes("//m:warning")) {
            resp.addWarning(this.defect(node));
        }
        return resp;
    }

    /**
     * Build response from error that just happened.
     * @param error The exception
     * @return The validation response just built
     */
    protected final DefaultValidationResponse failure(final Throwable error) {
        final DefaultValidationResponse resp = new DefaultValidationResponse(
            false,
            URI.create("http://localhost/failure"),
            "unknown-doctype",
            Charset.defaultCharset()
        );
        String message = error.getMessage();
        if (message == null) {
            message = "";
        }
        resp.addError(
            new Defect(
                0,
                0,
                "",
                Logger.format("%[exception]s", error),
                "",
                message
            )
        );
        return resp;
    }

    /**
     * Build a success response.
     * @param type Media type of resource just processed
     * @return The validation response just built
     */
    protected final DefaultValidationResponse success(final String type) {
        final DefaultValidationResponse resp = new DefaultValidationResponse(
            true,
            URI.create("http://localhost/success"),
            type,
            Charset.defaultCharset()
        );
        return resp;
    }

    /**
     * Convert SOAP node to defect.
     * @param node The node
     * @return The defect
     */
    private Defect defect(final XmlDocument node) {
        return new Defect(
            this.intOf(node.xpath("m:line/text()")),
            this.intOf(node.xpath("m:col/text()")),
            this.textOf(node.xpath("m:source/text()")),
            this.textOf(node.xpath("m:explanation/text()")),
            this.textOf(node.xpath("m:messageid/text()")),
            this.textOf(node.xpath("m:message/text()"))
        );
    }

    /**
     * Get text from list of strings, returned by
     * {@link XmlDocument#xpath(String)}.
     *
     * <p>This method is required to simplify manipulations with XPath returned
     * list of strings (returned by {@link TestResponse#xpath(String)} above).
     * The list of strings normally (!) contains one element or no elements. If
     * there are no elements it means that the XPath is not found in the
     * document. In this case we should return an empty string. If any elements
     * are found - we're interested only in the first one. All others are
     * ignored, because simply should not exist (if our XPath query is correct).
     *
     * @param lines The lines to work with
     * @return The value
     * @see #intOf(List)
     */
    private String textOf(final List<String> lines) {
        String text;
        if (lines.isEmpty()) {
            text = "";
        } else {
            text = lines.get(0);
        }
        return text;
    }

    /**
     * Get text from list of strings.
     *
     * <p>See explanation of {@link #textOf(List)}.
     *
     * @param lines The lines to work with
     * @return The value
     * @see #textOf(List)
     */
    private int intOf(final List<String> lines) {
        int value;
        if (lines.isEmpty()) {
            value = 0;
        } else {
            value = Integer.parseInt(lines.get(0));
        }
        return value;
    }

    /**
     * Convert text to charset.
     * @param text Text representation of charset
     * @return The charset
     */
    private Charset charset(final String text) {
        Charset charset;
        if (text.isEmpty()) {
            charset = Charset.defaultCharset();
        } else {
            charset = Charset.forName(text);
        }
        return charset;
    }

}
