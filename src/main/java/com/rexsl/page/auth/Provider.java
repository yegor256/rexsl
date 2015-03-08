/**
 * Copyright (c) 2011-2015, ReXSL.com
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

import com.rexsl.page.Link;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.constraints.NotNull;

/**
 * Authentication provider.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see <a href="http://www.rexsl.com/rexsl-page/inset-oauth.html">OAuth in RESTful Interfaces</a>
 */
public interface Provider {

    /**
     * Get user's identity or {@link Identity.ANONYMOUS} if can't authenticate.
     * @return Identity authenticated (or {@link Identity.ANONYMOUS})
     * @throws IOException If failed for some exceptional reason
     */
    Identity identity() throws IOException;

    /**
     * Annotates a provider that requires redirecting
     * right after authentication.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Redirect {
    }

    /**
     * Visible provider, for end-user.
     */
    interface Visible {
        /**
         * Get authentication link.
         * @return The link
         */
        Link link();
    }

    /**
     * Always returns the same identity (mostly used for unit testing).
     *
     * <p>This class is very useful for unit testing, when you need to
     * configure JAX-RS resource with a pre-authenticated identity,
     * for example:
     *
     * <pre> final Identity identity = new Identity.Simple();
     * final IndexRs rest = new ResourceMocker().mock(IndexRs.class);
     * rest.auth().with(new Provider.Always(identity));
     * </pre>
     *
     * @since 0.4.11
     */
    final class Always implements Provider {
        /**
         * The identity to return.
         */
        private final transient Identity idnt;
        /**
         * Public ctor.
         * @param identity Identity to return always
         */
        public Always(@NotNull final Identity identity) {
            this.idnt = identity;
        }
        @Override
        public Identity identity() throws IOException {
            return this.idnt;
        }
    }

}
