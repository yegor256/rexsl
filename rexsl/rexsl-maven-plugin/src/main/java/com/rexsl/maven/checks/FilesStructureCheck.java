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
package com.rexsl.maven.checks;

import com.rexsl.maven.Check;
import com.rexsl.maven.Environment;
import com.rexsl.maven.Reporter;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 * Validate location of files/dirs.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class FilesStructureCheck implements Check {

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validate(final Environment env) {
        final String[] names = {
            "src/main/webapp/",
            "src/main/webapp/xsl/",
            "src/main/webapp/WEB-INF/web.xml",
            "src/test/rexsl/xml/",
            "src/test/rexsl/xhtml/",
            "src/test/rexsl/scripts/",
            "src/test/rexsl/xsd/",
        };
        boolean success = true;
        for (String name : names) {
            try {
                this.one(env.basedir(), name);
            } catch (InternalCheckException ex) {
                final String msg = ex.getMessage();
                if (!msg.isEmpty()) {
                    env.reporter().report(msg);
                }
                // success = false;
            }
        }
        return success;
    }

    /**
     * Check for existence of this file/dir.
     * @param basedir Project basedir
     * @param name The name of the file to check
     * @throws InternalCheckException If some failure inside
     */
    public final void one(final File basedir, final String name)
        throws InternalCheckException {
        final File file = new File(basedir, name);
        if (!file.exists()) {
            throw new InternalCheckException(
                "File '%s' is absent, but should be there",
                file
            );
        }
    }

}
