/**
 * Copyright (c) 2011-2014, ReXSL.com
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
package com.rexsl.page.auth;

import com.jcabi.urn.URN;
import java.net.URI;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link Identity}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class IdentityMocker {

    /**
     * The mock.
     */
    private final transient Identity identity = Mockito.mock(Identity.class);

    /**
     * Public ctor.
     */
    public IdentityMocker() {
        this.withName("Mocked Joe");
        this.withURN(URN.create("urn:rexsl:mocked"));
        this.withPhoto(Identity.ANONYMOUS.photo());
    }

    /**
     * With this name.
     * @param name The name
     * @return This object
     */
    public IdentityMocker withName(final String name) {
        Mockito.doReturn(name).when(this.identity).name();
        return this;
    }

    /**
     * With this URN.
     * @param urn The URN
     * @return This object
     */
    public IdentityMocker withURN(final URN urn) {
        Mockito.doReturn(urn).when(this.identity).urn();
        return this;
    }

    /**
     * With this photo.
     * @param photo The photo
     * @return This object
     */
    public IdentityMocker withPhoto(final URI photo) {
        Mockito.doReturn(photo).when(this.identity).photo();
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The identity just created
     */
    public Identity mock() {
        return this.identity;
    }

}
