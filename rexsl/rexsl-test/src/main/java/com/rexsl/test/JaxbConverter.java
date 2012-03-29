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
package com.rexsl.test;

import com.ymock.util.Logger;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

/**
 * JAXB-empowered object to XML converting utility.
 *
 * <p>The object has to be annotated with JAXB annotations
 * in order to be convertable.
 * Let's consider an example JAXB-annotated class:
 *
 * <pre>
 * import javax.xml.bind.annotation.XmlAccessType;
 * import javax.xml.bind.annotation.XmlAccessorType;
 * import javax.xml.bind.annotation.XmlElement;
 * import javax.xml.bind.annotation.XmlRootElement;
 * &#64;XmlRootElement(name = "employee")
 * &#64;XmlAccessorType(XmlAccessType.NONE)
 * private static final class Employee {
 *   &#64;XmlElement(name = "name")
 *   public String getName() {
 *     return "John Doe";
 *   }
 * }
 * </pre>
 *
 * <p>Now you want to test how it works with real data after convertion
 * to XML (in a unit test):
 *
 * <pre>
 * import com.rexsl.test.JaxbConverter;
 * import com.rexsl.test.XhtmlMatchers;
 * import org.junit.Assert;
 * import org.junit.Test;
 * public final class EmployeeTest {
 *   &#64;Test
 *   public void testObjectToXmlConversion() throws Exception {
 *     final Object object = new Employee();
 *     Assert.assertThat(
 *       JaxbConverter.the(object),
 *       XhtmlMatchers.hasXPath("/employee/name[.='John Doe']")
 *     );
 *   }
 * }
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class JaxbConverter {

    /**
     * Private ctor, to avoid direct instantiation of the class.
     */
    private JaxbConverter() {
        // intentionally empty
    }

    /**
     * Convert an object to XML.
     * @param object The object to convert
     * @param deps Dependencies that we should take into account
     * @return DOM source/document
     */
    public static Source the(final Object object, final Class... deps) {
        final Class[] classes = new Class[deps.length + 1];
        classes[0] = object.getClass();
        System.arraycopy(deps, 0, classes, 1, deps.length);
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(classes);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        final JAXBIntrospector intro = ctx.createJAXBIntrospector();
        Object subject = object;
        if (intro.getElementName(object) == null) {
            subject = new JAXBElement(
                JaxbConverter.qname(object),
                object.getClass(),
                object
            );
        }
        final Marshaller mrsh = JaxbConverter.marshaller(ctx);
        final StringWriter writer = new StringWriter();
        try {
            mrsh.marshal(subject, writer);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        final String xml = writer.toString();
        return new StringSource(xml);
    }

    /**
     * Create marshaller.
     * @param ctx The context
     * @return Marshaller
     */
    private static Marshaller marshaller(final JAXBContext ctx) {
        Marshaller mrsh;
        try {
            mrsh = ctx.createMarshaller();
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            mrsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (javax.xml.bind.PropertyException ex) {
            throw new IllegalStateException(ex);
        }
        return mrsh;
    }

    /**
     * Get type name, if XmlType annotation is present (exception otherwise).
     * @param obj The object
     * @return Qualified name
     * @see XmlElement#namespace()
     */
    private static QName qname(final Object obj) {
        final XmlType type = (XmlType) obj.getClass()
            .getAnnotation(XmlType.class);
        if (type == null) {
            throw new IllegalArgumentException(
                Logger.format(
                    "XmlType annotation is absent at %[type]s",
                    obj
                )
            );
        }
        QName qname;
        if ("##default".equals(type.namespace())) {
            qname = new QName(type.name());
        } else {
            qname = new QName(type.namespace(), type.name());
        }
        return qname;
    }

}
