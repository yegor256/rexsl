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

import com.jcabi.log.Logger;
import com.rexsl.core.annotations.Schema;
import com.rexsl.core.annotations.Stylesheet;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlType;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

/**
 * Page builder.
 *
 * <p>First, you should create a base class, which will be used by all pages.
 * (they all will inherit it). The base class should be JAXB annotated, for
 * example:
 *
 * <pre> &#64;XmlRootElement(name = "page")
 * public class MyPage extends BasePage {
 *   public String title;
 *   public BasePage title(String text) {
 *     this.title = text;
 *   }
 *   &#64;XmlElement
 *   public String getTitle() {
 *     return this.title;
 *   }
 * }</pre>
 *
 * <p>Then, in JAX-RS resource you instantiate and extend this class with
 * application/request specific content (we think that suffix {@code Rs}
 * is a good notation for all JAX-RS resources):
 *
 * <pre> &#64;Path("/")
 * public class MainRs {
 *   &#64;GET
 *   &#64;Produces(MediaTypes.APPLICATION_XML)
 *   public MyPage front() {
 *     return new PageBuilder()
 *       .stylesheet("/xsl/front.xsl")
 *       .build(MyPage.class)
 *       .append(new JaxbBundle("score", 2))
 *       .title("Hello, world!");
 *   }
 * }</pre>
 *
 * <p>That's it. The XML produced by JAX-RS and JAXB will look like:
 *
 * <pre> &lt;?xml version="1.0" ?&gt;
 * &lt;?xml-stylesheet type='text/xsl' href='/xsl/front.xsl'?&gt;
 * &lt;page&gt;
 *   &lt;score&gt;2&lt;/score&gt;
 *   &lt;title&gt;Hello, world!&lt;/title&gt;
 * &lt;/page&gt;</pre>
 *
 * <p>We recommend to extend {@link BasePage}, since it already implements
 * a few most popular and necessary methods and properties.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.7
 * @see BasePage
 * @see JaxbGroup
 * @see JaxbBundle
 */
@ToString
public final class PageBuilder {

    /**
     * Static initialization of Javassist.
     */
    static {
        ClassPool.getDefault().insertClassPath(
            new ClassClassPath(PageBuilder.class)
        );
        Logger.debug(
            PageBuilder.class,
            "#PageBuilder(): javassist initialized"
        );
    }

    /**
     * Stylesheet to use.
     */
    @NotNull
    private transient URI xsl = UriBuilder.fromUri("/xsl/none.xsl").build();

    /**
     * Schema to use.
     */
    @NotNull
    private transient String xsd = "";

    /**
     * Configure the stylesheet to be used.
     * @param uri The URI of the stylesheet
     * @return This object
     */
    @NotNull
    public PageBuilder stylesheet(@NotNull final String uri) {
        this.xsl = UriBuilder.fromUri(uri)
            .replaceQuery("")
            .build();
        return this;
    }

    /**
     * Configure the schema to be used.
     * @param name Name of schema
     * @return This object
     */
    @NotNull
    public PageBuilder schema(@NotNull final String name) {
        this.xsd = name;
        return this;
    }

    /**
     * Create new class.
     * @param base Parent class, which will be inherited
     * @return The instance of the class just created
     * @param <T> The type of result expected
     */
    @NotNull
    public <T> T build(@NotNull final Class<T> base) {
        T page;
        try {
            page = base.cast(this.createOrFind(base).newInstance());
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.debug(
            PageBuilder.class,
            // @checkstyle LineLength (1 line)
            "#build(%s): page of class %[type]s created",
            base.getName(),
            page
        );
        return page;
    }

    /**
     * Create and return a new class for the given stylesheet, or find an
     * existing one and return it.
     * @param base Parent class, which will be inherited
     * @return The class just created or found
     */
    private Class<?> createOrFind(final Class<?> base) {
        synchronized (PageBuilder.class) {
            final String name = String.format(
                "%s$%s",
                base.getName(),
                this.xsl.getPath().replaceAll("[^a-zA-Z0-9]", "")
            );
            Class<?> cls;
            if (ClassPool.getDefault().getOrNull(name) == null) {
                cls = this.construct(name, base);
            } else {
                try {
                    cls = Class.forName(name);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException(ex);
                }
                final Stylesheet sheet = cls.getAnnotation(Stylesheet.class);
                if (sheet == null) {
                    throw new IllegalStateException(
                        String.format(
                            "Class %s doesn't have Stylesheet annotation",
                            cls.getName()
                        )
                    );
                }
                if (!sheet.value().equals(this.xsl.toString())) {
                    throw new IllegalStateException(
                        String.format(
                            "Class %s has '%s' stylesheet while %s expected",
                            cls.getName(),
                            sheet.value(),
                            this.xsl.toString()
                        )
                    );
                }
            }
            return cls;
        }
    }

    /**
     * Construct a new class with given name.
     * @param name The name of the class to construct
     * @param base Parent class, which will be inherited
     * @return The class just created
     */
    private Class<?> construct(final String name, final Class<?> base) {
        if (base.isAnnotationPresent(XmlType.class)) {
            throw new IllegalStateException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Class %s can't be annotated with @XmlType, use @XmlRootElement instead",
                    base.getName()
                )
            );
        }
        final ClassPool pool = ClassPool.getDefault();
        try {
            final CtClass parent = pool.get(base.getName());
            final CtClass ctc = pool.makeClass(name, parent);
            final ClassFile file = ctc.getClassFile();
            final AnnotationsAttribute attribute = new AnnotationsAttribute(
                file.getConstPool(),
                AnnotationsAttribute.visibleTag
            );
            new PageBuilder.Annotations(file, attribute)
                .append(XmlType.class, "name", name)
                .append(Stylesheet.class, this.xsl.toString())
                .append(Schema.class, this.xsd);
            for (Annotation existing : this.annotations(ctc, parent)) {
                attribute.addAnnotation(existing);
            }
            file.addAttribute(attribute);
            final Class<?> cls = ctc.toClass();
            Logger.debug(
                this,
                "#construct('%s', %s): class %s created",
                name,
                base.getName(),
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
     * Get list of existing annotations. Maybe we should filter out some of
     * them and not copy to the new class, I don't know.
     * @param dest Destination class, where these annotations will be used
     * @param parent Parent class, which will be inherited
     * @return The list of them
     */
    private Collection<Annotation> annotations(final CtClass dest,
        final CtClass parent) {
        final Collection<Annotation> result = new ArrayList<Annotation>();
        final AttributeInfo originals =
            parent.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if (originals != null) {
            final AnnotationsAttribute attrib = AnnotationsAttribute.class.cast(
                originals.copy(
                    dest.getClassFile().getConstPool(),
                    new HashMap<Object, Object>()
                )
            );
            final Annotation[] all = attrib.getAnnotations();
            final List<String> names = new ArrayList<String>();
            for (Annotation annotation : all) {
                result.add(annotation);
                names.add(annotation.getTypeName());
            }
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#annotations(%s, %s): %d found in base class, %d of them are copied: %s",
                dest.getName(),
                parent.getName(),
                all.length,
                result.size(),
                StringUtils.join(names, ", ")
            );
        }
        return result;
    }

    /**
     * Annotations maker.
     */
    private static final class Annotations {
        /**
         * File to work with.
         */
        private final transient ClassFile file;
        /**
         * Document me...
         */
        private final transient AnnotationsAttribute attribute;
        /**
         * Public ctor.
         * @param fle Class file
         * @param attr Attribute
         */
        public Annotations(final ClassFile fle,
            final AnnotationsAttribute attr) {
            this.file = fle;
            this.attribute = attr;
        }
        /**
         * Append one more annotation.
         * @param type Type of annotation
         * @param prop Name of property to set
         * @param value Value to use for property
         * @return This object
         */
        public Annotations append(final Class<?> type, final String prop,
            final String value) {
            final Annotation annotation = new Annotation(
                type.getName(),
                this.file.getConstPool()
            );
            annotation.addMemberValue(
                prop,
                new StringMemberValue(value, this.file.getConstPool())
            );
            this.attribute.addAnnotation(annotation);
            return this;
        }
        /**
         * Append one more annotation (with default "value" property).
         * @param type Type of annotation
         * @param value Value to use for property
         * @return This object
         */
        public Annotations append(final Class<?> type, final String value) {
            return this.append(type, "value", value);
        }
    }

}
