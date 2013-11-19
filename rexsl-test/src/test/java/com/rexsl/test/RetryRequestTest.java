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
package com.rexsl.test;

import com.jcabi.aspects.Tv;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link RetryRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RetryRequestTest {

    /**
     * BaseRequest can retry on IOException.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retriesOnIoException() throws Exception {
        final Request origin = Mockito.mock(Request.class);
        Mockito.doThrow(new IOException("")).when(origin).fetch();
        try {
            new RetryRequest(origin).fetch();
            Assert.fail("exception expected here");
        } catch (IOException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.equalTo("")
            );
        }
        Mockito.verify(origin, Mockito.atLeast(Tv.THREE)).fetch();
    }

    /**
     * BaseRequest can retry after changes of body.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retriesOnIoExceptionWithChangedBody() throws Exception {
        final RequestBody body = Mockito.mock(RequestBody.class);
        final Request origin = Mockito.mock(Request.class);
        Mockito.doReturn(body).when(body).set(Mockito.anyString());
        Mockito.doReturn(origin).when(body).back();
        Mockito.doReturn(body).when(origin).body();
        Mockito.doThrow(new IOException("")).when(origin).fetch();
        try {
            new RetryRequest(origin).body().set("").back().fetch();
            Assert.fail("exception expected");
        } catch (IOException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.equalTo("")
            );
        }
        Mockito.verify(origin, Mockito.atLeast(Tv.THREE)).fetch();
    }

    /**
     * BaseRequest can retry after changes of method.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retriesOnIoExceptionWithChangedMethod() throws Exception {
        final Request origin = Mockito.mock(Request.class);
        Mockito.doReturn(origin).when(origin).method(Mockito.anyString());
        Mockito.doThrow(new IOException("")).when(origin).fetch();
        try {
            new RetryRequest(origin).method(Request.POST).fetch();
            Assert.fail("exception expected at this point");
        } catch (IOException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.equalTo("")
            );
        }
        Mockito.verify(origin, Mockito.atLeast(Tv.THREE)).fetch();
    }

}
