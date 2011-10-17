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
package com.rexsl.test.client;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;

/**
 * Collection of headers.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see com.rexsl.test.TestClient#headers()
 */
public final class Headers {

    /**
     * Collection of headers.
     */
    private Header[] headers;

    /**
     * Public ctor.
     * @param hdrs List of headers
     */
    public Headers(final Header[] hdrs) {
        this.headers = hdrs;
    }

    /**
     * Do we have the header with this name?
     * @param name Header name
     * @return Yes or no
     */
    public boolean has(final String name) {
        for (Header header : this.headers) {
            if (header.getName().compareToIgnoreCase(name) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get first header with this name.
     * @param name Header name
     * @return This value
     */
    public String get(final String name) {
        for (Header header : this.headers) {
            if (header.getName().compareToIgnoreCase(name) == 0) {
                return header.getValue();
            }
        }
        throw new IllegalArgumentException(
            String.format(
                "Header %s not found",
                name
            )
        );
    }

    /**
     * Get all headers with this name.
     * @param name Header name
     * @return List of values
     */
    public List<String> all(final String name) {
        final List<String> values = new ArrayList<String>();
        for (Header header : this.headers) {
            if (header.getName().compareToIgnoreCase(name) == 0) {
                values.add(header.getValue());
            }
        }
        return values;
    }

}
