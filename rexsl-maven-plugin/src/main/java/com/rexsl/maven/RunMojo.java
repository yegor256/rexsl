/**
 * Copyright (c) 2011-2014, ReXSL.com
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
import com.rexsl.maven.utils.EmbeddedContainer;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Run WAR as a web server to enable interactive testing of a web application.
 *
 * <p>Run {@code mvn clean package rexsl:run -Drexsl.port=9099} and open
 * your browser at {@code http://localhost:9099/}. You will see the front page
 * of your application. What is especially interesting is that you can make
 * changes to static resources ({@code xsl}, {@code css} and other files in
 * {@code src/main/webapp}) and then just refresh the page in a browser.
 * New versions of the files will be delivered. That's how you can interactively
 * test your XSL stylesheets.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Mojo(
    name = "run", defaultPhase = LifecyclePhase.INTEGRATION_TEST,
    threadSafe = true
)
public final class RunMojo extends AbstractRexslMojo {

    @Override
    protected void run() {
        this.env().setRuntimeFiltering(true);
        final EmbeddedContainer container = EmbeddedContainer.start(this.env());
        Logger.info(
            this,
            "Available at http://localhost:%d",
            this.env().port()
        );
        Logger.info(this, "Press Ctrl-C to stop...");
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException ex) {
                container.stop();
                Thread.currentThread().interrupt();
            }
        }
    }

}
