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
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import groovy.lang.Binding;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link GroovyExecutor}.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
public final class GroovyExecutorTest {

    /**
     * GroovyExecutor can execute a simple groovy script.
     * @throws Exception If something goes wrong
     */
    @Test
    public void executesSimpleGroovyScript() throws Exception {
        final String name = "src/test.groovy";
        final Environment env = new EnvironmentMocker()
            .withTextFile(name, "builder.append('hello, world!')")
            .mock();
        final Binding binding = new BindingBuilder(env).build();
        final StringBuilder builder = new StringBuilder();
        binding.setVariable("builder", builder);
        final GroovyExecutor exec = new GroovyExecutor(env, binding);
        exec.execute(new File(env.basedir(), name));
        MatcherAssert.assertThat(
            builder.toString(),
            Matchers.equalTo("hello, world!")
        );
    }

    /**
     * GroovyExecutor can work with explicitly provided classloader.
     * @throws Exception If something goes wrong
     */
    @Test
    public void worksWithSpecifiedClassloader() throws Exception {
        final String name = "src/foo.groovy";
        final Environment env = new EnvironmentMocker()
            .withTextFile(name, "bldr.append('hi there!')")
            .mock();
        final Binding binding = new BindingBuilder(env).build();
        final StringBuilder builder = new StringBuilder();
        binding.setVariable("bldr", builder);
        final GroovyExecutor exec = new GroovyExecutor(
            Thread.currentThread().getContextClassLoader(),
            binding
        );
        exec.execute(new File(env.basedir(), name));
        MatcherAssert.assertThat(
            builder.toString(),
            Matchers.equalTo("hi there!")
        );
    }

}
