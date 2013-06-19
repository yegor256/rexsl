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

import com.jcabi.log.Logger;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import java.net.ServerSocket;
import java.net.URI;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Mocker of Java Servlet container.
 *
 * <p>A convenient tool to test your application classes against a web
 * service. For example:
 *
 * <pre> URI home = new ContainerMocker()
 *   .expectMethod("GET")
 *   .expectHeader("Accept", Matchers.startsWith("text/"))
 *   .returnBody("&lt;done/&gt;")
 *   .returnStatus(200)
 *   .mock()
 *   .home();
 * // sometime later
 * RestTester.start(home)
 *   .header("Accept", "text/xml")
 *   .get("Load sample page")
 *   .assertStatus(200);</pre>
 *
 * <p>Keep in mind that container automatically reserves a new free TCP port
 * and works until JVM is shut down. The only way to stop it is to call
 * {@link #stop()}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see RestTester
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ContainerMocker {

    /**
     * Grizzly adapter.
     */
    private final transient GrizzlyAdapterMocker adapter =
        new GrizzlyAdapterMocker();

    /**
     * Grizzly container.
     */
    private transient GrizzlyWebServer gws;

    /**
     * Port where it works.
     */
    private transient int port;

    /**
     * Expect body.
     * @param matcher Matcher for body
     * @return This object
     * @todo #150 It is impossible to match parameter and body at the same time.
     */
    public ContainerMocker expectBody(final Matcher<String> matcher) {
        if (this.adapter.hasParamMatcher()) {
            throw new IllegalStateException(
                "Cannot add body matcher. Adapter already has parameter matcher"
            );
        }
        this.adapter.setBodyMatcher(matcher);
        return this;
    }

    /**
     * Expect request URI to match this matcher.
     * @param matcher Matcher for request path
     * @return This object
     */
    public ContainerMocker expectRequestUri(final Matcher<String> matcher) {
        this.adapter.setRequestUriMatcher(matcher);
        return this;
    }

    /**
     * Expect this HTTP method (take a look at {@link RestTester} constants).
     * @param matcher Name of the method to expect.
     * @return This object
     * @see RestTester
     */
    public ContainerMocker expectMethod(final Matcher<String> matcher) {
        this.adapter.setMethodMatcher(matcher);
        return this;
    }

    /**
     * Expect this param with this matcher.
     * @param name Name of the param
     * @param matcher Matcher for its content
     * @return This object
     * @todo #150 It is impossible to match parameter and body at the same
     *  time.
     */
    public ContainerMocker expectParam(final String name,
        final Matcher<String> matcher) {
        if (this.adapter.hasBodyMatcher()) {
            throw new IllegalStateException(
                "Cannot add parameter matcher. Adapter already has body matcher"
            );
        }
        this.adapter.addParamMatcher(name, matcher);
        return this;
    }

    /**
     * Expect this header with this matcher.
     * @param name Name of the header.
     * @param matcher Matcher for body
     * @return This object
     */
    public ContainerMocker expectHeader(final String name,
        final Matcher<String> matcher) {
        this.adapter.addHeaderMatcher(name, matcher);
        return this;
    }

    /**
     * Expect this header with exactly this value.
     * @param name Name of the header.
     * @param value The value to expect
     * @return This object
     */
    public ContainerMocker expectHeader(final String name,
        final String value) {
        this.adapter.addHeaderMatcher(name, Matchers.equalTo(value));
        return this;
    }

    /**
     * Return this body.
     * @param body The body
     * @return This object
     */
    public ContainerMocker returnBody(final String body) {
        try {
            return this.returnBody(body.getBytes(CharEncoding.UTF_8));
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Return this body.
     * @param body The body
     * @return This object
     */
    public ContainerMocker returnBody(final byte[] body) {
        return this.returnBody(
            new Callable<byte[]>() {
                @Override
                public byte[] call() {
                    final byte[] bytes = new byte[body.length];
                    System.arraycopy(body, 0, bytes, 0, body.length);
                    return bytes;
                }
            }
        );
    }

    /**
     * Return this body.
     * @param body The body to return
     * @return This object
     */
    public ContainerMocker returnBody(final Callable<byte[]> body) {
        this.adapter.setBody(body);
        return this;
    }

    /**
     * Return this header.
     * @param name Header's name
     * @param value Header's value
     * @return This object
     */
    public ContainerMocker returnHeader(final String name, final String value) {
        this.adapter.addHeader(name, value);
        return this;
    }

    /**
     * Return this status.
     * @param code The code
     * @return This object
     */
    public ContainerMocker returnStatus(final int code) {
        this.adapter.setStatus(code);
        return this;
    }

    /**
     * Mock it, and return this object.
     * @return This object
     */
    public ContainerMocker mock() {
        return this.mock(reservePort());
    }
    
    /**
     * Mock it, and return this object.
     * @param port The port where it works
     * @return This object
     * @since 0.5
     */
    public ContainerMocker mock(final int port) {
        this.port = port;
        this.gws = new GrizzlyWebServer(this.port);
        this.gws.addGrizzlyAdapter(this.adapter, new String[] {"/"});
        this.start();
        return this;
    }

    /**
     * Start container (to use in &#64;Before-annotated unit test method).
     */
    public void start() {
        try {
            this.gws.start();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.debug(
            this,
            "#start(): Grizzly started at port #%s",
            this.port
        );
    }

    /**
     * Stop container (to use in &#64;After-annotated unit test method).
     */
    public void stop() {
        this.gws.stop();
        Logger.debug(
            this,
            "#stop(): Grizzly stopped at port #%s",
            this.port
        );
    }

    /**
     * Get its home.
     * @return URI of the started container
     */
    public URI home() {
        try {
            return new URI(Logger.format("http://localhost:%d/", this.port));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Reserve port.
     * @return Reserved TCP port
     */
    private int reservePort() {
        int reserved;
        try {
            final ServerSocket socket = new ServerSocket(0);
            try {
                reserved = socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return reserved;
    }

}
