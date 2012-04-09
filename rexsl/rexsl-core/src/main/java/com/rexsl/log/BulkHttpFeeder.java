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
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Bulk feeder through HTTP POST request.
 *
 * <p>The feeder sends POST HTTP requests every X seconds (as configured
 * with {@link #setPeriod(long)}).
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class BulkHttpFeeder extends AbstractHttpFeeder {

    /**
     * Period in seconds.
     * @checkstyle MagicNumber (2 lines)
     */
    private transient long period = 60L;

    /**
     * Buffer to send.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final transient StringBuffer buffer = new StringBuffer();

    /**
     * The future we're running in.
     */
    private transient Future future;

    /**
     * Set option {@code period}.
     * @param secs Interval, in seconds
     */
    public void setPeriod(final long secs) {
        this.period = secs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format("HTTP bulk POST to \"%s\"", this.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (!this.future.cancel(true)) {
            throw new IOException("Failed to close scheduled future");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        this.future = Executors
            .newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BulkHttpFeeder.this.flush();
                        } catch (java.io.IOException ex) {
                            Logger.warn(this, "#run(): %[exception]s", ex);
                        }
                    }
                },
                1L,
                this.period,
                TimeUnit.SECONDS
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void feed(final String text) throws IOException {
        this.buffer.append(text);
    }

    /**
     * Flush buffer through HTTP.
     *
     * @throws IOException If some IO problem inside
     * @todo #296 This implemenation is not thread-safe. It's possible to
     *  to get content from the buffer, and then some other thread will
     *  add a line, and then we set length to zero. We will lose that line.
     *  Synchronization should added into feed() and here.
     */
    private void flush() throws IOException {
        final String text = this.buffer.toString();
        this.buffer.setLength(0);
        this.post(text);
    }

}
