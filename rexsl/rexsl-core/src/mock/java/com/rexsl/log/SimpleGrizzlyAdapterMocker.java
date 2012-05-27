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
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import org.apache.commons.io.IOUtils;

/**
 * Simple Custom {@link GrizzlyAdapter}.
 * It stores the request data into a buffer.
 * @author Flavius Ivasca (ivascaflavius@gmail.com)
 * @version $Id: SimpleGrizzlyAdapterMocker.java $
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
public class SimpleGrizzlyAdapterMocker extends GrizzlyAdapter {
    /**
     * Buffer that stores concatenated data from requests.
     */
    private final transient StringBuffer buffer = new StringBuffer();
    /**
     * Port where it works.
     */
    private transient int port;

    /**
     * Get option {@code buffer}.
     * @return Buffer that holds concatenated data from requests
     */
    public final StringBuffer getBuffer() {
        return this.buffer;
    }

    /**
     * Get its home.
     * @return URI of the started container
     */
    public final URI home() {
        try {
            return new URI(Logger.format("http://localhost:%d/", this.port));
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Mock it, and return this object.
     * @return This object
     */
    public final SimpleGrizzlyAdapterMocker mock() {
        this.port = this.reservePort();
        final GrizzlyWebServer gws = new GrizzlyWebServer(this.port);
        gws.addGrizzlyAdapter(this, new String[] {"/"});
        try {
            gws.start();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return this;
    }

    /**
     * Reserve port.
     * @return Reserved TCP port
     */
    protected final int reservePort() {
        int reserved;
        try {
            final ServerSocket socket = new ServerSocket(0);
            try {
                reserved = socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return reserved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException", "rawtypes" })
    public final void service(final GrizzlyRequest request,
        final GrizzlyResponse response) {
        String input = null;
        try {
            input = IOUtils.toString(request.getInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        if (input != null) {
            this.buffer.append(input);
        }
    }

}
