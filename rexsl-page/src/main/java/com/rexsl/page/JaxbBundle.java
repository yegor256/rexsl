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

import com.jcabi.aspects.Loggable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JAXB bundle.
 *
 * <p>It's a convenient instrument that enables on-fly creation of DOM/XML
 * structures, for example
 * (<a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent
 * interface</a>):
 *
 * <pre> Element element = new JaxbBundle("root")
 *   .add("employee")
 *     .attr("age", "28")
 *     .add("dept", "Software")
 *       .attr("country", "DE")
 *     .up()
 *     .add("salary", "> &#92;u20AC 50,000")
 *     .up()
 *     .add("rank", "high")
 *   .up()
 *   .attr("time", new Date())
 *   .element();</pre>
 *
 * <p>If you convert this {@code element} to XML this is how it will look:
 *
 * <pre> &lt;?xml version="1.0" ?&gt;
 * &lt;root time="Sun Jul 20 16:17:00 EDT 1969"&gt;
 *   &lt;employee age="28"&gt;
 *     &lt;dept country="DE"&gt;Software&lt;/dept&gt;
 *     &lt;salary&gt;&amp;gt; &#x20AC; 50,000&lt;/salary&gt;
 *     &lt;rank&gt;high&lt;/rank&gt;
 *   &lt;/employee&gt;
 * &lt;/root&gt;</pre>
 *
 * <p>Then, you can add this {@link Element} to your JAXB object, and return
 * it from a method annotated with {@link javax.xml.bind.annotation.XmlElement},
 * for example:
 *
 * <pre> &#64;XmlRootElement
 * public class Page {
 *   &#64;XmlElement
 *   public Object getEmployee() {
 *     return new JaxbBundle("employee")
 *       .attr("age", "35")
 *       .attr("country", "DE")
 *       .add("salary", "> &#92;u20AC 50,000")
 *       .up()
 *       .element();
 *   }
 * }</pre>
 *
 * <p>This mechanism, very often, is much more convenient and shorter than
 * a declaration of a new POJO every time you need to return a small piece
 * of XML data.
 *
 * <p>Since version 0.4.10 it's possible to add links, groups, and other
 * bundles. For example:
 *
 * <pre> final Element element = new JaxbBundle("garage")
 *   .link(new Link("add", "/add-car"))
 *   .add(
 *     new JaxbBundle.Group&lt;String&gt;(cars) {
 *       &#64;Override
 *       public JaxbBundle bundle(Car car) {
 *         return new JaxbBundle("car").add("make", car.make()).up();
 *       }
 *     }
 *   )
 *   .add(new JaxbBundle("owner").add("email", "...").up())
 *   .element();</pre>
 *
 * <p>The class is mutable and thread-safe.
 *
 * @todo #430 A new design would be required for this class. <br/>
 *  <b>The problems</b>:
 *  <ul>
 *  <li>this.content and this.parent are used as flags, not only as data
 *  storage variables.
 *  <li>there are three different "types" combined in
 *  one class: root element (parent == null), node element (content ==
 *  null), and leaf element.
 *  </ul>
 *  <br/>
 *  <b>The solution</b>: A new hierarchy of classes: <br/>
 *  <code>
 *  interface TreeNode<br/>
 *  <br/>
 *  abstract AbstractNode implements TreeNode<br/>
 *  +name: String<br/>
 *  +attributes: ConcurrentMap<String, String><br/>
 *  <br/>
 *  abstract SubNode extends AbstractNode<br/>
 *  +parent: TreeNode<br/>
 *  <br/>
 *  InnerNode extends SubNode<br/>
 *  +children: List<TreeNode><br/>
 *  <br/>
 *  LeafNode extends SubNode<br/>
 *  +content: String<br/>
 *  <br/>
 *  DeadNode implements TreeNode<br/>
 *  // this is the node we set as parent in root node<br/>
 *  </code> <br/>
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.7
 */
@SuppressWarnings({ "PMD.NullAssignment", "PMD.TooManyMethods" })
@ToString(of = { "name", "content" })
@EqualsAndHashCode(of = { "name", "content", "parent" })
@Loggable(Loggable.DEBUG)
public final class JaxbBundle {

    /**
     * Parent bundle, if exists.
     */
    private final transient JaxbBundle parent;

    /**
     * Name of it.
     */
    @NotNull
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
     * Links.
     */
    private final transient List<Link> links =
        new CopyOnWriteArrayList<Link>();

    /**
     * Attributes.
     */
    private final transient ConcurrentMap<String, String> attrs =
        new ConcurrentHashMap<String, String>();

    /**
     * Group.
     * @since 0.4.10
     * @checkstyle AbstractClassName (2 lines)
     */
    @SuppressWarnings("PMD.AbstractNaming")
    public abstract static class Group<T> {
        /**
         * Collection of objects.
         */
        private final transient Collection<T> objects;
        /**
         * Public ctor.
         * @param objs All objects
         */
        public Group(final Collection<T> objs) {
            this.objects = objs;
        }
        /**
         * Convert an object to JaxbBundle.
         * @param object The object to convert
         * @return Bundle produced
         */
        public abstract JaxbBundle bundle(T object);
        /**
         * Fetch all bundles.
         * @return All bundles
         */
        private Collection<JaxbBundle> bundles() {
            final Collection<JaxbBundle> bundles =
                new ArrayList<JaxbBundle>(this.objects.size());
            for (T object : this.objects) {
                bundles.add(this.bundle(object));
            }
            return bundles;
        }
    }

    /**
     * Default ctor, for JAXB (always throws a runtime exception).
     *
     * <p>You're not supposed to use this constructor. Instead, use either
     * {@link JaxbBundle(String)} or {@link JaxbBundle(String,String)}.
     */
    public JaxbBundle() {
        throw new IllegalStateException(
            "JaxbBundle() ctor can't be used, use JaxbBundle(String) instead"
        );
    }

    /**
     * Public ctor, with just a name of XML element and no text content.
     * @param nam The name of XML element
     */
    public JaxbBundle(@NotNull final String nam) {
        this.parent = null;
        this.name = nam;
        this.content = null;
    }

    /**
     * Public ctor, with XML element name and its content.
     * @param nam The name of XML element
     * @param text Text content of the XML document
     */
    public JaxbBundle(@NotNull final String nam, final String text) {
        this.parent = null;
        this.name = nam;
        if (text == null) {
            this.content = null;
        } else {
            this.content = text;
        }
    }

    /**
     * Public ctor.
     * @param prnt Not-null parent bundle
     * @param nam The name of XML element
     * @param text Text content of the XML element
     */
    private JaxbBundle(@NotNull final JaxbBundle prnt,
        @NotNull final String nam, final String text) {
        this.parent = prnt;
        this.name = nam;
        if (text == null) {
            this.content = null;
        } else {
            this.content = text;
        }
    }

    /**
     * Add new child XML element.
     * @param nam The name of child element
     * @return The child bundle (use {@link #up()} on it in order to get back to
     *  the parent bundle)
     */
    @NotNull
    public JaxbBundle add(@NotNull final String nam) {
        return this.add(nam, "");
    }

    /**
     * Add new child with text value.
     * @param nam The name of child
     * @param txt The text
     * @return The child bundle (use {@link #up()} on it in order to get back to
     *  the parent bundle)
     */
    @NotNull
    public JaxbBundle add(@NotNull final String nam,
        @NotNull final String txt) {
        final JaxbBundle child = new JaxbBundle(this, nam, txt.toString());
        this.children.add(child);
        return child;
    }

    /**
     * Add XML attribute to this bundle.
     * @param nam The name of attribute
     * @param val The plain text value
     * @return New bundle with set attribute
     */
    @NotNull
    public JaxbBundle attr(@NotNull final String nam,
        @NotNull final String val) {
        this.attrs.put(nam, val.toString());
        return this;
    }

    /**
     * Add new link into {@code &lt;links&gt;} section.
     * @param link The link to attach
     * @return New bundle with a newly added link
     * @since 0.4.10
     */
    @NotNull
    public JaxbBundle link(@NotNull final Link link) {
        this.links.add(link);
        return this;
    }

    /**
     * Add new bundle.
     * @param bundle The bundle to add
     * @return New bundle with a newly added element
     * @since 0.4.10
     */
    @NotNull
    public JaxbBundle add(@NotNull final JaxbBundle bundle) {
        this.children.add(bundle);
        return this;
    }

    /**
     * Add new group.
     * @param wrap The name of the wrapper
     * @param group The group
     * @return New bundle with a newly added group of elements
     * @since 0.4.10
     */
    @NotNull
    public JaxbBundle add(@NotNull final String wrap,
        @NotNull final JaxbBundle.Group<?> group) {
        JaxbBundle holder = this.add(wrap);
        for (JaxbBundle bundle : group.bundles()) {
            holder = holder.add(bundle);
        }
        return holder.up();
    }

    /**
     * Return parent bundle.
     * @return The parent bundle
     * @checkstyle MethodName (4 lines)
     */
    @NotNull
    @SuppressWarnings("PMD.ShortMethodName")
    public JaxbBundle up() {
        if (this.parent == null) {
            throw new IllegalArgumentException(
                "Already at the top, can't go #up()"
            );
        }
        return this.parent;
    }

    /**
     * Convert this bundle into DOM/XML {@link Element}.
     * @return The element
     */
    @NotNull
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
     * Create and return a DOM element, inside the document provided.
     * @param doc The document
     * @return The element just created
     */
    private Element element(final Document doc) {
        final Element element = doc.createElement(this.name);
        for (ConcurrentMap.Entry<String, String> attr : this.attrs.entrySet()) {
            element.setAttribute(attr.getKey(), attr.getValue());
        }
        for (JaxbBundle child : this.children) {
            element.appendChild(child.element(doc));
        }
        if (!this.links.isEmpty()) {
            final Element lnks = doc.createElement("links");
            for (Link link : this.links) {
                final Element lnk = doc.createElement("link");
                lnk.setAttribute("rel", link.getRel());
                lnk.setAttribute("href", link.getHref().toString());
                lnk.setAttribute("type", link.getType());
                lnks.appendChild(lnk);
            }
            element.appendChild(lnks);
        }
        element.appendChild(doc.createTextNode(this.content));
        return element;
    }

}
