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
package com.rexsl.maven;

import java.io.File;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link PackageMojo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class PackageMojoTest {

    /**
     * PackageMojo can't work with non-WAR projects.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalStateException.class)
    public void testNonWarPackaging() throws Exception {
        final PackageMojo mojo = new PackageMojo();
        final MavenProject project = new MavenProjectMocker()
            .withPackaging("jar")
            .mock();
        mojo.setProject(project);
        mojo.execute();
    }

    /**
     * PackageMojo can package artifacts normally.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void packsArtifactsNormally() throws Exception {
        final Environment env = new EnvironmentMocker().mock();
        final PackageMojo mojo = new PackageMojo();
        mojo.setWebappDirectory(env.webdir().getAbsolutePath());
        final MavenProject project = new MavenProjectMocker().mock();
        final Log log = Mockito.mock(Log.class);
        mojo.setLog(log);
        mojo.setProject(project);
        mojo.execute();
    }

    /**
     * PackageMojo can filter meta commands.
     * @todo #529 Would be better to implement filtering in method under test
     *  with custom Reader. Current implementation loads the entire file
     *  into memory, which may be a problem if the file is too big.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void canFilterMetaCommands() throws Exception {
        final String input = " "
            // @checkstyle LineLength (24 lines)
            // @checkstyle StringLiteralsConcatenationCheck (23 lines)
            + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
            + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + "    xmlns=\"http://www.w3.org/1999/xhtml\""
            + "    xmlns:xhtml=\"http://www.w3.org/1999/xhtml\""
            + "    version=\"2.0\" exclude-result-prefixes=\"xs xsl xhtml\">"
            + "    <xsl:output method=\"xhtml\""
            + "        doctype-system=\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\""
            + "        doctype-public=\"-//W3C//DTD XHTML 1.0 Strict//EN\" />"
            + "    <xsl:template match=\"/\">"
            + "        <html xml:lang=\"en\">"
            + "            <head>"
            + "                <link href=\"/css/screen.css\" rel=\"stylesheet\" type=\"text/css\"/>"
            + "                <title>hello!</title>"
            + "            </head>"
            + "            <body>"
            + "                <div id=\"content\">"
            + "                    <xsl:call-template name=\"content\" />"
            + "                </div>"
            + "                <div>${project.version}</div>"
            + "            </body>"
            + "        </html>"
            + "    </xsl:template>"
            + "</xsl:stylesheet>";
        final String version = "1.0-SNAPSHOT";
        final Environment env = new EnvironmentMocker()
            .withTextFile("src/main/webapp/xsl/test.xsl", input)
            .mock();
        final PackageMojo mojo = new PackageMojo();
        mojo.setWebappDirectory(env.webdir().getAbsolutePath());
        final Properties props = new Properties();
        props.put("project.version", version);
        final MavenProject project = new MavenProjectMocker()
            .withBasedir(env.basedir())
            .withProperties(props)
            .mock();
        final Log log = Mockito.mock(Log.class);
        mojo.setLog(log);
        mojo.setProject(project);
        mojo.execute();
        final File output = new File(
            project.getBasedir(),
            "target/webdir/xsl/test.xsl"
        );
        final String actual = FileUtils.readFileToString(output);
        MatcherAssert.assertThat(
            actual.contains(version),
            Matchers.equalTo(true)
        );
    }
}
