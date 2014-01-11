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

import java.io.IOException;
import java.net.URI;

/**
 * Mock version of Java Servlet container.
 *
 * <p>A convenient tool to test your application classes against a web
 * service. For example:
 *
 * <pre> MkContainer container = new MkGrizzlyContainer()
 *   .next(new MkAnswer.Simple(200, "works fine!"))
 *   .start();
 * new JdkRequest(container.home())
 *   .header("Accept", "text/xml")
 *   .fetch().as(RestResponse.class)
 *   .assertStatus(200)
 *   .assertBody(Matchers.equalTo("works fine!"));
 * MatcherAssert.assertThat(
 *   container.take().method(),
 *   Matchers.equalTo("GET")
 * );
 * container.stop();</pre>
 *
 * <p>Keep in mind that container automatically reserves a new free TCP port
 * and works until JVM is shut down. The only way to stop it is to call
 * {@link #stop()}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
public interface MkContainer {

    /**
     * Give this answer on the next request.
     * @param answer Next answer to give
     * @return This object
     */
    MkContainer next(MkAnswer answer);

    /**
     * Get the oldest request received.
     * @return Request received
     */
    MkQuery take();

    /**
     * Start it on the first available TCP port.
     * @return This object
     * @throws IOException If fails
     */
    MkContainer start() throws IOException;

    /**
     * Start it on a provided port.
     * @param prt The port where it should start listening
     * @return This object
     * @throws IOException If fails
     */
    MkContainer start(int prt) throws IOException;

    /**
     * Stop container.
     */
    void stop();

    /**
     * Get its home.
     * @return URI of the started container
     */
    URI home();

}
