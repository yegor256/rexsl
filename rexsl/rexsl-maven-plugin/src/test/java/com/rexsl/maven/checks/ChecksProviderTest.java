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
package com.rexsl.maven.checks;

import com.rexsl.maven.Check;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ChecksProvider}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class ChecksProviderTest {

    /**
     * Forward SLF4J to Maven Log.
     * @throws Exception If something is wrong inside
     */
    @BeforeClass
    public static void startLogging() throws Exception {
        new com.rexsl.maven.LogMocker().mock();
    }

    /**
     * ChecksProvider can provide a set of checks.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retrievesSetOfChecks() throws Exception {
        final Set<Check> checks = new ChecksProvider().all();
        MatcherAssert.assertThat(checks, Matchers.notNullValue());
        MatcherAssert.assertThat(checks.size(), Matchers.greaterThan(0));
    }

    /**
     * ChecksProvider can provide a set of checks.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void retrievesSpecifiedCheck() throws Exception {
        final ChecksProvider checksProvider = new ChecksProvider();
        final String sCheck = "JigsawCssCheck";
        checksProvider.setCheck(sCheck);
        final Set<Check> checks = checksProvider.all();
        MatcherAssert.assertThat(checks, Matchers.notNullValue());
        MatcherAssert.assertThat(checks.size(), Matchers.is(1));
        MatcherAssert.assertThat(
            checks.iterator().next().getClass().getSimpleName(),
            Matchers.is(sCheck)
        );
    }

}
