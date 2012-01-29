/**
 * Copyright (c) 2011, ReXSL.com
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementaiton of validation response.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class DefaultValidationResponse implements ValidationResponse {

    /**
     * Is it valid?
     */
    private final transient boolean ivalid;

    /**
     * Who validated it?
     */
    private final transient URI validator;

    /**
     * DOCTYPE of the document.
     */
    private final transient String type;

    /**
     * The encoding.
     */
    private final transient String encoding;

    /**
     * List of errors found.
     */
    private final transient List<Defect> ierrors = new ArrayList<Defect>();

    /**
     * List of warnings found.
     */
    private final transient List<Defect> iwarnings = new ArrayList<Defect>();

    /**
     * Public ctor.
     * @param val The document is valid?
     * @param server Who validated it?
     * @param tpe DOCTYPE of the document
     * @param enc Charset of the document
     * @checkstyle ParameterNumber (3 lines)
     */
    public DefaultValidationResponse(final boolean val, final URI server,
        final String tpe, final String enc) {
        this.ivalid = val;
        this.validator = server;
        this.type = tpe;
        this.encoding = enc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("Validity: %B\n", this.ivalid));
        text.append(String.format("Validator: \"%s\"\n", this.validator));
        text.append(String.format("DOCTYPE: \"%s\"\n", this.type));
        text.append(String.format("Charset: \"%s\"\n", this.encoding));
        text.append("Errors:\n").append(this.asText(this.ierrors));
        text.append("Warnings:\n").append(this.asText(this.iwarnings));
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean valid() {
        return this.ivalid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI checkedBy() {
        return this.validator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doctype() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String charset() {
        return this.encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Defect> errors() {
        return new ArrayList<Defect>(this.ierrors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Defect> warnings() {
        return new ArrayList<Defect>(this.iwarnings);
    }

    /**
     * Add error.
     * @param error The error to add
     */
    public void addError(final Defect error) {
        this.ierrors.add(error);
    }

    /**
     * Add warning.
     * @param warning The warning to add
     */
    public void addWarning(final Defect warning) {
        this.iwarnings.add(warning);
    }

    /**
     * Convert list of defects into string.
     * @param defects List of them
     * @return The text
     */
    private String asText(final List<Defect> defects) {
        final StringBuilder text = new StringBuilder();
        for (Defect defect : defects) {
            text.append("  ").append(defect.toString()).append("\n");
        }
        return text.toString();
    }

}
