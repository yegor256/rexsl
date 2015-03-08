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
package com.rexsl.core;

import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test case for {@link TypesMatcher}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class TypesMatcherTest {

    /**
     * The header.
     */
    private final transient String header;

    /**
     * The MIME type to check.
     */
    private final transient String type;

    /**
     * Accepts this type?
     */
    private final transient boolean accepts;

    /**
     * It's the only one there?
     */
    private final transient boolean explicit;

    /**
     * Public ctor.
     * @param hdr The header
     * @param tpe The type
     * @param acc Accepts?
     * @param exp Explicit?
     * @checkstyle ParameterNumber (3 lines)
     */
    public TypesMatcherTest(final String hdr, final String tpe,
        final boolean acc, final boolean exp) {
        this.header = hdr;
        this.type = tpe;
        this.accepts = acc;
        this.explicit = exp;
    }

    /**
     * Params for this parametrized test.
     * @return Array of arrays of params for ctor
     */
    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                {null, "text/xml", false, false},
                {"text/xml, text/plain;0.8, */*", "text/plain", true, false},
                {"application/xml;q=0.7", "application/xml", true, true},
            }
        );
    }

    /**
     * TypesMatcher can spot a type in header.
     * @throws Exception If something goes wrong
     */
    @Test
    public void decidesToAcceptOrNot() throws Exception {
        MatcherAssert.assertThat(
            new TypesMatcher(this.header).accepts(this.type),
            Matchers.describedAs(
                this.header,
                Matchers.equalTo(this.accepts)
            )
        );
    }

    /**
     * TypesMatcher can spot an explicit request.
     * @throws Exception If something goes wrong
     */
    @Test
    public void spotsAnExplicitRequestForCertainType() throws Exception {
        MatcherAssert.assertThat(
            new TypesMatcher(this.header).explicit(this.type),
            Matchers.describedAs(
                this.header,
                Matchers.equalTo(this.explicit)
            )
        );
    }

}
