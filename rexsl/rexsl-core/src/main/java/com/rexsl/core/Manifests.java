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

import com.ymock.util.Logger;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;

/**
 * Wrapper around <tt>MANIFEST.MF</tt> files.
 *
 * The class will read all <tt>MANIFEST.MF</tt> files available in classpath
 * and all attributes from them. This mechanism is very useful for sending
 * information from continuous integration environment to the production
 * environment. For example, you want you site to show project version and
 * date of when WAR file was packaged. First, you configure
 * <tt>maven-war-plugin</tt> to add this information to <tt>MANIFEST.MF</tt>:
 *
 * <pre>
 * &lt;plugin>
 *  &lt;artifactId>maven-war-plugin&lt;/artifactId>
 *  &lt;configuration>
 *   &lt;archive>
 *    &lt;manifestEntries>
 *     &lt;Foo-Version>${project.version}&lt;/Foo-Version>
 *     &lt;Foo-Date>${maven.build.timestamp}&lt;/Foo-Date>
 *    &lt;/manifestEntries>
 *   &lt;/archive>
 *  &lt;/configuration>
 * &lt;/plugin>
 * </pre>
 *
 * <p>The plugin will add these attributes to your <tt>MANIFEST.MF</tt> and the
 * project will be deployed to the production environment. Then, you can read
 * them where it's necessary (in one of your JAXB annotated objects,
 * for example) and show to users:
 *
 * <pre>
 * import com.rexsl.core.Manifests.
 * import java.text.SimpleDateFormat;
 * import java.util.Date;
 * import java.util.Locale;
 * import javax.xml.bind.annotation.XmlElement;
 * import javax.xml.bind.annotation.XmlRootElement;
 * &#64;XmlRootElement
 * public final class Page {
 *   &#64;XmlElement
 *   public String version() {
 *    return Manifests.read("Foo-Version");
 *   }
 *   &#64;XmlElement
 *   public Date date() {
 *    return new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).parse(
 *     Manifests.read("Foo-Date");
 *    );
 *   }
 * }
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Prasath Premkumar (popprem@gmail.com)
 * @version $Id$
 * @see <a href="http://download.oracle.com/javase/1,5.0/docs/guide/jar/jar.html#JAR%20Manifest">JAR Manifest</a>
 * @see <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver</a>
 * @see <a href="http://trac.fazend.com/rexsl/ticket/55">Class was introduced in ticket #55</a>
 * @since 0.3
 */
public final class Manifests {

    /**
     * Properties retrieved from all existing <tt>MANIFEST.MF</tt> files.
     * @see #load()
     */
    private static Map<String, String> attributes;

    /**
     * Failures registered during loading.
     * @see #load()
     */
    private static Map<URL, String> failures;

    static {
        Manifests.load();
    }

    /**
     * It's a utility class.
     */
    private Manifests() {
        // intentionally empty
    }

    /**
     * Read one attribute available in one of <tt>MANIFEST.MF</tt> files.
     *
     * <p>If such a attribute doesn't exist {@link IllegalArgumentException}
     * will be thrown.
     *
     * @param name Name of the attribute
     * @return The value of the attribute retrieved
     */
    public static String read(final String name) {
        if (Manifests.attributes == null) {
            throw new IllegalArgumentException(
                "Manifests haven't been loaded yet by request from XsltFilter"
            );
        }
        if (!Manifests.attributes.containsKey(name)) {
            final StringBuilder bldr = new StringBuilder(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Atribute '%s' not found in MANIFEST.MF files among %d other attributes (%s)",
                    name,
                    Manifests.attributes.size(),
                    Manifests.group(Manifests.attributes.keySet())
                )
            );
            if (!Manifests.failures.isEmpty()) {
                bldr.append("; failures: ").append(
                    Manifests.group(Manifests.failures.keySet())
                );
            }
            throw new IllegalArgumentException(bldr.toString());
        }
        return Manifests.attributes.get(name);
    }

    /**
     * Inject new attribute.
     * @param name Name of the attribute
     * @param value The value of the attribute being injected
     */
    public static void inject(final String name, final String value) {
        // todo
    }

    /**
     * Check whether attribute exists in any of <tt>MANIFEST.MF</tt> files.
     * @param name Name of the attribute
     * @return It exists?
     */
    public static boolean exists(final String name) {
        // todo
        return true;
    }

    /**
     * Append properties from the web application <tt>MANIFEST.MF</tt>,
     * {@link XsltFilter#init(FilterConfig)}.
     * @param ctx Servlet context
     * @see #Manifests()
     */
    protected static void append(final ServletContext ctx) {
        URL main;
        try {
            main = ctx.getResource("/META-INF/MANIFEST.MF");
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        if (main != null) {
            Manifests.attributes.putAll(Manifests.loadOneFile(main));
        }
    }

    /**
     * Load attributes from classpath.
     */
    private static void load() {
        Manifests.attributes = new HashMap<String, String>();
        Manifests.failures = new HashMap<URL, String>();
        Integer count = 0;
        for (URL url : Manifests.urls()) {
            try {
                Manifests.attributes.putAll(Manifests.loadOneFile(url));
            // @checkstyle IllegalCatch (1 line)
            } catch (Exception ex) {
                Manifests.failures.put(url, ex.getMessage());
                Logger.error(
                    Manifests.class,
                    "#load(): '%s' failed %s",
                    url,
                    ex.getMessage()
                );
            }
            count += 1;
        }
        Logger.debug(
            Manifests.class,
            "#load(): %d properties loaded from %d URL(s): %s",
            Manifests.attributes.size(),
            count,
            Manifests.group(Manifests.attributes.keySet())
        );
    }

    /**
     * Find all URLs.
     * @return The list of URLs
     * @see #load()
     */
    private static Set<URL> urls() {
        final Set<URL> urls = new HashSet<URL>();
        Enumeration<URL> resources;
        try {
            resources = Manifests.class.getClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }

    /**
     * Load properties from one file.
     * @param url The URL of it
     * @return The properties loaded
     * @see #load()
     */
    private static Map<String, String> loadOneFile(final URL url) {
        final Map<String, String> props = new HashMap<String, String>();
        InputStream stream;
        try {
            stream = url.openStream();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            final Manifest manifest = new Manifest(stream);
            final Attributes attrs = manifest.getMainAttributes();
            for (Object key : attrs.keySet()) {
                final String value = attrs.getValue((Name) key);
                props.put(key.toString(), value);
            }
            Logger.debug(
                Manifests.class,
                "#loadOneFile('%s'): %d properties loaded (%s)",
                url,
                props.size(),
                Manifests.group(props.keySet())
            );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                stream.close();
            } catch (java.io.IOException ex) {
                Logger.error(
                    Manifests.class,
                    "#loadOneFile('%s'): %s",
                    url,
                    ex.getMessage()
                );
            }
        }
        return props;
    }

    /**
     * Convert collection of objects into text.
     * @param group The objects
     * @return The text
     */
    private static String group(final Collection group) {
        return StringUtils.join(group, ", ");
    }

}
