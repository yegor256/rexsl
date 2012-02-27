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

import java.io.IOException;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Notifier by SMTP.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 */
public final class SmtpNotifier implements Notifier {

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
    public SmtpNotifier(final Properties props) {
        this.session = Session.getInstance(new Properties());
        this.properties = props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final String defect) throws IOException {
        try {
            final Transport transport = this.session.getTransport(
                this.prop("transport")
            );
            transport.connect(
                this.prop("host"),
                Integer.valueOf(this.prop("port")),
                this.prop("user"),
                this.prop("password")
            );
            final Message message = new MimeMessage(this.session);
            final InternetAddress reply = new InternetAddress(
                this.prop("reply-to")
            );
            message.addFrom(new Address[] {reply});
            message.setReplyTo(new Address[] {reply});
            message.addRecipient(
                javax.mail.Message.RecipientType.TO,
                new InternetAddress(this.prop("to"))
            );
            message.setText(defect);
            message.setSubject(this.prop("subject"));
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (javax.mail.NoSuchProviderException ex) {
            throw new IOException(ex);
        } catch (javax.mail.MessagingException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Get one property.
     * @param name Name of it
     * @throws IOException If can't find it
     */
    private String prop(final String name) throws IOException {
        if (!this.properties.containsKey(name)) {
            throw new IOException(String.format("%s param not found", name));
        }
        return this.properties.getProperty(name);
    }

}
