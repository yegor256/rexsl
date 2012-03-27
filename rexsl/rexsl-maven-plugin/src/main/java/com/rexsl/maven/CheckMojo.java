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
     * {@inheritDoc}
     */
    @Override
    protected void run() throws MojoFailureException {
        final long start = System.nanoTime();
        if (this.systemPropertyVariables != null) {
            this.injectVariables(this.systemPropertyVariables);
        }
        final ChecksProvider provider = new ChecksProvider();
        provider.setTest(this.test);
        final Set<Check> checks = provider.all();
        for (Check check : checks) {
            if (!check.validate(this.env())) {
                throw new MojoFailureException(
                    Logger.format(
                        "%s check failed",
                        check.getClass().getName()
                    )
                );
            }
        }
        Logger.info(
            this,
            "All ReXSL checks passed in %[nano]s",
            System.nanoTime() - start
        );
    }

    /**
     * Inject system property variables.
     * @param vars The variables to inject
     */
    private void injectVariables(final Map<String, String> vars) {
        for (Map.Entry<String, String> var : vars.entrySet()) {
            System.setProperty(var.getKey(), var.getValue());
            Logger.info(
                this,
                "System variable '%s' set to '%s'",
                var.getKey(),
                var.getValue()
            );
        }
    }

}
