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

import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;

/**
 * Collection of headers.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @see com.rexsl.test.TestClient#getHeaders()
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
    public Boolean has(final String name) {
        Boolean found = false;
        for (Header header : this.headers) {
            if (header.getName().compareToIgnoreCase(name) == 0) {
                found = true;
            }
        }
        Logger.info(
            this,
            "#has(%s): %s among '%s'",
            name,
            found.toString(),
            this.summary()
        );
        return found;
    }

    /**
     * Get first header with this name.
     * @param name Header name
     * @return This value
     */
    public String get(final String name) {
        for (Header header : this.headers) {
            if (header.getName().compareToIgnoreCase(name) == 0) {
                Logger.info(
                    this,
                    "#get(%s): '%s' found among '%s'",
                    name,
                    header.getValue(),
                    this.summary()
                );
                return header.getValue();
            }
        }
        throw new IllegalArgumentException(
            String.format(
                "Header %s not found in '%s'",
                name,
                this.summary()
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
        Logger.info(
            this,
            "#all(%s): %d found among '%s'",
            name,
            values.size(),
            this.summary()
        );
        return values;
    }

    /**
     * Create a text summary of all headers here.
     * @return The summary
     */
    private String summary() {
        final List<String> lines = new ArrayList<String>();
        for (Header header : this.headers) {
            lines.add(
                String.format("%s=%s", header.getName(), header.getValue())
            );
        }
        return StringUtils.join(lines, ";");
    }

}
