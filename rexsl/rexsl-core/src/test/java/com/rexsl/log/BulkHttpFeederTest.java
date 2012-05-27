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
import com.rexsl.test.ContainerMocker;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link BulkHttpFeeder}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
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
     * Test {@link BulkHttpFeeder} for thread safety.
     * @throws Exception If there is some problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Test
    public void testThreadSafety() throws Exception {
        final long period = 300L;
        final int numThreads = 20;
        final int numTriesPerThread = 5;
        final int timeout = 2000;
        final StringBuffer expectedResult = new StringBuffer();
        final SimpleGrizzlyAdapterMocker adapter =
        new SimpleGrizzlyAdapterMocker().mock();
        final BulkHttpFeeder feeder = new BulkHttpFeeder();
        feeder.setUrl(adapter.home().toString());
        feeder.setPeriod(period);
        feeder.setUnit(TimeUnit.MILLISECONDS);
        feeder.activateOptions();
        final CountDownLatch latch = new CountDownLatch(numTriesPerThread);
        final ExecutorService taskExecutor =
            Executors.newFixedThreadPool(numThreads);
        for (int num = 0; num < numThreads; num = num + 1) {
            taskExecutor.execute(
                new TestThread(latch, feeder, expectedResult)
            );
        }
        latch.await();
        taskExecutor.shutdown();
        Thread.sleep(timeout);
        final StringBuffer actualResult = adapter.getBuffer();
        Assert.assertEquals(
            expectedResult.toString(),
            actualResult.toString()
        );
    }

    /**
     * Thread used for testing thread safety of {@link BulkHttpFeeder}.
     * @author Flavius Ivasca (ivascaflavius@gmail.com)
     */
    @SuppressWarnings({ "PMD.CascadeIndentationCheck",
        "PMD.AvoidStringBufferField" })
    class TestThread implements Runnable {
        /**
         * Message to be sent through POST.
         */
        private static final String POST_MESSAGE = "foo";
        /**
         * Time interval in milis to wait between feeding.
         */
        private static final int FEED_PERIOD = 100;
        /**
         * Buffer to store all fed data.
         */
        private final transient StringBuffer buffer;
        /**
         * Countdown latch used for managing feed trials.
         */
        private final transient CountDownLatch latch;
        /**
         * HTTP feeder to which POST requests are sent.
         */
        private final transient BulkHttpFeeder feeder;
        /**
         * Creates a new {@link TestThread}.
         * @param cdlatch Count down latch
         * @param httpfeeder Bulk HTTP feeder
         * @param data Buffer to hold fed data
         */
        TestThread(final CountDownLatch cdlatch,
            final BulkHttpFeeder httpfeeder,
            final StringBuffer data) {
            this.latch = cdlatch;
            this.feeder = httpfeeder;
            this.buffer = data;
            new Thread(this);
        }
        /**
         * Logs {@link Exception} to the logger.
         * @param exc Exception to be logged
         */
        private void logException(final Exception exc) {
            Logger.warn(this, "#testThreadSafety(): %[exception]s", exc);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                this.feeder.feed(this.POST_MESSAGE);
                this.buffer.append(this.POST_MESSAGE);
                Thread.sleep(this.FEED_PERIOD);
            } catch (IOException exc) {
                this.logException(exc);
            } catch (InterruptedException exc) {
                this.logException(exc);
            } finally {
                this.latch.countDown();
            }
        }
    }
}
