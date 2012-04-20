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
package com.rexsl.w3c;

import com.rexsl.test.TestResponse;
import com.rexsl.test.XmlDocument;
import java.net.URI;
import javax.ws.rs.core.MediaType;

/**
 * MobileOK validator.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see <a href="http://validator.w3.org/docs/users.html">mobileOK API</a>
 */
final class MobileValidator extends BaseValidator implements Validator {

    /**
     * The URI to use in W3C.
     */
    private final transient URI uri;

    /**
     * Public ctor with default entry point.
     */
    public MobileValidator() {
        this(URI.create("http://validator.w3.org/mobile/check"));
    }

    /**
     * Public ctor.
     * @param entry Entry point to use
     */
    public MobileValidator(final URI entry) {
        super();
        this.uri = entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({
        "PMD.AvoidCatchingThrowable",
        "PMD.AvoidInstantiatingObjectsInLoops"
    })
    public ValidationResponse validate(final String html) {
        DefaultValidationResponse response;
        try {
            final TestResponse soap = this
                .send(
                    this.uri,
                    this.entity("uploaded_file", html, MediaType.TEXT_HTML)
            )
                // @checkstyle LineLength (1 line)
                .registerNs("rsp", "http://www.w3.org/2009/10/unicorn/observationresponse")
                .assertThat(new RetryPolicy("/rsp:observationresponse"));
            response = new DefaultValidationResponse(
                soap.nodes("/*/rsp:message").isEmpty(),
                this.uri,
                "HTML",
                "UTF-8"
            );
            for (XmlDocument node : soap.nodes("//rsp:message")) {
                response.addError(
                    new Defect(
                        0,
                        0,
                        this.textOf(node.xpath("rsp:title/text()")),
                        this.textOf(node.xpath("rsp:description/text()")),
                        "",
                        ""
                    )
                );
            }
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (Throwable ex) {
            response = this.failure(ex);
        }
        return response;
    }

}
