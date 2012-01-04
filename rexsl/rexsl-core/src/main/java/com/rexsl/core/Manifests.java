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
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.SerializationUtils;

/**
 * Wrapper around {@code MANIFEST.MF} files.
 *
 * The class will read all {@code MANIFEST.MF} files available in classpath
 * and all attributes from them. This mechanism is very useful for sending
 * information from continuous integration environment to the production
 * environment. For example, you want your site to show project version and
 * the date of WAR file packaging. First, you configure
 * {@code maven-war-plugin} to add this information to {@code MANIFEST.MF}:
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
 * <p>{@code maven-war-plugin} will add these attributes to your
 * {@code MANIFEST.MF} file and the
 * project will be deployed to the production environment. Then, you can read
 * these attributes where it's necessary (in one of your JAXB annotated objects,
 * for example) and show to users:
 *
 * <pre>
 * import com.rexsl.core.Manifest;
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
 * <p>In unit and integration tests you may need to inject some values
 * to {@code MANIFEST.MF} in runtime (for example in your bootstrap Groovy
 * scripts):
 *
 * <pre>
 * import com.rexsl.core.Manifests
 * Manifests.inject("Foo-URL", "http://localhost/abc");
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
     * Injected attributes.
     * @see #inject(String,String)
     */
    private static final Map<String, String> INJECTED =
        new ConcurrentHashMap<String, String>();

    /**
     * Attributes retrieved from all existing {@code MANIFEST.MF} files.
     * @see #load()
     */
    private static Map<String, String> attributes;

    /**
     * Failures registered during loading.
     * @see #load()
     */
    private static Map<URL, String> failures;

    /**
     * Load add available data on first loading of this class
     * into JVM.
     */
    static {
        Manifests.load();
    }

    /**
     * It's a utility class, can't be instantiated.
     */
    private Manifests() {
        // intentionally empty
    }

    /**
     * Read one attribute available in one of {@code MANIFEST.MF} files.
     *
     * <p>If such a attribute doesn't exist {@link IllegalArgumentException}
     * will be thrown. If you're not sure whether the attribute is present or
     * not use {@link #exists(String)} beforehand.
     *
     * <p>During testing you can inject attributes into this class by means
     * of {@link #inject(String,String)}.
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
        if (!Manifests.exists(name)) {
            final StringBuilder bldr = new StringBuilder(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "Atribute '%s' not found in MANIFEST.MF file(s) among %d other attribute(s) %[list]s and %d injection(s)",
                    name,
                    Manifests.attributes.size(),
                    Manifests.attributes.keySet(),
                    Manifests.INJECTED.size()
                )
            );
            if (!Manifests.failures.isEmpty()) {
                bldr.append("; failures: ").append(
                    Logger.format("%[list]s", Manifests.failures.keySet())
                );
            }
            throw new IllegalArgumentException(bldr.toString());
        }
        String result;
        if (Manifests.INJECTED.containsKey(name)) {
            result = Manifests.INJECTED.get(name);
        } else {
            result = Manifests.attributes.get(name);
        }
        return result;
    }

    /**
     * Inject new attribute.
     *
     * <p>An attribute can be injected in runtime, mostly for the sake of
     * unit and integration testing. Once injected an attribute becomes
     * available with {@link read(String)}.
     *
     * @param name Name of the attribute
     * @param value The value of the attribute being injected
     */
    public static void inject(final String name, final String value) {
        if (Manifests.INJECTED.containsKey(name)) {
            Logger.info(
                Manifests.class,
                "#inject(%s, '%s'): replaced previous injection '%s'",
                name,
                value,
                Manifests.INJECTED.get(name)
            );
        } else {
            Logger.info(
                Manifests.class,
                "#inject(%s, '%s'): injected",
                name,
                value
            );
        }
        Manifests.INJECTED.put(name, value);
    }

    /**
     * Check whether attribute exists in any of {@code MANIFEST.MF} files.
     *
     * <p>Use this method before {@link read(String)} to check whether an
     * attribute exists, in order to avoid a runtime exception.
     *
     * @param name Name of the attribute to check
     * @return Returns {@code TRUE} if it exists, {@code FALSE} otherwise
     */
    public static boolean exists(final String name) {
        return Manifests.attributes.containsKey(name)
            || Manifests.INJECTED.containsKey(name);
    }

    /**
     * Make a snapshot of current attributes and their values.
     * @return The snapshot, to be used later with {@link #revert(String)}
     */
    public static String snapshot() {
        try {
            return new String(
                SerializationUtils.serialize((Serializable) Manifests.INJECTED),
                CharEncoding.UTF_8
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Revert to the state that was recorded by {@link #snapshot()}.
     * @param snapshot The snapshot taken by {@link #snapshot()}
     */
    public static void revert(final String snapshot) {
        Manifests.INJECTED = (Map<String, String>)
            SerializationUtils.deserialize(snapshot);
    }

    /**
     * Append attributes from the web application {@code MANIFEST.MF}, called
     * from {@link XsltFilter#init(FilterConfig)}.
     * @param ctx Servlet context
     * @see #Manifests()
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static void append(final ServletContext ctx) {
        URL main;
        try {
            main = ctx.getResource("/META-INF/MANIFEST.MF");
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        if (main == null) {
            Logger.warn(
                Manifests.class,
                "#append(%s): MANIFEST.MF not found in WAR package",
                ctx.getClass().getName()
            );
        } else {
            final Map<String, String> attrs = Manifests.loadOneFile(main);
            Manifests.attributes.putAll(attrs);
            Logger.info(
                Manifests.class,
                "#append(%s): %d attributes loaded from %s: %[list]s",
                ctx.getClass().getName(),
                attrs.size(),
                main,
                attrs.keySet()
            );
        }
    }

    /**
     * Load attributes from classpath.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static void load() {
        final long start = System.currentTimeMillis();
        Manifests.attributes = new ConcurrentHashMap<String, String>();
        Manifests.failures = new ConcurrentHashMap<URL, String>();
        Integer count = 0;
        for (URL url : Manifests.urls()) {
            try {
                Manifests.attributes.putAll(Manifests.loadOneFile(url));
            // @checkstyle IllegalCatch (1 line)
            } catch (Exception ex) {
                Manifests.failures.put(url, ex.getMessage());
                Logger.error(
                    Manifests.class,
                    "#load(): '%s' failed %[exception]s",
                    url,
                    ex
                );
            }
            count += 1;
        }
        Logger.info(
            Manifests.class,
            "#load(): %d attributes loaded from %d URL(s) in %dms: %[list]s",
            Manifests.attributes.size(),
            count,
            System.currentTimeMillis() - start,
            Manifests.attributes.keySet()
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
            resources = Thread.currentThread().getContextClassLoader()
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
     * Load attributes from one file.
     * @param url The URL of it
     * @return The attributes loaded
     * @see #load()
     */
    private static Map<String, String> loadOneFile(final URL url) {
        final Map<String, String> props =
            new ConcurrentHashMap<String, String>();
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
            Logger.trace(
                Manifests.class,
                "#loadOneFile('%s'): %d attributes loaded (%[list]s)",
                url,
                props.size(),
                props.keySet()
            );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                stream.close();
            } catch (java.io.IOException ex) {
                Logger.error(
                    Manifests.class,
                    "#loadOneFile('%s'): %[exception]s",
                    url,
                    ex
                );
            }
        }
        return props;
    }

}
