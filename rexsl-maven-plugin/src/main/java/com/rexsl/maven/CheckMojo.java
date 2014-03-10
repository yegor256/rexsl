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
import java.io.IOException;
import java.util.Collection;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Test the entire project against RESTful principles.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Mojo(
    name = "check", defaultPhase = LifecyclePhase.INTEGRATION_TEST,
    threadSafe = true
)
public final class CheckMojo extends AbstractRexslMojo {

    /**
     * Skip all tests.
     * @since 0.3.8
     */
    @Parameter(property = "skipTests", defaultValue = "false")
    private transient boolean skip;

    /**
     * Regular expression that determines tests ({@code groovy},
     * {@code xml}, etc.) to be executed during test.
     * @since 0.3.6
     */
    @Parameter(property = "rexsl.test", defaultValue = ".*")
    private transient String test = ".*";

    /**
     * Name of the check class to execute (if not set -
     * all checks are executed).
     *
     * <p>Set it to some irrelevant value in order to see a full list of
     * available checks, for example: {@code mvn rexsl:check -Drexsl.check=foo}.
     *
     * @since 0.3.6
     */
    @Parameter(property = "rexsl.check")
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

    @Override
    protected void run() throws MojoFailureException {
        final long start = System.currentTimeMillis();
        this.provider.setTest(this.test);
        if (this.check != null) {
            this.provider.setCheck(this.check);
        }
        final Collection<Check> checks = this.provider.all();
        for (final Check chck : checks) {
            if (this.skip) {
                Logger.warn(
                    this,
                    "%[type]s skipped because of skip",
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
        try {
            if (!chck.validate(this.env())) {
                throw new MojoFailureException(
                    Logger.format(
                        "%s check failed in %[ms]s",
                        chck.getClass().getName(),
                        System.currentTimeMillis() - start
                    )
                );
            }
        } catch (IOException ex) {
            throw new MojoFailureException("IO failure", ex);
        }
        Logger.info(
            this,
            "%[type]s completed in %[ms]s",
            chck,
            System.currentTimeMillis() - start
        );
    }

}
