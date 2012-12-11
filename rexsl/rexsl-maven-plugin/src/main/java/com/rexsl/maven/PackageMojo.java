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
package com.rexsl.maven;

import com.jcabi.log.Logger;
import com.rexsl.maven.packers.PackersProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Package resources.
 *
 * <p>Text resources should be packed before they got delivered to the
 * {@code WAR} package. For example, comments should be removed from CSS and
 * XSL stylesheets, JavaScripts should be minified, etc. We have a number of
 * convenient packers that do this job, you just need to call them. First of
 * a {@code package} goal should be called:
 *
 * <pre>
 * &lt;plugin>
 *   &lt;groupId&gt;com.rexsl&lt;/groupId&gt;
 *   &lt;artifactId&gt;rexsl-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;...&lt;/version&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;phase&gt;&lt;phase&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;package&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <p>The goal will package all files it knows about and place their packaged
 * (minified) versions into {@code webappDirectory}. Then, you should configure
 * {@code maven-war-plugin} to not touch these files again (to avoid their
 * packaging):
 *
 * <pre>
 * &lt;plugin>
 *   &lt;artifactId&gt;maven-war-plugin&lt;/artifactId&gt;
 *   &lt;configuration&gt;
 *     &lt;warSourceExcludes&gt;js/**, css/**, xsl/**&lt;/warSourceExcludes&gt;
 *     [..]
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @goal package
 * @phase process-resources
 * @threadSafe
 */
public final class PackageMojo extends AbstractRexslMojo {

    /**
     * Static resources (CSS, XSL, JS, etc.) should be filtered before
     * packaging.
     *
     * <p>At the moment only properties defined in {@code pom/properties}
     * section are supported.
     *
     * @parameter property="rexsl.filtering"
     * @since 0.3.7
     * @see <a href="http://trac.rexsl.com/rexsl/ticket/342">Introduced in ticket #342</a>
     */
    private transient boolean filtering;

    /**
     * Set filtering.
     * @param fltr Shall we filter before packaging?
     */
    public void setFiltering(final boolean fltr) {
        this.filtering = fltr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void run() {
        Filter filter;
        if (this.filtering) {
            filter = new Filter() {
                @Override
                public Reader filter(final File file) throws IOException {
                    return new InputStreamReader(
                        new FileInputStream(file),
                        CharEncoding.UTF_8
                    );
                }
            };
        } else {
            filter = new PackageMojo.PropsFilter();
        }
        final long start = System.currentTimeMillis();
        final Set<Packer> packers = new PackersProvider().all();
        for (Packer packer : packers) {
            packer.pack(this.env(), filter);
        }
        Logger.info(
            this,
            "Packaging finished in %[ms]s",
            System.currentTimeMillis() - start
        );
    }

    /**
     * Filtering using project properties.
     * @todo #342 Only properties from PROPERTIES section in pom.xml are
     *  supported. I don't know how to get access to all other project props.
     */
    private final class PropsFilter implements Filter {
        /**
         * {@inheritDoc}
         */
        @Override
        public Reader filter(final File file) throws IOException {
            final String origin = FileUtils.readFileToString(file);
            final StringBuffer text = new StringBuffer();
            final Matcher matcher = Pattern.compile("\\$\\{([\\w\\.\\-]+)\\}")
                .matcher(origin);
            final Properties props = PackageMojo.this.project().getProperties();
            Logger.debug(
                this,
                "#filter(..): all properties available %[list]s",
                props.keySet()
            );
            while (matcher.find()) {
                final String name = matcher.group(1);
                String replacer;
                if (props.containsKey(name)) {
                    replacer = props.get(name).toString();
                    Logger.info(
                        this,
                        "'%s' replaced with '%s' in %s",
                        matcher.group(),
                        replacer,
                        file
                    );
                } else {
                    replacer = matcher.group();
                    Logger.warn(
                        this,
                        "'%s' can't be replaced in %s (not found in pom.xml)",
                        matcher.group(),
                        file
                    );
                }
                matcher.appendReplacement(
                    text,
                    Matcher.quoteReplacement(replacer)
                );
            }
            matcher.appendTail(text);
            return new StringReader(text.toString());
        }
    }

}
