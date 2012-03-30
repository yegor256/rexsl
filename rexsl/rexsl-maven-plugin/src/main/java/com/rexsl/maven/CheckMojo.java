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

import com.rexsl.maven.checks.ChecksProvider;
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
     * @parameter
     * @since 0.3
     */
    @SuppressWarnings("PMD.LongVariable")
    private transient Map<String, String> systemPropertyVariables;

    /**
     * Scope of tests to check.
     * @parameter expression="${rexsl.test}"
     */
    @SuppressWarnings("PMD.ImmutableField")
    private transient String test = ".*";

    /**
     * Check to perform.
     * @parameter expression="${rexsl.check}"
     */
    @SuppressWarnings("PMD.ImmutableField")
    private transient String check;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void run() throws MojoFailureException {
        final long start = System.nanoTime();
        final Properties before = this.inject();
        final ChecksProvider provider = new ChecksProvider();
        provider.setTest(this.test);
        provider.setCheck(this.check);
        final Set<Check> checks = provider.all();
        try {
            for (Check chck : checks) {
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
