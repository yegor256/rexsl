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
package com.rexsl.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link StaticTemplate}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class StaticTemplateTest {
    /**
     * Text that replaces marker.
     */
    private static final String DEFECT = "A DEFECT TEXT";

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Should render template from local resource.
     */
    @Test
    public void rendersFromLocalResource() {
        MatcherAssert.assertThat(
            new StaticTemplate(URI.create("template.txt"))
                .render(StaticTemplateTest.DEFECT),
            Matchers.endsWith(StaticTemplateTest.DEFECT)
        );
    }

    /**
     * Should render template from file resource.
     * @throws IOException In case of error.
     */
    @Test
    public void rendersFromFileResource() throws IOException {
        final File tmp = this.temp.newFile();
        FileUtils.write(tmp, "Example text with ${text}");
        MatcherAssert.assertThat(
            new StaticTemplate(
                URI.create(String.format("file://%s", tmp.getAbsolutePath()))
            ).render(StaticTemplateTest.DEFECT),
            Matchers.endsWith(StaticTemplateTest.DEFECT)
        );
    }

    /**
     * Should render template from http resource.
     * @throws IOException In case of error.
     * @todo #760 Add a correct test for http schema - need to create a template
     *  file that will be always available on the web.
     */
    @Test
    @Ignore
    public void rendersFromHttpResource() throws IOException {
        MatcherAssert.assertThat(
            new StaticTemplate(URI.create("http://example.com"))
                .render(StaticTemplateTest.DEFECT),
            Matchers.endsWith(StaticTemplateTest.DEFECT)
        );
    }
}
