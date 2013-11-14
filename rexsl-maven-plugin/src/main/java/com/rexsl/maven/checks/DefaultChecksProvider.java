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
package com.rexsl.maven.checks;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.maven.Check;
import com.rexsl.maven.ChecksProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Provider of checks.
 *
 * <p>This class is NOT thread-safe.
 *
 * <p>Checks are executed in the order they are listed here.
 *
 * <h3>BinaryFilesCheck</h3>
 *
 * <p>You shouldn't keep any binary files (images, video, etc.) in
 * {@code src/main/webapp} folder and deploy them inside your {@code WAR}
 * package. Instead, you should use some storage service
 * (<a href="http://aws.amazon.com/s3">Amazon S3</a>, for example) and a
 * content delivery network (CDN) on top of it
 * (<a href="http://aws.amazon.com/cloudfront">Amazon CloudFront</a>,
 * for example). This check validates that no binary files are located inside
 * {@code src/main/webapp} and complains if anything found.
 *
 * <h3>JigsawCssCheck</h3>
 *
 * <p>All CSS stylesheets from {@code src/main/webapp/css} folder are sent
 * to W3C Jigsaw validator. Any defect found will fail the build. Read more
 * about this validation in {@link com.rexsl.w3c.Validator}.
 *
 * <h3>JSStaticCheck</h3>
 *
 * <p>JavaScript files from {@code src/main/webapp/js} are validated with YUI.
 * This test is not implemented yet.
 *
 * <h3>FilesStructureCheck</h3>
 *
 * <p>The following structure of directories and files is enforced inside
 * {@code src/main/webapp} and {@code src/test/rexsl}
 * (all of them should be present):
 *
 * <pre>
 * src/main/webapp
 *   /xsl
 *   /WEB-INF/web.xml
 *   /robots.txt
 * src/test/rexsl
 *   /xml
 *   /scripts
 *   /xsd
 *   /xhtml
 * </pre>
 *
 * <h3>RexslFilesCheck</h3>
 *
 * <p>Test folders should contain files of certain types only:
 *
 * <pre>
 * src/test/rexsl
 *   /xml/*.xml
 *   /scripts/*.groovy
 *   /xsd/*.xsd
 *   /xhtml/*.groovy
 *   /bootstrap/*.groovy
 *   /setup/*.groovy
 * </pre>
 *
 * <p>If any other types of files are found in these folders the check will
 * complain and fail the build.
 *
 * <h3>LibrariesCheck</h3>
 *
 * <p>The check verifies all libraries from {@code target/.../WEB-INF/lib}
 * and finds conflicts between them.
 *
 * <h3>XhtmlOutputCheck</h3>
 *
 * <p>XML pages from {@code src/test/rexsl/xml} are transformed to XHTML using
 * XSL stylesheets attatched to them. Then, produced HTML pages are sent to
 * W3C validator. And then, they are sent to designated groovy scripts
 * from {@code src/test/rexsl/xhtml}. Name of Groovy script should be identical
 * to the name of the XML page (except the extension, of course). Keep in mind
 * that Groovy pre-compiles scripts to Java classes, that's why names should
 * obey Java class naming rules.
 *
 * <p>Content of a created XHTML page is sent to the Groovy script in
 * {@code rexsl.document} variable. It's very convenient to use
 * {@link com.rexsl.test.XhtmlMatchers} methods to assert certain XPath
 * queries on the HTML documents.
 *
 * <h3>InContainerScriptsCheck</h3>
 *
 * <p>The check starts an embedded Java Servlet container and runs Groovy
 * scripts from {@code src/test/rexsl/scripts}, one by one. Scripts can
 * use {@link com.rexsl.test.RestTester} class to test the application
 * through its RESTful interface. The URI of the running application is
 * supplied in {@code rexsl.home} variable.
 *
 * <h3>JSUnitTestsCheck</h3>
 *
 * <p>TBD...
 *
 * <h3>WebXmlCheck</h3>
 *
 * <p>The check validates {@code src/main/webapp/WEB-INF/web.xml} XML file
 * against its XSD schema and fails the build if any incosistencies are
 * detected. Validation may take some time (15-45 seconds).
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (100 lines)
 */
@ToString
@EqualsAndHashCode(of = "test")
public final class DefaultChecksProvider implements ChecksProvider {

    /**
     * All known checks.
     */
    private static final Collection<String> NAMES =
        Collections.unmodifiableList(
            Arrays.asList(
                "com.rexsl.maven.checks.BinaryFilesCheck",
                "com.rexsl.maven.checks.JigsawCssCheck",
                "com.rexsl.maven.checks.JSStaticCheck",
                "com.rexsl.maven.checks.FilesStructureCheck",
                "com.rexsl.maven.checks.RexslFilesCheck",
                "com.rexsl.maven.checks.LibrariesCheck",
                "com.rexsl.maven.checks.XhtmlOutputCheck",
                "com.rexsl.maven.checks.InContainerScriptsCheck",
                "com.rexsl.maven.checks.WebXmlCheck"
            )
        );

    /**
     * Checks to retrieve (comma-separated list of full or short class names).
     */
    private final transient Set<String> checks = new LinkedHashSet<String>(
        DefaultChecksProvider.NAMES
    );

    /**
     * Test scope.
     */
    private transient String test = "";

    /**
     * Mutex for synchronization.
     */
    @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
    private final transient Object mutex = "_mutex_";

    @Override
    @Loggable(Loggable.DEBUG)
    public Collection<Check> all() {
        final Set<Check> all = new LinkedHashSet<Check>();
        for (String name : this.checks) {
            try {
                all.add(this.build(name));
            } catch (InvalidCheckException ex) {
                Logger.info(this, "Can't find %s, available checks:", name);
                for (String check : DefaultChecksProvider.NAMES) {
                    Logger.info(this, "  %s", check);
                }
                throw new IllegalArgumentException(
                    String.format("Check %s can't be built", name),
                    ex
                );
            }
        }
        return Collections.unmodifiableSet(all);
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public void setTest(@NotNull final String scope) {
        synchronized (this.mutex) {
            this.test = scope;
        }
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public void setCheck(@NotNull final String scope) {
        synchronized (this.mutex) {
            this.checks.clear();
            this.checks.addAll(
                Arrays.asList(scope.split("\\s*,\\s*"))
            );
        }
    }

    /**
     * Build check from its name.
     * @param name Full name of the class or a short one
     * @return The check
     * @throws DefaultChecksProvider.InvalidCheckException If failed
     */
    private Check build(final String name)
        throws DefaultChecksProvider.InvalidCheckException {
        String cname;
        if (name.contains(".")) {
            cname = name;
        } else {
            cname = String.format(
                "%s.%s",
                DefaultChecksProvider.class.getPackage().getName(),
                name
            );
        }
        Check check;
        try {
            check = Check.class.cast(Class.forName(cname).newInstance());
        } catch (ClassNotFoundException ex) {
            throw new DefaultChecksProvider.InvalidCheckException(ex);
        } catch (InstantiationException ex) {
            throw new DefaultChecksProvider.InvalidCheckException(ex);
        } catch (IllegalAccessException ex) {
            throw new DefaultChecksProvider.InvalidCheckException(ex);
        }
        check.setScope(this.test);
        return check;
    }

    /**
     * Thrown when class can't be built.
     */
    private static final class InvalidCheckException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7526FA7CDAA2147AL;
        /**
         * Public ctor.
         * @param cause Cause of the problem
         */
        public InvalidCheckException(final Throwable cause) {
            super(cause);
        }
    }

}
