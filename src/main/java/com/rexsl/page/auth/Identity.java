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

import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Authentication inset.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see <a href="http://www.rexsl.com/rexsl-page/inset-oauth.html">OAuth in RESTful Interfaces</a>
 */
@Immutable
public interface Identity {

    /**
     * Anonymous.
     */
    Identity ANONYMOUS = new Identity() {
        @Override
        public String toString() {
            return this.urn().toString();
        }
        @Override
        public URN urn() {
            return URN.create("urn:rexsl:anonymous");
        }
        @Override
        public String name() {
            return "anonymous";
        }
        @Override
        public URI photo() {
            return URI.create("http://img.rexsl.com/anonymous.png");
        }
    };

    /**
     * Unique URN.
     * @return Unique name of it, e.g. "urn:facebook:1815696122110"
     */
    URN urn();

    /**
     * Full name of the person to display, e.g. "John Doe".
     * @return Full name
     */
    String name();

    /**
     * URI of his/her photo.
     * @return URI of the image
     */
    URI photo();

    /**
     * Simple Identity.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "idnt")
    final class Simple implements Identity {
        /**
         * URN of it.
         */
        private final transient URN idnt;
        /**
         * Name of it.
         */
        private final transient String title;
        /**
         * Photo.
         */
        private final transient String pic;
        /**
         * Public ctor.
         * @param urn URN of it
         * @param name Name of it
         * @param photo Photo
         */
        public Simple(@NotNull final URN urn, @NotNull final String name,
            @NotNull final URI photo) {
            this.idnt = urn;
            this.title = name;
            this.pic = photo.toString();
        }
        /**
         * Public ctor.
         * @param identity Original identity
         */
        public Simple(@NotNull final Identity identity) {
            this(identity.urn(), identity.name(), identity.photo());
        }
        /**
         * Public ctor for a test identity, with default coordinates.
         */
        public Simple() {
            this(URN.create("urn:test:0"), "John Doe", URI.create("#"));
        }
        @Override
        public URN urn() {
            return this.idnt;
        }
        @Override
        public String name() {
            return this.title;
        }
        @Override
        public URI photo() {
            return URI.create(this.pic);
        }

    }

}
