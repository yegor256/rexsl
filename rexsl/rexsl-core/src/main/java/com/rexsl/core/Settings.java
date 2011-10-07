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
package com.rexsl.core;

import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;

/**
 * ReXSL runtime settings.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class Settings {

    /**
     * Singleton instance.
     */
    public static final Settings INSTANCE = new Settings();

    /**
     * Separator of names.
     */
    private static final String COMMA = ",";

    /**
     * List of JAXB-ready packages to work with.
     */
    private List<String> packages = new ArrayList<String>();

    /**
     * List of URL prefixes to exclude from ReXSL processing.
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * Private ctor.
     */
    private Settings() {
        this.excludes.add("/robots.txt");
        this.excludes.add("/images/.*");
        this.excludes.add("/css/.*");
        this.excludes.add("/xsl/.*");
        this.excludes.add("/favicon.ico");
        this.packages.add(this.getClass().getPackage().getName());
    }

    /**
     * Configure settings from servlet context.
     * @param context The context
     */
    public void reset(final ServletContext context) {
        final List<String> names =
            Collections.list(context.getInitParameterNames());
        for (String name : names) {
            final String value = context.getInitParameter(name);
            if ("com.rexsl.PACKAGES".equals(name)) {
                this.packages.addAll(
                    Arrays.asList(StringUtils.split(value, this.COMMA))
                );
                Logger.info(
                    this,
                    "#reset(): '%s' packages added",
                    value
                );
                continue;
            }
            if ("com.rexsl.EXCLUDES".equals(name)) {
                this.excludes.addAll(
                    Arrays.asList(StringUtils.split(value, this.COMMA))
                );
                Logger.info(
                    this,
                    "#reset(): '%s' exclude regular expressions added",
                    value
                );
                continue;
            }
        }
    }

    /**
     * Return all packages for JAX-RS processing.
     * @return The list of packages
     */
    public List<String> packages() {
        return this.packages;
    }

    /**
     * Return all excluding regular expressions.
     * @return The list of regexs
     */
    public List<String> excludes() {
        return this.excludes;
    }

}
