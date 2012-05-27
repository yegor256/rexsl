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
    @Test
    public void testThreadSafety() throws Exception {
        final long period = 300L;
        final int numThreads = 20;
        final int numTriesPerThread = 5;
        final int feedPeriod = 100;
        final int timeout = 2000;
        final StringBuffer expectedResult = new StringBuffer();
        final SimpleGrizzlyAdapterMocker adapter =
        new SimpleGrizzlyAdapterMocker().mock();
        final String postMessage = "foo";
        final BulkHttpFeeder feeder = new BulkHttpFeeder();
        feeder.setUrl(adapter.home().toString());
        feeder.setPeriod(period);
        feeder.setUnit(TimeUnit.MILLISECONDS);
        feeder.activateOptions();
        for (int val = 0; val < numThreads; val = val + 1) {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int num = 0; num < numTriesPerThread;
                        num = num + 1) {
                        try {
                            feeder.feed(postMessage);
                            expectedResult.append(postMessage);
                            Thread.sleep(feedPeriod);
                        } catch (IOException exc) {
                            BulkHttpFeederTest.this.logException(exc);
                        } catch (InterruptedException exc) {
                            BulkHttpFeederTest.this.logException(exc);
                        }
                    }
                }
            });
            thread.start();
        }
        Thread.sleep(timeout);
        final StringBuffer actualResult = adapter.getBuffer();
        Assert.assertEquals(
            expectedResult.toString(),
            actualResult.toString()
        );
    }

    /**
     * Logs {@link Exception} to the logger.
     * @param exc Exception to be logged
     */
    private void logException(final Exception exc) {
        Logger.warn(this, "#testThreadSafety(): %[exception]s", exc);
    }
}
