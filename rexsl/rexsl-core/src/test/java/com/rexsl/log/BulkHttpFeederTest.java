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
package com.rexsl.log;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.rexsl.test.ContainerMocker;
import com.rexsl.test.RestTester;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link BulkHttpFeeder}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TestClassWithoutTestCases" })
public final class BulkHttpFeederTest {

    /**
     * BulkHttpFeeder can send messages to HTTP via POST.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsMessagesToCloud() throws Exception {
        final String message = "hi there!";
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo(RestTester.POST))
            .expectBody(Matchers.containsString(message))
            .expectHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.equalTo(MediaType.TEXT_PLAIN)
            )
            .returnBody("posted")
            .mock();
        final BulkHttpFeeder feeder = new BulkHttpFeeder();
        feeder.setUrl(container.home().toString());
        feeder.setPeriod(2L);
        feeder.activateOptions();
        feeder.feed(message);
        feeder.feed(message);
    }

    /**
     * BulkHttpFeeder can handle multiple feeding threads.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void feedsMultipleMessagesToCloud() throws Exception {
        final long period = 1L;
        final int timeout = 200;
        final StringBuffer expectedResult = new StringBuffer();
        final GrizzlyAdapterMocker adapter =
        new GrizzlyAdapterMocker().mock();
        final BulkHttpFeeder feeder = new BulkHttpFeeder();
        feeder.setUrl(adapter.home().toString());
        feeder.setPeriod(period);
        feeder.setUnit(TimeUnit.MILLISECONDS);
        feeder.activateOptions();
        this.runFeedThreads(expectedResult, feeder);
        Thread.sleep(timeout);
        final StringBuffer actualResult = adapter.getBuffer();
        MatcherAssert.assertThat(
            expectedResult.toString(),
            Matchers.equalTo(actualResult.toString())
        );
    }
    /**
     * Runs threads that feed data to cloud.
     * @param expected Buffer to keep track of fed data.
     * @param feeder Feeder to feed data to.
     * @throws InterruptedException If there is some problem inside.
     */
    private void runFeedThreads(final StringBuffer expected,
        final BulkHttpFeeder feeder) throws InterruptedException {
        final String message = "foo";
        final int numThreads = 20;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(numThreads);
        final ExecutorService taskExecutor =
            Executors.newFixedThreadPool(numThreads);
        class FeedRunnable implements Runnable {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                try {
                    start.await();
                    feeder.feed(message);
                    expected.append(message);
                } catch (IOException exc) {
                    Logger.warn(
                        this,
                        "#testThreadSafety(): %[exception]s",
                        exc
                    );
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                } finally {
                    finish.countDown();
                }
            }
        }
        final VerboseRunnable runnable = new VerboseRunnable(
            new FeedRunnable(),
            true
        );
        for (int num = 0; num < numThreads; num = num + 1) {
            taskExecutor.execute(
                runnable
            );
        }
        start.countDown();
        finish.await();
        taskExecutor.shutdown();
    }
    /**
     * Simple Custom {@link GrizzlyAdapter}.
     * It stores the request data into a buffer.
     * @author Flavius Ivasca (ivascaflavius@gmail.com)
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private class GrizzlyAdapterMocker extends GrizzlyAdapter {
        /**
         * Stores concatenated data from requests.
         */
        private final transient StringBuffer buffer = new StringBuffer();
        /**
         * Port where it works.
         */
        private transient int port;
        /**
         * Get option {@code buffer}.
         * @return Buffer that holds concatenated data from requests
         */
        public final StringBuffer getBuffer() {
            return this.buffer;
        }
        /**
         * Get its home.
         * @throws java.net.URISyntaxException If there is some problem inside.
         * @return URI of the started container
         */
        public final URI home()
            throws java.net.URISyntaxException {
            return new URI(
                Logger.format("http://localhost:%d/", this.port)
            );
        }
        /**
         * Mock it, and return this object.
         * @throws java.io.IOException If there is some problem inside.
         * @return This object
         */
        public final GrizzlyAdapterMocker mock()
            throws java.io.IOException {
            this.port = this.reservePort();
            final GrizzlyWebServer gws = new GrizzlyWebServer(this.port);
            gws.addGrizzlyAdapter(this, new String[] {"/"});
            gws.start();
            return this;
        }
        /**
         * Reserve port.
         * @return Reserved TCP port
         */
        protected final int reservePort() {
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
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("rawtypes")
        public final void service(final GrizzlyRequest request,
            final GrizzlyResponse response) {
            try {
                this.buffer.append(IOUtils.toString(request.getInputStream()));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
