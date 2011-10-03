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

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Basic configuration module.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class JerseyModule extends JerseyServletModule {

    /**
     * Option to configure packages.
     */
    public static final String OPT_JSR311_PACKAGES = "rexsl.packages";

    /**
     * Separator between Jersey package names.
     */
    private static final String COMMA = ",";

    /**
     * Static files patterns.
     */
    private static final String STATIC =
        "robots.txt|images/|css/|xsl/|favicon.ico";

    /**
     * Init parameters.
     */
    private final Map<String, String> params;

    /**
     * Public ctor.
     * @param prms The params, provided by {@link CoreListener}
     */
    public JerseyModule(final Map<String, String> prms) {
        this.params = prms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureServlets() {
        // add XML-to-XHTML filter
        this.filterRegex(
            String.format("^/(?!%s).*$", this.STATIC)
        ).through(XslBrowserFilter.class);
        final Map<String, String> args = new HashMap<String, String>();
        args.put(
            "com.sun.jersey.config.property.packages",
            StringUtils.join(this.packages(), this.COMMA)
        );
        args.put(
            "com.sun.jersey.config.property.WebPageContentRegex",
            String.format("^/(%s).*$", this.STATIC)
        );
        args.put(
            "com.sun.jersey.config.feature.Redirect",
            Boolean.TRUE.toString()
        );
        args.put(
            "com.sun.jersey.config.feature.ImplicitViewables",
            Boolean.TRUE.toString()
        );
        this.filter("/*").through(GuiceContainer.class, args);
    }

    /**
     * Return a list of packages for Jersey.
     * @return The list (names)
     */
    private List<String> packages() {
        final String param = this.params.get(this.OPT_JSR311_PACKAGES);
        if (param == null) {
            throw new IllegalStateException(
                String.format(
                    "%s init-param is absent in web.xml",
                    this.OPT_JSR311_PACKAGES
                )
            );
        }
        final List<String> names = new ArrayList<String>();
        names.addAll(Arrays.asList(StringUtils.split(param, this.COMMA)));
        names.add(this.getClass().getPackage().getName());
        Logger.info(
            this,
            "#packages(): JSR311 packages: '%s'",
            StringUtils.join(names, this.COMMA)
        );
        return names;
    }

}
