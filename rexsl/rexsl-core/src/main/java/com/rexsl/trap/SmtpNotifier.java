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

import com.rexsl.core.Manifests;
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
 * <p>Configuration of this notifier is to be done via its URI in
 * {@code web.xml}, for example:
 *
 * <pre>
 * &lt;servlet>
 *  &lt;servlet-name&gt;ExceptionTrap&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;com.rexsl.trap.ExceptionTrap&lt;/servlet-class&gt;
 *  &lt;init-param&gt;
 *   &lt;param-name&gt;com.rexsl.trap.Notifier&lt;/param-name&gt;
 *   &lt;param-value&gt;
 *    com.rexsl.trap.SmtpNotifier?to=me&#64;example.com&amp;host=gmail.com...
 *   &lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre>
 *
 * <p>All parameters you set as URI query params will be delivered to Java Mail
 * API as explained in {@link javax.mail}. The following parameters are expected
 * besides the ones defined in the API:
 * {@code transport}, {@code password}, {@code subject}, {@code to}.
 *
 * <p>You can specify explicit values of parameters or refer us to one of your
 * {@code MANIFEST.MF} files, for example:
 *
 * <pre>
 * com.rexsl.trap.SmtpNotifier?mail.smtp.host=:My-Host
 * </pre>
 *
 * <p>In this case we will try to find and read {@code My-Host} attribute from
 * one of available {@code MANIFEST.MF} files (read more in {@link Manifests}).
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/mail/package-summary.html">javax.mail</a>
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
        this.session = Session.getInstance(props);
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
                this.prop("mail.smtp.host"),
                Integer.valueOf(this.prop("mail.smtp.port")),
                this.prop("mail.smtp.user"),
                this.prop("password")
            );
            final Message message = new MimeMessage(this.session);
            final InternetAddress reply = new InternetAddress(
                this.prop("mail.smtp.from")
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
     * @return Value of it
     * @throws IOException If can't find it
     */
    private String prop(final String name) throws IOException {
        if (!this.properties.containsKey(name)) {
            throw new IOException(
            String.format("'%s' param not found", name)
        );
        }
        String value = this.properties.getProperty(name);
        if (!value.isEmpty() && value.charAt(0) == ':') {
            value = Manifests.read(value.substring(1));
        }
        return value;
    }

}
