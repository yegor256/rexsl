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
package com.rexsl.test;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.MissingNode;

/**
 * Implementation of {@link JsonDocument}.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4
 */
@ToString
@EqualsAndHashCode(of = "node")
@Loggable(Loggable.DEBUG)
public final class SimpleJson implements JsonDocument {

    /**
     * Underlying json node.
     */
    @NotNull
    private final transient JsonNode node;

    /**
     * Public ctor.
     * @param txt Body
     */
    public SimpleJson(@NotNull final String txt) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            this.node = mapper.readValue(txt, JsonNode.class);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Public ctor.
     * @param root JsonNode
     */
    public SimpleJson(final JsonNode root) {
        this.node = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> json(@NotNull final String query) {
        final List<String> result = new LinkedList<String>();
        for (JsonNode doc : this.getNodes(query)) {
            if (doc.isMissingNode() || doc.size() > 0) {
                continue;
            }
            result.add(SimpleJson.unquote(doc.getTextValue()));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<JsonDocument> nodesJson(@NotNull final String query) {
        final List<JsonDocument> result = new LinkedList<JsonDocument>();
        for (JsonNode item : this.getNodes(query)) {
            result.add(new SimpleJson(item));
        }
        return result;
    }

    /**
     * Unquotates given string. For some reason jackson stores
     * text values quoted, so there is a need in unquoting them
     * @param str To unqoute
     * @return Unquoted string
     */
    private static String unquote(final String str) {
        final String quote = "\"";
        String result = str;
        if (str != null && str.startsWith(quote) && str.endsWith(quote)) {
            result = str.substring(1, str.length() - 1);
        }
        return result;
    }

    /**
     * Finds nodes by query.
     * @param query Path
     * @return List of nodes
     */
    private List<JsonNode> getNodes(final String query) {
        final JsonNode terminalNode =
            this.getTerminalNode(this.node, new StringTokenizer(query, "."));
        final List<JsonNode> result = new LinkedList<JsonNode>();
        if (terminalNode.size() > 0) {
            for (final Iterator<JsonNode> iterator = terminalNode.iterator();
                iterator.hasNext();) {
                result.add(iterator.next());
            }
        } else {
            result.add(terminalNode);
        }
        return result;
    }

    /**
     * Finds subnode matchig query for given node.
     * @param start To search in
     * @param tokenizer Path
     * @return Node matching qury
     */
    private JsonNode getTerminalNode(
        final JsonNode start,
        final StringTokenizer tokenizer) {
        final StringTokenizer indexTokenizer = new StringTokenizer(
            tokenizer.nextToken(),
            "[]"
        );
        final String name = indexTokenizer.nextToken();
        JsonNode stepNode = start.findPath(name);
        if (indexTokenizer.hasMoreTokens()) {
            try {
                final int index = Integer.parseInt(indexTokenizer.nextToken());
                if (index < 0 || index >= stepNode.size()) {
                    stepNode = MissingNode.getInstance();
                } else {
                    stepNode = stepNode.get(index);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        if (tokenizer.hasMoreTokens()) {
            stepNode = this.getTerminalNode(stepNode, tokenizer);
        }
        return stepNode;
    }
}
