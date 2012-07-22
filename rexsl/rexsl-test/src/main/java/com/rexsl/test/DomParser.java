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

import com.jcabi.log.Logger;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sourceforge.reb4j.Regex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * Convenient parser of XML to DOM.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class DomParser {

    /**
     * Pattern to detect if passed txt looks like xml.
     */
    private static final Pattern PATTERN = DomParser.buildPattern();

    /**
     * The XML as a text.
     */
    private final transient String xml;

    /**
     * Public ctor.
     *
     * <p>An {@link IllegalArgumentException} may be thrown if the parameter
     * passed is not in XML format. It doesn't perform a strict validation
     * and is not guaranteed that an exception will be thrown whenever
     * the parameter is not XML.
     * @param txt The XML in text
     */
    public DomParser(@NotNull final String txt) {
        if (txt.isEmpty()
            || !this.PATTERN.matcher(txt.replaceAll("\\s", "")).matches()) {
            throw new IllegalArgumentException(
                Logger.format("Doesn't look like XML: '%s'", txt)
            );
        }
        this.xml = txt;
    }

    /**
     * Get document of body.
     * @return The document
     */
    public Document document() {
        Document doc;
        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            // @checkstyle LineLength (1 line)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            doc = factory
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(this.xml, CharEncoding.UTF_8));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(
                Logger.format("Invalid XML: \"%s\"", this.xml),
                ex
            );
        }
        return doc;
    }

    /**
     * Pattern initialization method.
     * @return The pattern
     */
    private static Pattern buildPattern() {
        // @checkstyle LineLength (2 line)DPT
        final String startCharacter =
            "[:_a-zA-Z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]";
        final String character = "[-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]";
        final Regex prolog = Regex.fromPattern(
            Pattern.compile("(<\\?xml.*\\?>\\s*)")
        );
        final Regex doctype = Regex.fromPattern(
            Pattern.compile("(<!DOCTYPE.*>)")
        );
        final Regex comment = Regex.fromPattern(
            Pattern.compile("(<!--.*-->)")
        );
        final Regex element = Regex
            .fromPattern(Pattern.compile(startCharacter)).atLeastOnce()
            .then(Regex.fromPattern(Pattern.compile(character)).star());
        return Regex.sequence(
            prolog.optional(),
            doctype.optional(),
            comment.optional(),
            Regex.literal("<"),
            element.then(Regex.fromPattern(Pattern.compile(".")).star()),
            element.or(Regex.literal("/")),
            Regex.literal(">")
        ).toPattern();
    }
}
