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
package com.rexsl.maven.checks;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link CssStaticCheck}.
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id$
 * @todo #57 Implement JS stylesheet validation and enable all test methods in
 *  this class.
 */
public final class JSStaticCheckTest {

    /**
     * JSStaticCheck can validate correct Java script file.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesCorrectJSFile() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withFile("src/main/webapp/js/valid.js")
            .mock();
        MatcherAssert.assertThat(
            "valid Java script passes without problems",
            new JSStaticCheck().validate(env)
        );
    }

    /**
     * JSStaticCheck can validate incorrect Java script file.
     * @throws Exception If something goes wrong
     */
    @Ignore
    @Test
    public void validatesIncorrectJSFile() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withFile("src/main/webapp/js/invalid.js")
            .mock();
        MatcherAssert.assertThat(
            "invalid JS is caught",
            !new JSStaticCheck().validate(env)
        );
    }

}
