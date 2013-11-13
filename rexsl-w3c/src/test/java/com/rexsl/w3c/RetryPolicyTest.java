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

import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link RetryPolicy}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RetryPolicyTest {

    /**
     * RetryPolicy can detect defective response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void detectsDefectiveResponse() throws Exception {
        final AssertionPolicy policy = new RetryPolicy("/a");
        final TestResponse response = new TestResponseMocker()
            .withBody("<!DOCTYPE a PUBLIC \"foo\"\"ff\"><a/>")
            .withStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .mock();
        try {
            policy.assertThat(response);
            Assert.fail();
        } catch (AssertionError ex) {
            MatcherAssert.assertThat("", policy.isRetryNeeded(1));
        }
    }

    /**
     * RetryPolicy can detect defective response, with runtime exception.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void detectsDefectiveResponseEvenWithException() throws Exception {
        final AssertionPolicy policy = new RetryPolicy("/foo");
        final TestResponse response = Mockito.mock(TestResponse.class);
        Mockito.doReturn(HttpURLConnection.HTTP_OK).when(response).getStatus();
        Mockito.doThrow(new AssertionError())
            .when(response).nodes(Mockito.anyString());
        try {
            policy.assertThat(response);
            Assert.fail();
        } catch (AssertionError ex) {
            MatcherAssert.assertThat("", policy.isRetryNeeded(1));
        }
    }

}
