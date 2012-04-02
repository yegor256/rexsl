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

import com.google.common.io.Files;
import com.rexsl.maven.utils.PortReserver;
import com.ymock.util.Logger;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

/**
 * Mocker of {@link Environment}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class EnvironmentMocker {

    /**
     * The mock.
     */
    private final transient Environment env = Mockito.mock(Environment.class);

    /**
     * Temporary folder.
     */
    private final transient File basedir;

    /**
     * Public ctor.
     */
    public EnvironmentMocker() {
        new LogMocker().mock();
        final File temp = Files.createTempDir();
        try {
            FileUtils.forceDeleteOnExit(temp);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        this.basedir = new File(temp, "basedir");
        this.basedir.mkdirs();
        Mockito.doReturn(this.basedir).when(this.env).basedir();
        final File webdir = new File(this.basedir, "target/webdir");
        webdir.mkdirs();
        Mockito.doReturn(webdir).when(this.env).webdir();
        Mockito.doReturn(new PortReserver().port()).when(this.env).port();
    }

    /**
     * With this file in basedir.
     * @param name File name
     * @return This object
     */
    public EnvironmentMocker withFile(final String name) {
        return this.withFile(name, name.substring(name.lastIndexOf('/') + 1));
    }

    /**
     * With this file in basedir.
     * @param name File name
     * @param res The resource to use
     * @return This object
     */
    public EnvironmentMocker withFile(final String name, final String res) {
        final InputStream stream = this.getClass().getResourceAsStream(res);
        if (stream == null) {
            throw new IllegalArgumentException(
                Logger.format("resource '%s' not found", res)
            );
        }
        try {
            return this.withTextFile(name, IOUtils.toString(stream));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * With this file in basedir.
     * @param name File name
     * @param txt Content of the file to save
     * @return This object
     */
    public EnvironmentMocker withTextFile(final String name, final String txt) {
        final File dest = new File(this.basedir, name);
        dest.getParentFile().mkdirs();
        try {
            FileUtils.writeStringToFile(dest, txt);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return this;
    }

    /**
     * With this folder in basedir.
     * @param name File name
     * @return This object
     */
    public EnvironmentMocker withFolder(final String name) {
        final File dest = new File(this.basedir, name);
        dest.mkdirs();
        return this;
    }

    /**
     * With default classpath.
     * @return This object
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public EnvironmentMocker withDefaultClasspath() {
        final Set<File> files = new HashSet<File>();
        for (String file : System.getProperty("java.class.path")
            .split(System.getProperty("path.separator"))) {
            files.add(new File(file));
        }
        Mockito.doReturn(files).when(this.env).classpath(Mockito.anyBoolean());
        return this;
    }

    /**
     * Mock it.
     * @return Mocked environment
     */
    public Environment mock() {
        return this.env;
    }

}
