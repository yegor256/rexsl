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

import com.ymock.util.Logger;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * Notifier by SMTP, with pre-packaging into bulks.
 *
 * <p>Configuration of this notifier is similar to {@link SmtpNotifier}, but
 * requires one more parameter: {@code interval}. This parameter should contain
 * an integer number of minutes we should wait before actual delivery of
 * emails. This option may be critically important if you expect high volume
 * of exceptions and don't want to receive many individual emails.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SmtpBulkNotifier extends AbstractSmtpNotifier
    implements Runnable {

    /**
     * Maximum allowed interval in minutes.
     */
    private static final Long MAX_INTERVAL = 180L;

    /**
     * Minimum allowed interval in minutes.
     */
    private static final Long MIN_INTERVAL = 5L;

    /**
     * List of reported defect.
     */
    private final transient Queue<Defect> defects =
        new ConcurrentLinkedQueue<Defect>();

    /**
     * Public ctor.
     * @param props The properties
     */
    public SmtpBulkNotifier(final Properties props) {
        super(props);
        final Long interval = this.interval();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            this,
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
    public void notify(final String defect) throws IOException {
        synchronized (this) {
            this.defects.add(new Defect(defect));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        synchronized (this) {
            if (!this.defects.isEmpty()) {
                try {
                    final Message message = this.compress();
                    this.send(message);
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
                // @checkstyle LineLength (1 line)
                "During the last few mins there were %d exceptions:\n\n",
                this.defects.size()
            )
        );
        final StringBuilder attachment = new StringBuilder();
        final Iterator<Defect> iterator = this.defects.iterator();
        while (iterator.hasNext()) {
            final Defect defect = iterator.next();
            text.append(defect.text()).append("\n\n");
            attachment.append(defect.date()).append("\n");
            iterator.remove();
        }
        text.append("Detailed information is attached in text file.");
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
    private Long interval() {
        Long interval = Long.valueOf(this.prop("interval"));
        if (interval < this.MIN_INTERVAL) {
            Logger.warn(
                this,
                "#interval(): set to %d, while minimum allowed is %d",
                interval,
                this.MIN_INTERVAL
            );
            interval = this.MIN_INTERVAL;
        }
        if (interval > this.MAX_INTERVAL) {
            Logger.warn(
                this,
                "#interval(): set to %d, while maximum allowed is %d",
                interval,
                this.MAX_INTERVAL
            );
            interval = this.MAX_INTERVAL;
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
