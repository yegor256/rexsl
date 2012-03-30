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
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * Dummy transport.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.6
 */
public final class TransportMocker extends Transport {

    /**
     * Public ctor.
     */
    public TransportMocker(final Session session, final URLName name) {
        super(session, name);
        Logger.info(
            TransportMocker.class,
            "#TransportMocker('%[type]s', '%s'): instantiated",
            session,
            name
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(final Message message, final Address[] addrs) {
        try {
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "#sendMessage(..):\n  From: %[list]s\n  To: %[list]s\n  CC:%s\n  Reply-to: %[list]s\n  Subject: %s\n  Text: %s",
                message.getFrom(),
                message.getRecipients(Message.RecipientType.TO),
                message.getRecipients(Message.RecipientType.CC),
                message.getReplyTo(),
                message.getSubject(),
                message.getContent()
            );
        } catch (javax.mail.MessagingException ex) {
            throw new IllegalArgumentException(ex);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(final String host, final int port, final String user,
        final String password) {
        Logger.info(
            this,
            "#connect('%s', %d, '%s', '%s')",
            host,
            port,
            user,
            password
        );
    }

}
