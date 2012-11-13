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
package com.rexsl.trap;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.NotNull;

/**
 * Notifier by SMTP, with pre-packaging into bulks.
 *
 * <p>Configuration of this notifier is similar to {@link SmtpNotifier}, but
 * requires one more parameter: {@code interval}. This parameter should contain
 * an integer number of minutes we should wait before actual delivery of
 * emails. This option may be critically important if you expect high volume
 * of exceptions and don't want to receive many individual emails.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.6
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SmtpBulkNotifier extends AbstractSmtpNotifier {

    /**
     * Maximum allowed interval in minutes.
     */
    private static final long MAX_INTERVAL = 180L;

    /**
     * Minimum allowed interval in minutes.
     */
    private static final long MIN_INTERVAL = 5L;

    /**
     * The service to run the future.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads(this));

    /**
     * Running thread.
     */
    private final transient ScheduledFuture<?> future;

    /**
     * List of reported defect.
     */
    private final transient Queue<Defect> defects =
        new ConcurrentLinkedQueue<Defect>();

    /**
     * Public ctor.
     * @param props The properties
     */
    public SmtpBulkNotifier(@NotNull final Properties props) {
        super(props);
        final long interval = this.interval();
        this.future = this.service.scheduleAtFixedRate(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        SmtpBulkNotifier.this.flush();
                    }
                },
                true
            ),
            0L,
            interval,
            TimeUnit.MINUTES
        );
        Logger.info(
            this,
            "#SmtpBulkNotifier(): started with %dmin interval",
            interval
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(@NotNull final String defect) throws IOException {
        this.defects.add(new Defect(defect));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.flush();
        if (!this.future.cancel(true)) {
            throw new IOException("Failed to close scheduled future");
        }
        this.service.shutdown();
    }

    /**
     * Run this on background and send emails.
     */
    private void flush() {
        synchronized (this.defects) {
            if (!this.defects.isEmpty()) {
                try {
                    this.send(this.compress());
                } catch (IOException ex) {
                    Logger.error(this, "#run(): %[exception]s", ex);
                }
            }
        }
    }

    /**
     * Compress all defects into one message.
     * @return The message
     * @throws IOException If some problem inside
     */
    private Message compress() throws IOException {
        final StringBuilder text = new StringBuilder();
        text.append(
            String.format(
                "During the last few mins there were %d exception(s):%s%2$s",
                this.defects.size(),
                AbstractSmtpNotifier.CRLF
            )
        );
        final StringBuilder attachment = new StringBuilder();
        final Iterator<Defect> iterator = this.defects.iterator();
        while (iterator.hasNext()) {
            final Defect defect = iterator.next();
            text.append(defect.date()).append(AbstractSmtpNotifier.CRLF);
            attachment.append(defect.text()).append("\n\n");
            iterator.remove();
        }
        text.append(AbstractSmtpNotifier.CRLF)
            .append("Detailed information is attached in text file.");
        return this.mime(text.toString(), attachment.toString());
    }

    /**
     * Create MIME message with body and attachment.
     * @param text The body
     * @param attachment The attachment
     * @return The message
     * @throws IOException If some problem inside
     */
    private Message mime(final String text, final String attachment)
        throws IOException {
        final Message message = this.message();
        try {
            final Multipart multipart = new MimeMultipart();
            final BodyPart body = new MimeBodyPart();
            body.setText(text);
            multipart.addBodyPart(body);
            final BodyPart file = new MimeBodyPart();
            file.setText(attachment);
            file.setFileName("exceptions.txt");
            multipart.addBodyPart(file);
            message.setContent(multipart);
        } catch (javax.mail.MessagingException ex) {
            throw new IOException(ex);
        }
        return message;
    }

    /**
     * Calculate interval in minutes.
     * @return The interval
     */
    private long interval() {
        long interval = Long.parseLong(this.prop("interval"));
        if (interval < SmtpBulkNotifier.MIN_INTERVAL) {
            Logger.warn(
                this,
                "#interval(): set to %d, while minimum allowed is %d",
                interval,
                SmtpBulkNotifier.MIN_INTERVAL
            );
            interval = SmtpBulkNotifier.MIN_INTERVAL;
        }
        if (interval > SmtpBulkNotifier.MAX_INTERVAL) {
            Logger.warn(
                this,
                "#interval(): set to %d, while maximum allowed is %d",
                interval,
                SmtpBulkNotifier.MAX_INTERVAL
            );
            interval = SmtpBulkNotifier.MAX_INTERVAL;
        }
        return interval;
    }

    /**
     * Single defect reported.
     */
    private static final class Defect {
        /**
         * The date.
         */
        private final transient Date when = new Date();
        /**
         * The text.
         */
        private final transient String what;
        /**
         * Public ctor.
         * @param txt The text
         */
        public Defect(final String txt) {
            this.what = txt;
        }
        /**
         * Get date.
         * @return The date
         */
        public Date date() {
            return this.when;
        }
        /**
         * Get text.
         * @return The text
         */
        public String text() {
            return this.what;
        }
    }

}
