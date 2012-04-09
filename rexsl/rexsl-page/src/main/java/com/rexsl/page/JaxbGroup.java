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
package com.rexsl.page;

import com.ymock.util.Logger;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * JAXB group of elements.
 *
 * <p>A convenient utility class that enables on-fly creation of JAXB annotated
 * collections of elements, for example:
 *
 * <pre>
 * &#64;XmlRootElement
 * public class Page {
 *   &#64;XmlElement
 *   public Object getEmployees() {
 *     Collection&lt;Employee&gt; employees = this.retrieve();
 *     return JaxbGroup.build(employees, "employee");
 *   }
 * }
 * </pre>
 *
 * <p>It's even more convenient in combination with {@link JaxbBundle}, for
 * example:
 *
 * <pre>
 * &#64;XmlRootElement
 * public class Page {
 *   &#64;XmlElement
 *   public Object getEmployee() {
 *     return new JaxbBundle("employee")
 *       .attr("age", "45")
 *       .add("depts", JaxbGroup.build(this.depts(), "dept"))
 *       .up()
 *       .element();
 *   }
 * }
 * </pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public final class JaxbGroup {

    /**
     * Classes already created before.
     */
    private static final ConcurrentMap<String, Class> READY =
        new ConcurrentHashMap<String, Class>();

    /**
     * Collection of elements.
     */
    private final transient Collection group;

    /**
     * Public ctor, for JAXB (always throws a runtime exception).
     */
    public JaxbGroup() {
        throw new IllegalStateException(
            // @checkstyle LineLength (1 line)
            "JaxbGroup can't be instantiated with default ctor, use JaxbGroup#build(..) instead"
        );
    }

    /**
     * Protected ctor, for on-fly instantiating (you're not supposed to use it).
     * @param grp Group of elements
     */
    protected JaxbGroup(final Collection grp) {
        this.group = grp;
    }

    /**
     * Creates a new JAXB-annotated collection of elements.
     * @param grp Group of elements (JAXB-annotated)
     * @param name Name of parent XML element
     * @return JAXB-annotated object, just created
     */
    public static Object build(final Collection grp, final String name) {
        synchronized (JaxbGroup.READY) {
            final String mnemo = JaxbGroup.mnemo(grp.isEmpty(), name);
            if (!JaxbGroup.READY.containsKey(mnemo)) {
                JaxbGroup.READY.put(
                    mnemo,
                    JaxbGroup.construct(JaxbGroup.types(grp), name)
                );
            }
            try {
                return JaxbGroup.READY.get(mnemo)
                    .getDeclaredConstructor(Collection.class)
                    .newInstance(grp);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            } catch (InstantiationException ex) {
                throw new IllegalStateException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Get group of elements.
     * @return The collection
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public Collection getGroup() {
        return this.group;
    }

    /**
     * Create NEW class name.
     * @param empty Is it an empty group?
     * @param name Name of root element
     * @return The name
     */
    private static String mnemo(final boolean empty, final String name) {
        return String.format(
            "%s$%s$%s",
            JaxbGroup.class.getName(),
            name,
            // @checkstyle AvoidInlineConditionals (1 line)
            empty ? "empty" : "full"
        );
    }

    /**
     * Construct new class.
     * @param types Types used in the collection
     * @param name Name of root element
     * @return Class just created
     */
    private static Class construct(final Collection<Class> types,
        final String name) {
        final ClassPool pool = ClassPool.getDefault();
        try {
            final CtClass ctc = pool.getAndRename(
                JaxbGroup.class.getName(),
                JaxbGroup.mnemo(types.isEmpty(), name)
            );
            final ClassFile file = ctc.getClassFile();
            final AnnotationsAttribute attribute =
                (AnnotationsAttribute) file.getAttribute(
                    AnnotationsAttribute.visibleTag
                );
            attribute.addAnnotation(JaxbGroup.xmlRootElement(file, name));
            if (!types.isEmpty()) {
                attribute.addAnnotation(JaxbGroup.xmlSeeAlso(file, types));
            }
            final Class cls = ctc.toClass();
            ctc.defrost();
            Logger.debug(
                JaxbGroup.class,
                "#construct('%s'): class %s constructed",
                name,
                cls.getName()
            );
            return cls;
        } catch (javassist.NotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (javassist.CannotCompileException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Find all types used in the collection.
     * @param group The collection
     * @return List of types used there
     */
    private static Collection<Class> types(final Collection group) {
        final Collection<Class> types = new HashSet<Class>();
        for (Object element : group) {
            types.add(element.getClass());
        }
        return types;
    }

    /**
     * Create new <tt>XmlRootElement</tt> annotation.
     * @param file Javassist file to work with
     * @param name Name of root element
     * @return The annotation
     */
    private static Annotation xmlRootElement(final ClassFile file,
        final String name) {
        final AnnotationsAttribute attribute =
            (AnnotationsAttribute) file.getAttribute(
                AnnotationsAttribute.visibleTag
            );
        final Annotation annotation = attribute.getAnnotation(
            XmlRootElement.class.getName()
        );
        annotation.addMemberValue(
            "name",
            new StringMemberValue(name, file.getConstPool())
        );
        Logger.debug(
            JaxbGroup.class,
            "#xmlRootElement(.., '%s'): annotation created",
            name
        );
        return annotation;
    }

    /**
     * Create new <tt>XmlSeeAlso</tt> annotation.
     * @param file Javassist file to work with
     * @param types The class to refer to
     * @return The annotation
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Annotation xmlSeeAlso(final ClassFile file,
        final Collection<Class> types) {
        final Annotation annotation = new Annotation(
            XmlSeeAlso.class.getName(),
            file.getConstPool()
        );
        final ArrayMemberValue member = new ArrayMemberValue(
            file.getConstPool()
        );
        final ClassMemberValue[] values = new ClassMemberValue[types.size()];
        int pos = 0;
        for (Class type : types) {
            values[pos] = new ClassMemberValue(
                type.getName(),
                file.getConstPool()
            );
            pos += 1;
        }
        member.setValue(values);
        annotation.addMemberValue("value", member);
        Logger.debug(
            JaxbGroup.class,
            "#xmlSeeAlso(.., %d classes): annotation created",
            types.size()
        );
        return annotation;
    }

}
