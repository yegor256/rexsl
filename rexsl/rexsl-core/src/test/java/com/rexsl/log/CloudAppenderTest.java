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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link CloudAppender}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (9 lines)
 */
public final class CloudAppenderTest {

    /**
     * Out content.
     */
    private final transient ByteArrayOutputStream outContent =
        new ByteArrayOutputStream();

    /**
     * Before.
     */
    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    /**
     * After.
     */
    @After
    public void cleanUpStreams() {
        System.setOut(System.out);
    }

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
            // @checkstyle MultipleStringLiterals (2 lines)
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
                    Matchers.startsWith("DEBUG - some text to log"),
                    Matchers.containsString("IllegalArgumentException")
                )
            )
        );
        appender.close();
    }

    /**
     * CloudAppender outputs an error message when the message queue is full.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void outputMessageWhenQueueIsFull() throws Exception {
        final CloudAppender appender = new CloudAppender();
        appender.setLayout(new SimpleLayout());
        final Field messagesField =
            CloudAppender.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        // @checkstyle MagicNumber (3 lines)
        final int numElements = 500;
        messagesField.set(
            appender,
            new LinkedBlockingQueue<String>(numElements)
        );
        final LoggingEvent event = new LoggingEvent(
            this.getClass().getName(),
            new RootLogger(Level.DEBUG),
            Level.DEBUG,
            "some text to log",
            new IllegalArgumentException()
        );
        int index = 0;
        while (index <= numElements) {
            appender.append(event);
            ++index;
        }
        MatcherAssert.assertThat(
            this.outContent.toString(),
            Matchers.startsWith(
                "CloudAppender doesn't have space available to store the event"
            )
        );
        appender.close();
    }

    /**
     * CloudAppender validates it's state before run.
     */
    @Test(expected = IllegalStateException.class)
    public void validateAppenderState() {
        final CloudAppender appender = new CloudAppender();
        appender.activateOptions();
    }

    /**
     * CloudAppender checks if feeder is set only once.
     */
    @Test(expected = IllegalStateException.class)
    public void setsFeeder() {
        final CloudAppender appender = new CloudAppender();
        appender.setFeeder(Mockito.mock(Feeder.class));
        appender.setFeeder(Mockito.mock(Feeder.class));
    }
}
