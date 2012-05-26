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

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link com.rexsl.maven.checks.WebappFilesCheckTest}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id: WebappFilesCheckTest.java 1732 2012-05-24 20:36:50Z guard $
 */
public final class WebappFilesCheckTest {

    /**
     * WebappFilesCheckTest can validate certain file types
     * under {@code src/main/webapp} folder and it's subfolders.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesFileTypes() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/main/webapp/valid.html", "")
            .withTextFile("src/main/webapp/robots.txt", "")
            .withTextFile("src/main/webapp/WEB-INF/file.xml", "")
            .withTextFile("src/main/webapp/css/test.css", "")
            .withTextFile("src/main/webapp/js/file.js", "")
            .withTextFile("src/main/webapp/xsl/convert.xsl", "")
            .mock();
        MatcherAssert.assertThat(
            "valid file types passes without problems",
            new WebappFilesCheck().validate(env)
        );
    }

    /**
     * WebappFilesCheck can validate incorrect file type under
     * {@code src/main/webapp}.
     * @throws Exception If something goes wrong
     */
    @Test
    public void validatesIncorrectFileTypes() throws Exception {
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/main/webapp/js/invalid-type.jpg", "")
            .mock();
        MatcherAssert.assertThat(
            "invalid file type is caught",
            !new WebappFilesCheck().validate(env)
        );
    }
}
