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
import javax.validation.constraints.NotNull;

/**
 * Groovy-generated exception.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class GroovyException extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7526FA78EEAA1470L;

    /**
     * Default public no-agrument ctor.
     */
    public GroovyException() {
        super();
    }

    /**
     * Public ctor.
     * @param cause The cause
     */
    public GroovyException(@NotNull final Throwable cause) {
        super(cause);
    }

    /**
     * Public ctor.
     * @param cause The cause (formatting string for {@code String#format()})
     * @param args Agruments for {@code String#format()}
     */
    public GroovyException(@NotNull final String cause, final Object... args) {
        super(GroovyException.toText(cause, args));
    }

    /**
     * To message.
     * @param cause The cause
     * @param args Agruments for Logger.format()
     * @return Compiled text
     */
    private static String toText(final String cause, final Object... args) {
        final String text;
        if (args.length > 0) {
            text = Logger.format(cause, args);
        } else {
            text = cause;
        }
        return text;
    }

}
