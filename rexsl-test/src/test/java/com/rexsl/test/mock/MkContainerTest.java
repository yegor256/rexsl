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
package com.rexsl.test.mock;

import com.rexsl.test.Request;
import com.rexsl.test.request.JdkRequest;
import com.rexsl.test.response.RestResponse;
import com.rexsl.test.wire.VerboseWire;
import java.net.HttpURLConnection;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MkContainer}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MkContainerTest {

    /**
     * MkContainer can return required answers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void worksAsServletContainer() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "works fine!"))
            .start();
        new JdkRequest(container.home())
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.startsWith("works"));
        container.stop();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.method(),
            Matchers.equalTo(Request.GET)
        );
    }

    /**
     * MkContainer can understand duplicate headers.
     * @throws Exception If something goes wrong inside
     * @todo #1 Grizzly container doesn't understand same-name
     *  headers, or we don't fetch them correctly from GrizzlyRequest
     */
    @Test
    @org.junit.Ignore
    public void understandsDuplicateHeaders() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .start();
        final String header = "X-Something";
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .header(header, MediaType.TEXT_HTML)
            .header(header, MediaType.TEXT_XML)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(header),
            Matchers.hasSize(2)
        );
    }

}
