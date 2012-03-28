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

import java.util.List;

/**
 * XML document.
 *
 * <p>Set of convenient XML manipulations:
 *
 * <pre>
 * XmlDocument xml = new SimpleXml(content);
 * for (XmlDocument employee : xml.nodes("//Employee")) {
 *   String name = employee.xpath("name/text()").get(0);
 *   // ...
 * }
 * </pre>
 *
 * <p>Implementation of this interface shall be immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
public interface XmlDocument {

    /**
     * Find and return nodes matched by xpath.
     * @param query The XPath query
     * @return The list of node values (texts)
     */
    List<String> xpath(String query);

    /**
     * Retrieve nodes from the XML response.
     * @param query The XPath query
     * @return Collection of responses
     */
    List<XmlDocument> nodes(String query);

    /**
     * Register additional namespace prefix for XPath.
     * @param prefix The prefix to register
     * @param uri Namespace URI
     * @return This object
     */
    XmlDocument registerNs(String prefix, Object uri);

}
