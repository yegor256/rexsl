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
import com.jcabi.log.VerboseThreads;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log appender, for cloud loggers.
 *
 * <p>Configure it in your {@code log4j.properties} like this (just an example,
 * which uses <a href="http://www.loggly.com">loggly.com</a> HTTP log
 * consuming interface):
 *
 * <pre>
 * log4j.rootLogger=WARN, LOGGLY, CONSOLE
 * log4j.appender.LOGGLY=com.rexsl.log.CloudAppender
 * log4j.appender.LOGGLY.feeder=com.rexsl.log.HttpFeeder
 * log4j.appender.LOGGLY.feeder.url=https://logs.loggly.com/inputs/0604e96...
 * log4j.appender.LOGGLY.layout=org.apache.log4j.PatternLayout
 * log4j.appender.LOGGLY.layout.ConversionPattern = [%5p] %t %c: %m\n
 * log4j.appender.CONSOLE=com.netbout.log.CloudAppender
 * log4j.appender.CONSOLE.feeder=com.rexsl.log.ConsoleFeeder
 * log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
 * log4j.appender.CONSOLE.layout.ConversionPattern = [%5p] %t %c: %m\n
 * </pre>
 *
 * <p>You can extend it with your own feeding mechanisms. Just implement
 * the {@link Feeder} interface and add an instance of the class to the
 * appender.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.2
 */
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.SystemPrintln" })
public final class CloudAppender extends AppenderSkeleton {

    /**
     * End of line, for our own internal presentation.
     *
     * <p>We use this symbol in order to separate lines in buffer, not in order
     * to show them to the user. Thus, it's platform independent symbol and
     * will work on any OS (incl. Windows).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String EOL = "\n";

    /**
     * Queue of messages to send to server.
     */
    private final transient BlockingQueue<String> messages =
        new LinkedBlockingQueue<String>();

    /**
     * The service to run the background process.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads(this));

    /**
     * The feeder.
     */
    private transient Feeder feeder;

    /**
     * The future we're running in.
     */
    private transient ScheduledFuture future;

    /**
     * Set feeder, option {@code feeder} in config.
     * @param fdr The feeder to use
     */
    public void setFeeder(final Feeder fdr) {
        if (this.feeder != null) {
            throw new IllegalStateException("call #setFeeder() only once");
        }
        this.feeder = fdr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        if (this.feeder == null) {
            throw new IllegalStateException(
                "Unable to start with no feeder set"
            );
        }
        super.activateOptions();
        System.out.println(
            Logger.format(
                "CloudAppender started to work with %s...",
                this.feeder
            )
        );
        this.future = this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        CloudAppender.this.flush();
                    }
                }
            ),
            1L,
            1L,
            TimeUnit.SECONDS
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (this.future != null) {
            this.future.cancel(true);
        }
        this.service.shutdown();
        System.out.println(
            Logger.format(
                "CloudAppender finished to work with %s.",
                this.feeder
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    public void append(final LoggingEvent event) {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.getLayout().format(event));
        final String[] exc = event.getThrowableStrRep();
        if (exc != null) {
            for (String text : exc) {
                buf.append(text).append(CloudAppender.EOL);
            }
        }
        final boolean correctlyInserted = this.messages.offer(buf.toString());
        if (!correctlyInserted) {
            System.out.println(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "CloudAppender doesn't have space available to store the event: %s",
                    buf.toString()
                )
            );
            Logger.warn(
                this,
                // @checkstyle LineLength (1 line)
                "The messages queue has reached its capacity. The event couldn't be added: %s",
                buf.toString()
            );
        }
    }

    /**
     * Method to be executed by a thread in the background.
     *
     * <p>Takes messages from the queue and feeds the feeder.
     */
    private void flush() {
        String text;
        try {
            text = this.messages.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        try {
            this.feeder.feed(text);
        } catch (java.io.IOException ex) {
            System.out.println(
                Logger.format(
                    "%sCloudAppender failed to report: %[exception]s",
                    text,
                    ex
                )
            );
        }
    }

}
