/**
 * Copyright (c) 2011-2013, ReXSL.com
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

import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * Matches HTTP header against required value.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.4
 */
final class HeaderMatcher implements AssertionPolicy {

    /**
     * Header's name.
     */
    private final transient String name;

    /**
     * The matcher to use.
     */
    private final transient Matcher<Iterable<String>> matcher;

    /**
     * Public ctor.
     * @param hdr The name of the header to match
     * @param mtch The matcher to use
     */
    public HeaderMatcher(final String hdr,
        final Matcher<Iterable<String>> mtch) {
        this.name = hdr;
        this.matcher = mtch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertThat(final TestResponse response) {
        List<String> headers = response.getHeaders().get(this.name);
        if (headers == null) {
            headers = new ArrayList<String>(0);
        }
        MatcherAssert.assertThat(
            Logger.format(
                "HTTP header '%[text]s' has to match:\n%s",
                this.name,
                response
            ),
            headers,
            this.matcher
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRetryNeeded(final int attempt) {
        return false;
    }

}
