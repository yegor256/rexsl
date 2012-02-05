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
package com.rexsl.test.assertions;

import com.rexsl.test.AssertionPolicy;
import com.rexsl.test.ClientResponseDecor;
import com.rexsl.test.TestResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.ymock.util.Logger;

/**
 * Always fail.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public class Failure implements AssertionPolicy {

    /**
     * Client response.
     */
    private final transient ClientResponse response;

    /**
     * The reason of failure.
     */
    private final transient String reason;

    /**
     * Public ctor.
     * @param resp The response
     * @param txt The reason of failure
     */
    public Failure(final ClientResponse resp, final String txt) {
        this.response = resp;
        this.reason = txt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertThat(final TestResponse rsp) {
        throw new AssertionError(
            Logger.format(
                "%s:\n%s",
                this.reason,
                new ClientResponseDecor(this.response, rsp.getBody())
            )
        );
    }

}
