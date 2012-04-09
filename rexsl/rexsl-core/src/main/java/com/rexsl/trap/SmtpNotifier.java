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
import javax.mail.Message;

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
 *    com.rexsl.trap.SmtpNotifier?to=me&#64;example.com
 *    &amp;subject=Runtime%20problem%20at%20example%2Ecom
 *    &amp;from=no-replyexample%2Ecom
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
 * one of available {@code MANIFEST.MF} files (read more in
 * {@link com.rexsl.core.Manifests}).
 *
 * <p>Spaces and any control symbols (new lines, tabs, etc) inside
 * the URI are removed automatically.
 *
 * <p>We recommend to use {@link SmtpBulkNotifier}, which caches messages in
 * a temporary buffer and sends them all together every X seconds/minutes.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/mail/package-summary.html">javax.mail</a>
 * @see SmtpBulkNotifier
 */
public final class SmtpNotifier extends AbstractSmtpNotifier {

    /**
     * Public ctor.
     * @param props The properties
     */
    public SmtpNotifier(final Properties props) {
        super(props);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final String defect) throws IOException {
        final Message message = this.message();
        try {
            message.setText(defect.replace("\n", AbstractSmtpNotifier.CRLF));
        } catch (javax.mail.MessagingException ex) {
            throw new IOException(ex);
        }
        this.send(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to close here
    }

}
