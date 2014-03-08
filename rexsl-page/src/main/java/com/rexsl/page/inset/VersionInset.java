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
package com.rexsl.page.inset;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rexsl.page.BasePage;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Page with a flash message (through cookie).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see BasePage
 * @see <a href="http://www.rexsl.com/rexsl-page/inset-version.html">How to version WAR packages</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "version", "revision", "date" })
@Loggable(Loggable.DEBUG)
public final class VersionInset implements Inset {

    /**
     * Version.
     */
    private final transient String version;

    /**
     * Revision.
     */
    private final transient String revision;

    /**
     * Date of release.
     */
    private final transient String date;

    /**
     * Public ctor.
     * @param ver Version of the product
     * @param rev Unique revision number
     * @param when Date of release
     */
    public VersionInset(@NotNull final String ver, @NotNull final String rev,
        @NotNull final String when) {
        this.version = ver;
        this.revision = rev;
        this.date = when;
    }

    @Override
    public void render(@NotNull final BasePage<?, ?> page,
        @NotNull final Response.ResponseBuilder builder) {
        page.append(
            new JaxbBundle("version", "")
                .add("name", this.version)
                .up()
                .add("revision", this.revision)
                .up()
                .add("date", this.date)
                .up()
        );
    }

}
