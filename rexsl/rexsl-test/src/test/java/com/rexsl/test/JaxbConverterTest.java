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
package com.rexsl.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;

/**
 * Test JAXB converter.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class JaxbConverterTest {

    /**
     * Test simple conversion.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testObjectToXmlConversion() throws Exception {
        final Object object = new Employee();
        Assert.assertThat(
            JaxbConverter.the(object),
            XmlMatchers.hasXPath("/employee/name[.='John Doe']")
        );
    }

    /**
     * Testing that this converter returns properly formatted string.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testToStringConversion() throws Exception {
        final Object object = new Employee();
        Assert.assertThat(
            JaxbConverter.the(object).toString(),
            Matchers.containsString("John")
        );
    }

    /**
     * Test dynamically extendable objects.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testDynamicallyExtendableObject() throws Exception {
        final Employee employee = new Employee();
        employee.inject(new Foo());
        Assert.assertThat(
            JaxbConverter.the(employee, Foo.class),
            XmlMatchers.hasXPath("/employee/injected/name")
        );
    }

    @XmlRootElement(name = "employee")
    @XmlAccessorType(XmlAccessType.NONE)
    private static final class Employee {
        /**
         * Injected object.
         */
        private Object injected = "some text";
        /**
         * Inject an object.
         * @param obj The object to inject
         */
        public void inject(final Object obj) {
            this.injected = obj;
        }
        /**
         * Injected object.
         * @return The object
         */
        @XmlElement
        public Object getInjected() {
            return this.injected;
        }
        /**
         * Returns a simple string.
         * @return The text
         */
        @XmlElement(name = "name")
        public String getName() {
            return "John Doe";
        }
    }

    @XmlType(name = "foo")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Foo {
        /**
         * Simple name.
         * @return The name
         */
        @XmlElement
        public String getName() {
            return "some foo name";
        }
    }

}
