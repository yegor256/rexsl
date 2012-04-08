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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JAXB bundle.
 *
 * <p>It's a convenient instrument that enables on-fly creation of DOM/XML
 * structures, for example (
 * <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent
 * interface</a>):
 *
 * <pre>
 * final org.w3c.dom.Element elm = new JaxbBundle("root")
 *   .add("employee")
 *     .attr("age", "28")
 *     .add("dept")
 *       .attr("country", "DE")
 *       .add("salary", "> \u20AC 50,000")
 *       .up()
 *       .add("boss", "Charles de Batz-Castelmore d'Artagnan")
 *       .up()
 *     .up()
 *   .up();
 * </pre>
 *
 * <p>Then, you can add this {@link Element} to your JAXB object, and return
 * it from a method annotated with {@link javax.xml.bind.annotation.XmlElement},
 * for example:
 *
 * <pre>
 * &#64;XmlRootElement
 * public class Page {
 *   &#64;XmlElement
 *   public Object getEmployee() {
 *     return new JaxbBundle("employee")
 *       .attr("age", "35")
 *       .attr("country", "DE")
 *       .add("salary", "> \u20AC 50,000")
 *       .up()
 *       .element();
 *   }
 * }
 * </pre>
 *
 * <p>This mechanism, very often, is much more convenient and shorter than
 * a declaration of a new POJO every time you need to return a small piece
 * of XML data.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
@SuppressWarnings("PMD.NullAssignment")
public final class JaxbBundle {

    /**
     * Parent bundle, if exists.
     */
    private final transient JaxbBundle parent;

    /**
     * Name of it.
     */
    private final transient String name;

    /**
     * Text content of it.
     */
    private final transient String content;

    /**
     * Children.
     */
    private final transient List<JaxbBundle> children =
        new CopyOnWriteArrayList<JaxbBundle>();

    /**
     * Attributes.
     */
    private final transient ConcurrentMap<String, String> attrs =
        new ConcurrentHashMap<String, String>();

    /**
     * Default ctor, for JAXB.
     */
    public JaxbBundle() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param nam The name of it
     */
    public JaxbBundle(final String nam) {
        this.parent = null;
        this.name = nam;
        this.content = null;
    }

    /**
     * Public ctor.
     * @param nam The name of it
     * @param text The content
     */
    public JaxbBundle(final String nam, final Object text) {
        this.parent = null;
        this.name = nam;
        if (text == null) {
            this.content = (String) text;
        } else {
            this.content = text.toString();
        }
    }

    /**
     * Public ctor.
     * @param prnt Parent bundle
     * @param nam The name of it
     * @param text The content
     */
    private JaxbBundle(final JaxbBundle prnt, final String nam,
        final Object text) {
        this.parent = prnt;
        this.name = nam;
        if (text == null) {
            this.content = (String) text;
        } else {
            this.content = text.toString();
        }
    }

    /**
     * Add new child.
     * @param nam The name of child
     * @return This object
     */
    public JaxbBundle add(final String nam) {
        return this.add(nam, "");
    }

    /**
     * Add new child with text value.
     * @param nam The name of child
     * @param txt The text
     * @return This object
     */
    public JaxbBundle add(final String nam, final Object txt) {
        if (txt == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't add(%s, NULL) to '%s'",
                    nam,
                    this.name
                )
            );
        }
        final JaxbBundle child = new JaxbBundle(this, nam, txt.toString());
        this.children.add(child);
        return child;
    }

    /**
     * Add attribute.
     * @param nam The name of attribute
     * @param val The value
     * @return This object
     */
    public JaxbBundle attr(final String nam, final Object val) {
        this.attrs.put(nam, val.toString());
        return this;
    }

    /**
     * Return parent.
     * @return The parent bundle
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public JaxbBundle up() {
        return this.parent;
    }

    /**
     * Get DOM element.
     * @return The element
     */
    public Element element() {
        if (this.parent != null) {
            throw new IllegalArgumentException(
                "You can convert only top level JaxbBundle to DOM"
            );
        }
        try {
            return this.element(
                DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument()
            );
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get DOM element.
     * @param doc The document
     * @return The element
     */
    private Element element(final Document doc) {
        final Element element = doc.createElement(this.name);
        for (ConcurrentMap.Entry<String, String> attr : this.attrs.entrySet()) {
            element.setAttribute(attr.getKey(), attr.getValue());
        }
        for (JaxbBundle child : this.children) {
            element.appendChild(child.element(doc));
        }
        element.appendChild(doc.createTextNode(this.content));
        return element;
    }

}
