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

import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link CloudAppender}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class CloudAppenderTest {

    /**
     * CloudAppender can send messages in background.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsMessagesInBackground() throws Exception {
        final CloudAppender appender = new CloudAppender();
        appender.setLayout(new SimpleLayout());
        final Feeder feeder = Mockito.mock(Feeder.class);
        final StringBuilder builder = new StringBuilder();
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    final String text = (String) invocation.getArguments()[0];
                    builder.append(text);
                    return null;
                }
            }
        ).when(feeder).feed(Mockito.anyString());
        appender.setFeeder(feeder);
        appender.activateOptions();
        final LoggingEvent event = new LoggingEvent(
            this.getClass().getName(),
            new RootLogger(Level.DEBUG),
            Level.DEBUG,
            "some text to log",
            new IllegalArgumentException()
        );
        appender.append(event);
        final long start = System.currentTimeMillis();
        while (builder.length() == 0) {
            TimeUnit.SECONDS.sleep(1L);
            if (System.currentTimeMillis() - start
                > TimeUnit.MINUTES.toMillis(1L)) {
                throw new IllegalStateException("waiting for too long");
            }
        }
        Mockito.verify(feeder).feed(
            Mockito.argThat(
                Matchers.<String>allOf(
                    Matchers.startsWith("DEBUG - some text to log\n"),
                    Matchers.containsString("IllegalArgumentException")
                )
            )
        );
        appender.close();
    }

}
