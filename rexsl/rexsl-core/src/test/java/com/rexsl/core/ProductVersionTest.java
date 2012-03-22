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
 * Test case for {@link ProductVersion}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class ProductVersionTest {

    /**
     * Left part of comparison.
     */
    private final transient ProductVersion left;

    /**
     * Right part of comparison.
     */
    private final transient ProductVersion right;

    /**
     * Result of comparison.
     */
    private final transient int result;

    /**
     * Public ctor.
     * @param lft The left
     * @param rght The right
     * @param res The result
     */
    public ProductVersionTest(final String lft, final String rght,
        final int res) {
        this.left = new ProductVersion(lft);
        this.right = new ProductVersion(rght);
        this.result = res;
    }

    /**
     * Params for this parametrized test.
     * @return Array of arrays of params for ctor
     */
    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                {"1.13", "1.9", 1},
                {"1.0", "0.8.9-gamma", 1},
                {"9.6.0-beta", "9.6.0-alpha", 1},
                // @checkstyle MultipleStringLiterals (1 line)
                {"3.0", "3.0", 0},
                {"5.0.17", "5.0", 1},
                {"5.0.4", "5.0.14", -1},
                {"0.9", "0.9.1", -1},
            }
        );
    }

    /**
     * ProductVersion can compare.
     * @throws Exception If something goes wrong
     */
    @Test
    public void comparesTwoVersions() throws Exception {
        int cmp = this.left.compareTo(this.right);
        if (cmp != 0) {
            cmp = cmp / Math.abs(cmp);
        }
        MatcherAssert.assertThat(
            cmp,
            Matchers.describedAs(
                String.format(
                    "%d at [%s vs. %s]",
                    this.result,
                    this.left,
                    this.right
                ),
                Matchers.equalTo(this.result)
            )
        );
    }

    /**
     * ProductVersion equals method test.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testEquality() throws Exception {
        final int cmp = this.left.compareTo(this.right);
        if (cmp == 0) {
            MatcherAssert.assertThat(
                this.left,
                Matchers.describedAs(
                    String.format(
                        // @checkstyle MultipleStringLiterals (2 lines)
                        "[%s vs. %s]",
                        this.left,
                        this.right
                    ),
                    Matchers.equalTo(this.right)
                )
            );
        } else {
            MatcherAssert.assertThat(
                this.left,
                Matchers.describedAs(
                    String.format(
                        "[%s vs. %s]",
                        this.left,
                        this.right
                    ),
                    Matchers.not(this.right)
                )
            );
        }
    }

}
