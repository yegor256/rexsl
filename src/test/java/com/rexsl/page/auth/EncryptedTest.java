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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Encrypted}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class EncryptedTest {

    /**
     * Encrypted can be converted to text and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToTextAndBack() throws Exception {
        final String key = "&6%4\u0433-({}*7hrs";
        final String name = "John Doe, \u0433";
        final URN urn = new URN("urn:test:89798");
        final Encrypted user = new Encrypted(
            new Identity.Simple(urn, name, URI.create("#")), key
        );
        final Identity reverted = Encrypted.parse(
            user.cookie(), key
        );
        MatcherAssert.assertThat(
            reverted.name(),
            Matchers.equalTo(name)
        );
        MatcherAssert.assertThat(
            reverted.urn(),
            Matchers.equalTo(urn)
        );
    }

    /**
     * Encrypted can throw on NULL.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = Encrypted.DecryptionException.class)
    public void throwsOnNullInput() throws Exception {
        Encrypted.parse(null, "");
    }

    /**
     * Encrypted can throw on invalid input.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = Encrypted.DecryptionException.class)
    public void throwsOnBrokenInput() throws Exception {
        Encrypted.parse("invalid-data", "");
    }

    /**
     * Encrypted can throw on empty input.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = Encrypted.DecryptionException.class)
    public void throwsOnEmptyInput() throws Exception {
        Encrypted.parse("", "");
    }

}
