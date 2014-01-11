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
package com.rexsl.w3c;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Default implementaiton of validation response.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@EqualsAndHashCode(
    of = { "ivalid", "validator", "type", "encoding", "ierrors", "iwarnings" }
)
@Loggable(Loggable.DEBUG)
final class DefaultValidationResponse implements ValidationResponse {

    /**
     * Is it valid?
     */
    private transient boolean ivalid;

    /**
     * Who validated it?
     */
    @NotNull
    private final transient URI validator;

    /**
     * DOCTYPE of the document.
     */
    @NotNull
    private final transient String type;

    /**
     * The encoding.
     */
    @NotNull
    private final transient Charset encoding;

    /**
     * Set of errors found.
     */
    private final transient Set<Defect> ierrors = new HashSet<Defect>();

    /**
     * Set of warnings found.
     */
    private final transient Set<Defect> iwarnings = new HashSet<Defect>();

    /**
     * Public ctor.
     * @param val The document is valid?
     * @param server Who validated it?
     * @param tpe DOCTYPE of the document
     * @param enc Charset of the document
     * @checkstyle ParameterNumber (3 lines)
     */
    DefaultValidationResponse(final boolean val,
        @NotNull final URI server, @NotNull final String tpe,
        @NotNull final Charset enc) {
        this.ivalid = val;
        this.validator = server;
        this.type = tpe;
        this.encoding = enc;
    }

    @Override
    public String toString() {
        return new StringBuilder(0)
            .append(Logger.format("Validity: %B\n", this.ivalid))
            .append(Logger.format("Validator: \"%s\"\n", this.validator))
            .append(Logger.format("DOCTYPE: \"%s\"\n", this.type))
            .append(Logger.format("Charset: \"%s\"\n", this.encoding))
            .append("Errors:\n").append(this.asText(this.ierrors))
            .append("Warnings:\n").append(this.asText(this.iwarnings))
            .toString();
    }

    @Override
    public boolean valid() {
        return this.ivalid;
    }

    @Override
    public URI checkedBy() {
        return this.validator;
    }

    @Override
    public String doctype() {
        return this.type;
    }

    @Override
    public Charset charset() {
        return this.encoding;
    }

    @Override
    public Set<Defect> errors() {
        return Collections.unmodifiableSet(this.ierrors);
    }

    @Override
    public Set<Defect> warnings() {
        return Collections.unmodifiableSet(this.iwarnings);
    }

    /**
     * Set validity flag.
     * @param flag The flag to set
     */
    public void setValid(final boolean flag) {
        this.ivalid = flag;
    }

    /**
     * Add error.
     * @param error The error to add
     */
    public void addError(@Valid final Defect error) {
        this.ierrors.add(error);
    }

    /**
     * Add warning.
     * @param warning The warning to add
     */
    public void addWarning(@Valid final Defect warning) {
        this.iwarnings.add(warning);
    }

    /**
     * Convert list of defects into string.
     * @param defects Set of them
     * @return The text
     */
    private String asText(final Set<Defect> defects) {
        final StringBuilder text = new StringBuilder(0);
        for (final Defect defect : defects) {
            text.append("  ").append(defect.toString()).append('\n');
        }
        return text.toString();
    }

}
