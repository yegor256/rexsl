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
package com.rexsl.maven;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.checks.DefaultChecksProvider;
import com.rexsl.maven.utils.LoggingManager;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Test entire project against RESTful principles.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @goal check
 * @phase integration-test
 * @threadSafe
 */
public final class CheckMojo extends AbstractRexslMojo {

    /**
     * System variables to be set before running tests.
     *
     * <p>You define them in a similar way as in
     * <a href="http://maven.apache.org/plugins/maven-surefire-plugin/"
     * >maven-surefire-plugin</a>,
     * for example you want to reconfigure LOG4J just for the tests:
     *
     * <pre>
     * &lt;plugin>
     *   &lt;groupId&gt;com.rexsl&lt;/groupId&gt;
     *   &lt;artifactId&gt;rexsl-maven-plugin&lt;/artifactId&gt;
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
     * Skip all tests.
     *
     * @parameter property="skipTests" default-value="false"
     * @since 0.3.8
     */
    private transient boolean skipTests;

    /**
     * Regular expression that determines tests ({@code groovy},
     * {@code xml}, etc.) to be executed during test.
     *
     * @parameter property="rexsl.test" default-value=".*"
     * @since 0.3.6
     */
    private transient String test = ".*";

    /**
     * Name of the check class to execute (if not set -
     * all checks are executed).
     *
     * <p>Set it to some irrelevant value in order to see a full list of
     * available checks, for example: {@code mvn rexsl:check -Drexsl.check=foo}.
     *
     * @parameter property="rexsl.check"
     * @since 0.3.6
     */
    private transient String check;

    /**
     * Checks provider.
     */
    private transient ChecksProvider provider = new DefaultChecksProvider();

    /**
     * Set checks provider.
     * @param prov The provider to set
     */
    @Loggable(Loggable.DEBUG)
    public void setChecksProvider(final ChecksProvider prov) {
        this.provider = prov;
    }

    /**
     * Set name of check to run.
     * @param chk Name of check (short class name)
     */
    @Loggable(Loggable.DEBUG)
    public void setCheck(final String chk) {
        this.check = chk;
    }

    /**
     * Set name of test to run.
     * @param tst Name of test
     */
    @Loggable(Loggable.DEBUG)
    public void setTest(final String tst) {
        this.test = tst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void run() throws MojoFailureException {
        final long start = System.currentTimeMillis();
        final Properties before = this.inject();
        this.provider.setTest(this.test);
        if (this.check != null) {
            this.provider.setCheck(this.check);
        }
        final Set<Check> checks = this.provider.all();
        try {
            for (Check chck : checks) {
                if (this.skipTests) {
                    Logger.warn(
                        this,
                        "%[type]s skipped because of skipTests",
                        chck
                    );
                    continue;
                }
                LoggingManager.enter(chck.getClass().getSimpleName());
                try {
                    this.single(chck);
                } finally {
                    LoggingManager.leave();
                }
            }
        } finally {
            this.revert(before);
        }
        Logger.info(
            this,
            "All ReXSL checks passed in %[ms]s",
            System.currentTimeMillis() - start
        );
    }

    /**
     * Run single test.
     * @param chck The check to run
     * @throws MojoFailureException If case of problem inside
     */
    private void single(final Check chck) throws MojoFailureException {
        Logger.info(this, "%[type]s running...", chck);
        final long start = System.currentTimeMillis();
        if (!chck.validate(this.env())) {
            throw new MojoFailureException(
                Logger.format(
                    "%s check failed in %[ms]s",
                    chck.getClass().getName(),
                    System.currentTimeMillis() - start
                )
            );
        }
        Logger.info(
            this,
            "%[type]s completed in %[ms]s",
            chck,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Sets the system properties to the argument passed.
     * @param before The properties.
     */
    private void revert(final Properties before) {
        System.setProperties(before);
    }

    /**
     * Injects system property variables and returns the properties as
     * they are before being modified in the method.
     * @return The properties before being modified
     */
    private Properties inject() {
        final Properties before = new Properties();
        before.putAll(System.getProperties());
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
        return before;
    }

}
