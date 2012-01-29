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
package com.rexsl.w3c;

import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

/**
 * Implementation of (X)HTML validator.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://validator.w3.org/docs/api.html">W3C API</a>
 */
final class DefaultHtmlValidator extends AbstractValidator
    implements HtmlValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResponse validate(final String html) {
        final String field = "uploaded_file";
        final URI uri = UriBuilder.fromUri("http://validator.w3.org/check")
            .queryParam(field, "")
            .queryParam("output", "soap12")
            .build();
        final TestResponse soap = this
            .send(uri, this.entity(field, "p.html", html, MediaType.TEXT_HTML))
            .registerNs("env", "http://www.w3.org/2003/05/soap-envelope")
            .registerNs("m", "http://www.w3.org/2005/10/markup-validator")
            .assertXPath("/env:Envelope/env:Body/m:markupvalidationresponse");
        final DefaultValidationResponse resp = new DefaultValidationResponse(
            soap.xpath("//m:validity").get(0).equals("true"),
            UriBuilder.fromUri(soap.xpath("//m:checkedby").get(0)).build(),
            soap.xpath("//m:doctype").get(0),
            soap.xpath("//m:charset").get(0)
        );
        for (TestResponse node : soap.nodes("//m:errorlist/m:error")) {
            resp.addError(
                new Defect(
                    Integer.valueOf(node.xpath("/m:line").get(0)),
                    Integer.valueOf(node.xpath("/m:col").get(0)),
                    node.xpath("/m:source").get(0),
                    node.xpath("/m:explanation").get(0),
                    node.xpath("/m:messageid").get(0),
                    node.xpath("/m:message").get(0)
                )
            );
        }
        for (TestResponse node : soap.nodes("//m:warninglist/m:warning")) {
            resp.addWarning(
                new Defect(
                    Integer.valueOf(node.xpath("/m:line").get(0)),
                    Integer.valueOf(node.xpath("/m:col").get(0)),
                    node.xpath("/m:source").get(0),
                    node.xpath("/m:explanation").get(0),
                    node.xpath("/m:messageid").get(0),
                    node.xpath("/m:message").get(0)
                )
            );
        }
        return resp;
    }

}
