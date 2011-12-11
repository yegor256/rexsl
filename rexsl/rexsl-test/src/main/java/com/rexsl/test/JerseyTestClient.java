/**
 * Copyright (c) 2011, ReXSL.com
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
package com.rexsl.test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.ymock.util.Logger;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Implementation of {@link TestClient}.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
final class JerseyTestClient implements TestClient {

    /**
     * Jersey web resource.
     */
    private transient WebResource.Builder builder;

    /**
     * Entry point.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param res The resource to work with
     */
    public JerseyTestClient(final WebResource res) {
        this.builder = res.getRequestBuilder();
        this.home = res.getURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestClient header(final String name, final String value) {
        Logger.debug(this, "#header('%s', '%s'): set", name, value);
        this.builder.header(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse get() throws Exception {
        return this.method("GET", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse post(final String body) throws Exception {
        return this.method("POST", body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResponse put(final String body) throws Exception {
        return this.method("PUT", body);
    }

    /**
     * Run this method.
     * @param name The name of HTTP method
     * @param body Body of HTTP request
     * @return The response
     */
    public TestResponse method(final String name, final String body)
        throws Exception {
        final long start = System.currentTimeMillis();
        final ClientResponse resp = this.builder.method(
            name,
            ClientResponse.class,
            body
        );
        Logger.info(
            this,
            "#%s(%s): completed in %dms [%d %s]",
            name,
            this.home.getPath(),
            System.currentTimeMillis() - start,
            resp.getStatus(),
            resp.getClientResponseStatus()
        );
        return new JerseyTestResponse(resp);
    }

}
