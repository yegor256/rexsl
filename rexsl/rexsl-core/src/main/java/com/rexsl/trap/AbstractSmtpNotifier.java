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
import com.rexsl.core.Manifests;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

/**
 * Abstract notifier by SMTP.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/mail/package-summary.html">javax.mail</a>
 */
abstract class AbstractSmtpNotifier implements Notifier {

    /**
     * End of line, according to RFC2049.
     * @see <a href="http://www.ietf.org/rfc/rfc2049.txt">RFC 2049</a>
     */
    protected static final String CRLF = "\r\n";

    /**
     * SMTP session.
     */
    private final transient Session session;

    /**
     * Properties.
     */
    private final transient Properties properties;

    /**
     * Public ctor.
     * @param props The properties
     */
    public AbstractSmtpNotifier(@NotNull final Properties props) {
        this.session = Session.getInstance(props);
        this.properties = props;
    }

    /**
     * Create new message.
     * @return The message
     * @throws IOException If some problem inside
     */
    protected final Message message() throws IOException {
        return new MimeMessage(this.session);
    }

    /**
     * Send message by email.
     * @param message The message to send
     * @throws IOException If some problem inside
     */
    protected final void send(final Message message) throws IOException {
        final Transport transport = this.transport();
        try {
            transport.connect(
                this.prop("mail.smtp.host"),
                Integer.parseInt(this.prop("mail.smtp.port")),
                this.prop("mail.smtp.user"),
                this.prop("password")
            );
            final InternetAddress reply = new InternetAddress(
                this.prop("mail.smtp.from")
            );
            message.addFrom(new Address[] {reply});
            message.setReplyTo(new Address[] {reply});
            message.addRecipient(
                javax.mail.Message.RecipientType.TO,
                new InternetAddress(this.prop("to"))
            );
            message.setSubject(this.prop("subject"));
            transport.sendMessage(message, message.getAllRecipients());
        } catch (javax.mail.MessagingException ex) {
            throw new IOException(ex);
        } finally {
            try {
                transport.close();
            } catch (javax.mail.MessagingException ex) {
                Logger.error(this, "#send(..): failed %[exception]s", ex);
            }
        }
    }

    /**
     * Create transport.
     * @return The transport just created
     * @throws IOException If some problem inside
     */
    private Transport transport() throws IOException {
        Transport transport;
        try {
            transport = this.session.getTransport(this.prop("transport"));
        } catch (javax.mail.MessagingException ex) {
            throw new IOException(ex);
        }
        return transport;
    }

    /**
     * Get one property.
     * @param name Name of it
     * @return Value of it
     */
    protected final String prop(final String name) {
        if (!this.properties.containsKey(name)) {
            throw new IllegalStateException(
                Logger.format("'%s' param not found", name)
            );
        }
        String value = this.properties.getProperty(name);
        if (!value.isEmpty() && value.charAt(0) == ':') {
            value = Manifests.read(value.substring(1));
        }
        return value;
    }

}
