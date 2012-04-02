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

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.utils.FileFinder;
import com.ymock.util.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Checks JS Unit tests.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
final class JSUnitTestsCheck implements Check {

    /**
     * Directory with JS test files.
     */
    private static final String JS_TESTS_DIR = "src/test/rexsl/js";

    /**
     * Wrapper of rhino unit script.
     */
    private static final String RHINO_WRAPPER = "rhinoUnitWrapper.js";

    /**
     * Rhino unit script.
     */
    private static final String RHINO_UNIT = "rhinoUnitUtil.js";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Environment env) {
        final File dir = new File(env.basedir(), this.JS_TESTS_DIR);
        boolean success = true;
        if (dir.exists()) {
            final Collection<File> files = new FileFinder(dir, "js").random();
            for (File file : files) {
                try {
                    success &= this.one(env.basedir(), file);
                } catch (InternalCheckException ex) {
                    Logger.warn(
                        this,
                        "Failed:\n%[exception]s",
                        ex
                    );
                    success = false;
                }
            }
        } else {
            Logger.info(
                this,
                "%s directory is absent, no JS unit tests",
                this.JS_TESTS_DIR
            );
        }
        return success;
    }

    /**
     * Check one script.
     * @param base Folder to check
     * @param file JS Unit test file to check
     * @throws InternalCheckException If some failure inside
     * @return Is js unit test succeeded?
     */
    private boolean one(final File base, final File file)
        throws InternalCheckException {
        final Reader reader = new BufferedReader(
            new InputStreamReader(
                this.getClass().getResourceAsStream(this.RHINO_WRAPPER)
            )
        );
        final ScriptEngineManager factory = new ScriptEngineManager();
        final ScriptEngine engine = factory.getEngineByName("JavaScript");
        try {
            engine.put(
                "rhinoUnitUtil",
                IOUtil.toString(
                    this.getClass().getResourceAsStream(this.RHINO_UNIT)
                )
            );
            engine.put("base", base.getAbsolutePath());
            try {
                engine.eval(reader);
            } finally {
                reader.close();
            }
            final Invocable invocable = (Invocable) engine;
            final Object res = invocable.invokeFunction(
                "runTest",
                file.getAbsolutePath()
            );
            for (String msg : res.toString().split("\n")) {
                Logger.error(this, msg);
            }
            return res.toString().isEmpty();
        } catch (NoSuchMethodException ex) {
            throw new InternalCheckException(ex);
        } catch (ScriptException ex) {
            throw new InternalCheckException(ex);
        } catch (IOException ex) {
            throw new InternalCheckException(ex);
        }
    }
}
