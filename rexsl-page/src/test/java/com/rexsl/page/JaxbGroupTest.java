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
package com.rexsl.page;

import com.jcabi.aspects.Parallel;
import com.jcabi.aspects.Tv;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link JaxbGroup}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JaxbGroupTest {

    /**
     * JaxbGroup can be converted to XML text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToXml() throws Exception {
        final Object group = JaxbGroup.build(
            Arrays.asList(
                new JaxbGroupTest.Dummy("e1"),
                new JaxbGroupTest.Dummy("e2")
            ),
            "group"
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(group),
            XhtmlMatchers.hasXPaths(
                "/group[count(dummy) = 2]",
                "/group/dummy[text='e1']",
                "/group/dummy[text='e2']"
            )
        );
    }

    /**
     * JaxbGroup can be converted to XML text, when empty.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToXmlWhenEmpty() throws Exception {
        final Object group = JaxbGroup.build(
            Collections.emptyList(), "group-2"
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(group),
            XhtmlMatchers.hasXPath("/group-2[count(*) = 0]")
        );
    }

    /**
     * JaxbGroup can make instances in multiple threads.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void instantiatesInMultipleThreads() throws Exception {
        final Runnable runnable = new Runnable() {
            @Override
            @Parallel(threads = Tv.TEN)
            public void run() {
                JaxbGroup.build(Collections.emptyList(), "group-A");
            }
        };
        runnable.run();
    }

    /**
     * Dummy element of collection.
     */
    @XmlRootElement
    public static final class Dummy {
        /**
         * The text.
         */
        private final transient String text;
        /**
         * Public ctor, for JAXB.
         */
        public Dummy() {
            throw new IllegalStateException("should not be called");
        }
        /**
         * Public ctor.
         * @param txt The text
         */
        public Dummy(final String txt) {
            this.text = txt;
        }
        /**
         * Get text.
         * @return The text
         */
        @XmlElement
        public String getText() {
            return this.text;
        }
    }

}
