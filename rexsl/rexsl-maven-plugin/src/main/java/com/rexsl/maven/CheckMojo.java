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

import com.rexsl.maven.checks.DefaultChecksProvider;
import com.ymock.util.Logger;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Test entire project against RESTful principles.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @goal check
 * @phase integration-test
 * @threadSafe
 */
public final class CheckMojo extends AbstractRexslMojo {

    /**
     * System variables to be set before running tests.
     *
     * <p>You define them in a similar way as {@code maven-surefire-plugin}, for
     * example you want to reconfigure LOG4J just for the tests:
     *
     * <pre>
     * &lt;plugin>
     *   &lt;groupId&gt;com.rexsl&lt;/groupId&gt;
     *   &lt;artifactId&gt;rexsl-maven-plugin&lt;/artifactId&gt;
     *   &lt;version&gt;...&lt;/version&gt;
     *   &lt;configuration&gt;
     *     &lt;systemPropertyVariables&gt;
     *       &lt;log4j.configuration&gt;./x.props&lt;/log4j.configuration&gt;
     *     &lt;/systemPropertyVariables&gt;
     *   &lt;/configuration&gt;
     * &lt;/plugin&gt;
     * </pre>
     *
     * @parameter
     * @since 0.3
     */
    @SuppressWarnings("PMD.LongVariable")
    private transient Map<String, String> systemPropertyVariables;

    /**
     * Regular expression that determines tests ({@code groovy},
     * {@code xml}, etc.) to be executed during test.
     *
     * @parameter expression="${rexsl.test}" default-value=".*"
     * @since 0.3.6
     */
    @SuppressWarnings("PMD.ImmutableField")
    private transient String test = ".*";

    /**
     * Name of the check class to execute (if not set -
     * all checks are executed).
     * @parameter expression="${rexsl.check}"
     * @since 0.3.6
     */
    @SuppressWarnings("PMD.ImmutableField")
    private transient String check;

    /**
     * Checks provider.
     */
    private transient ChecksProvider provider = new DefaultChecksProvider();

    /**
     * Set checks provider.
     * @param prov The provider to set
     */
    public void setChecksProvider(final ChecksProvider prov) {
        this.provider = prov;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void run() throws MojoFailureException {
        final long start = System.nanoTime();
        final Properties before = this.inject();
        this.provider.setTest(this.test);
        this.provider.setCheck(this.check);
        final Set<Check> checks = this.provider.all();
        try {
            for (Check chck : checks) {
                Logger.info(this, "%[type]s running...", chck);
                if (!chck.validate(this.env())) {
                    throw new MojoFailureException(
                        Logger.format(
                            "%s check failed",
                            chck.getClass().getName()
                        )
                    );
                }
            }
        } finally {
            this.revert(before);
        }
        Logger.info(
            this,
            "All ReXSL checks passed in %[nano]s",
            System.nanoTime() - start
        );
    }

    /**
     * Sets the system properties to the argument passed.
     * @param properties The properties.
     */
    private void revert(final Properties properties) {
        System.setProperties(properties);
    }

    /**
     * Injects system property variables and returns the properties as
     * they are before being modified in the method.
     * @return The properties before being modified
     */
    private Properties inject() {
        final Properties systemProperties = new Properties();
        systemProperties.putAll(System.getProperties());
        if (this.systemPropertyVariables != null) {
            for (Map.Entry<String, String> var
                : this.systemPropertyVariables.entrySet()) {
                System.setProperty(var.getKey(), var.getValue());
                Logger.info(
                    this,
                    "System variable '%s' set to '%s'",
                    var.getKey(),
                    var.getValue()
                );
            }
        }
        return systemProperties;
    }

}
