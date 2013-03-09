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
package com.rexsl.maven.checks;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link RexslFilesCheck}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class RexslFilesCheckTest {

    /**
     * RexslFilesCheck can validate certain file types
     * under {@code src/test/rexsl} folder and it's subfolders.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesFileTypes() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/xml/valid-xml.xml", "")
            .withTextFile("src/test/rexsl/xhtml/script.groovy", "")
            .withTextFile("src/test/rexsl/setup/test.groovy", "")
            .withTextFile("src/test/rexsl/bootstrap/bootstrap.groovy", "")
            .withTextFile("src/test/rexsl/xsd/file.xsd", "")
            .withTextFile("src/test/rexsl/js/jsScript.js", "")
            .mock();
        MatcherAssert.assertThat(
            "valid file types passes without problems",
            new RexslFilesCheck().validate(env)
        );
    }

    /**
     * RexslFilesCheck can validate incorrect file type under
     * {@code src/test/rexsl}.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesIncorrectFileTypes() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/xml/invalid-type.jpg", "")
            .mock();
        MatcherAssert.assertThat(
            "invalid file type is caught",
            !new RexslFilesCheck().validate(env)
        );
    }

    /**
     * RexslFilesCheck can validate incorrect folder under src/test/rexsl.
     * @throws Exception If something goes wrong
     * @todo #171 This test is IGNORED because I don't think that we should
     *  prohibit users to create custom directories here. Let's just focus
     *  on file types.
     */
    @Test
    @org.junit.Ignore
    public void validatesIncorrectFolder() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/invalid-folder", "")
            .mock();
        MatcherAssert.assertThat(
            "invalid subfolder is caught",
            !new RexslFilesCheck().validate(env)
        );
    }

    /**
     * RexslFilesCheck can detect incorrect folder structure of src/test/rexsl.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesIncorrecStructure() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/xml/valid.xml", "")
            .withTextFile("src/test/rexsl/xhtml/rexsl/xml/script.xml", "")
            .withTextFile("src/test/rexsl/setup/setup.groovy", "")
            .withTextFile("src/test/rexsl/bootstrap/script.groovy", "")
            .mock();
        MatcherAssert.assertThat(
            "invalid structure is caught",
            !new RexslFilesCheck().validate(env)
        );
    }
}
