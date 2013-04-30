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

import com.sun.jersey.api.client.ClientResponse;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link BufferedJerseyFetcher}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BufferedJerseyFetcherTest {

    /**
     * BufferedJerseyFetcher can read XML in ISO-8859-1 encoding.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canReadNonUnicodeXmlDocuments() throws Exception {
        final ClientResponse response = Mockito.mock(ClientResponse.class);
        Mockito.doReturn(true).when(response).hasEntity();
        Mockito.doReturn(
            IOUtils.toInputStream(
                "<?xml version='1.0' encoding='ISO-8859-1'><root>\u009F</root>",
                Charset.forName("ISO-8859-1")
            )
        ).when(response).getEntityInputStream();
        final JerseyFetcher origin = Mockito.mock(JerseyFetcher.class);
        Mockito.doReturn(response).when(origin).fetch();
        final BufferedJerseyFetcher fetcher = new BufferedJerseyFetcher(origin);
        MatcherAssert.assertThat(
            fetcher.body(),
            XhtmlMatchers.hasXPath("/root")
        );
    }

}
