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

import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test case for {@link CoreListener} class.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CoreListener.class, JerseyModule.class })
public final class CoreListenerTest {

    /**
     * Let's test how it initialized this listener with context.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testInitializesWithListOfPackages() throws Exception {
        final Properties params = new Properties();
        final String value = "com.rexsl.test";
        params.put("com.rexsl.PACKAGES", value);
        final ServletContextListener listener = new CoreListener();
        listener.contextInitialized(this.event(params));
        MatcherAssert.assertThat(
            Settings.INSTANCE.packages(),
            Matchers.hasItem(value)
        );
    }

    /**
     * Let's test how it initialized this listener with names of excludes.
     * @throws Exception If something goes wrong
     */
    @Test
    public void testInitializesWithListOfExcludes() throws Exception {
        final Properties params = new Properties();
        final String value = "/rexsl/.*";
        params.put("com.rexsl.EXCLUDES", value);
        final ServletContextListener listener = new CoreListener();
        listener.contextInitialized(this.event(params));
        MatcherAssert.assertThat(
            Settings.INSTANCE.excludes(),
            Matchers.hasItem(value)
        );
    }

    /**
     * Create context event.
     * @param props Init params.
     * @return The event just created
     * @throws Exception If something goes wrong
     */
    private ServletContextEvent event(final Properties props) throws Exception {
        final ServletContextEvent event =
            Mockito.mock(ServletContextEvent.class);
        final ServletContext ctx = Mockito.mock(ServletContext.class);
        Mockito.doReturn(ctx).when(event).getServletContext();
        final Properties names = new Properties();
        Integer idx = 1;
        for (Object key : props.keySet()) {
            final String name = (String) key;
            names.put(idx, name);
            Mockito.doReturn(props.get(name)).when(ctx).getInitParameter(name);
            idx += 1;
        }
        Mockito.doReturn(names.elements()).when(ctx).getInitParameterNames();
        return event;
    }

}
