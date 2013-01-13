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
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import java.io.File;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * FileFinder test.
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id$
 */
public final class FileFinderTest {

    /**
     * Test string.
     */
    private static final String TEST = "assert true";

    /**
     * Checks alphabetical order.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testOrdered() throws Exception {
        final Environment environment = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/setup/g.groovy", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/f.groovy", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/a.groovy", FileFinderTest.TEST)
            .mock();
        final File directory = environment.basedir();
        final FileFinder finder = new FileFinder(directory, "groovy");
        final Collection<File> files = finder.ordered();
        String previous = "";
        for (File file : files) {
            final String name = file.getName();
            final int result = name.compareTo(previous);
            MatcherAssert.assertThat(
                "order",
                result,
                Matchers.greaterThanOrEqualTo(0)
            );
            previous = name;
        }
    }

    /**
     * Checks random order.
     */
    @Test
    public void testRandom() {
        final Environment environment = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/setup/k.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/b.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/c.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/d.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/e.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/l.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/m.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/h.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/i.xml", FileFinderTest.TEST)
            .mock();
        final File directory = environment.basedir();
        final FileFinder finder = new FileFinder(directory, "xml");
        MatcherAssert.assertThat(
            "random",
            this.namesHash(finder.random()),
            Matchers.anyOf(
                Matchers.not(this.namesHash(finder.random())),
                Matchers.not(this.namesHash(finder.random()))
            )
        );
    }

    /**
     * Checks certain type.
     * @throws Exception .
     */
    @Test
    public void testCertainType() throws Exception {
        final Environment environment = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/setup/n.css", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/o.xml", FileFinderTest.TEST)
            .withTextFile("src/test/rexsl/setup/p.css", FileFinderTest.TEST)
            .mock();
        final File directory = environment.basedir();
        final Collection<File> files =
            new FileFinder(directory, "css").ordered();
        for (File file : files) {
            MatcherAssert.assertThat(
                "file type",
                file.getName(),
                Matchers.endsWith(".css")
            );
        }
    }

    /**
     * Concatenates file names into one string.
     * @param files Collection of files to concatenate names
     * @return Concatenated file names
     */
    private String namesHash(final Collection<File> files) {
        final StringBuilder builder = new StringBuilder();
        for (File file : files) {
            builder.append(file.getName());
        }
        return builder.toString();
    }
}
