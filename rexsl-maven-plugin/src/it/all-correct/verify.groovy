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
 *
 * @version $Id$
 *
 * Validate that the build really validated XSL files.
 */

import com.rexsl.test.XhtmlMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def log = new File(basedir, 'build.log')
assert log.text.contains('All ReXSL checks passed')

// Let's verify that we're getting natural log from the application,
// as it's designed there (not a Maven log). This message will be visible in
// logs only if in-application logging facility is used.
assert log.text.contains('com.rexsl.core.RestfulServlet: #init():')

// Let's verify that all files are packaged in destination folder
def css = new File(basedir, 'target/all-correct-1.0/css/screen.css')
assert css.exists()
assert !css.text.contains('/**')

def xsl = new File(basedir, 'target/all-correct-1.0/xsl/layout.xsl')
MatcherAssert.assertThat(
    xsl.text,
    Matchers.allOf(
        Matchers.not(XhtmlMatchers.hasXPath('//comment()')),
        XhtmlMatchers.hasXPath('//xhtml:div[@id="marker" and .="ABC"]')
    )
)
