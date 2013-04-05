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

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * LoggingManager test.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id: LoggingManagerTest.java 2012-04-16$
 */
public final class LoggingManagerTest {

    /**
     * LoggingManager switches configured appender's filename.
     * @throws IOException if IO problem inside
     */
    @Test
    @Ignore
    public void enter() throws IOException {
        LoggingManager.enter("LoggingManagerTest");
        Logger.info(this, "Hello, world!");
        LoggingManager.leave();
        final File file = new File("LoggingManagerTest.log");
        MatcherAssert.assertThat(
            FileUtils.readFileToString(file),
            Matchers.containsString("Hello")
        );
    }

    /**
     * LoggingManager restores configured appender's filename to value before
     * enter() call.
     * @throws IOException if IO problem inside
     */
    @Test
    @Ignore
    public void leave() throws IOException {
        LoggingManager.enter("LoggingManagerLeave");
        Logger.info(this, "Message inside scope");
        LoggingManager.leave();
        Logger.info(this, "Hello, World!");
        final File file = new File("LoggingManagerLeave.log");
        MatcherAssert.assertThat(
            FileUtils.readFileToString(file),
            Matchers.not(Matchers.containsString("World"))
        );
    }
}
