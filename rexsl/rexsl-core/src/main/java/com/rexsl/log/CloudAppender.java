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

import com.ymock.util.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
@SuppressWarnings("PMD.DoNotUseThreads")
public final class CloudAppender extends AppenderSkeleton implements Runnable {

    /**
     * End of line.
     */
    public static final String EOL = "\n";

    /**
     * Queue of messages to send to server.
     */
    private final transient BlockingQueue<String> messages =
        new LinkedBlockingQueue<String>();

    /**
     * The feeder.
     */
    private transient Feeder feeder;

    /**
     * Are we still alive?
     */
    private final transient AtomicBoolean alive = new AtomicBoolean(false);

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
        final Thread thread = new Thread(this);
        thread.setName(Logger.format("CloudAppender to %[type]s", this.feeder));
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.alive.set(false);
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
                buf.append(text).append(this.EOL);
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    public void run() {
        System.out.println(
            Logger.format(
                "CloudAppender started to work with %s...",
                this.feeder
            )
        );
        this.alive.set(true);
        while (this.alive.get()) {
            String text;
            try {
                text = this.messages.take();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
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
        System.out.println(
            Logger.format(
                "CloudAppender finished to work with %s.",
                this.feeder
            )
        );
    }

}
