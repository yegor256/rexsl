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
package com.rexsl.maven.checks;

import com.rexsl.maven.Check;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider of checks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (100 lines)
 */
public final class ChecksProvider {

    /**
     * Test scope.
     */
    private transient String testScope;

    /**
     * Get full collection of checks.
     *
     * <p>Checks should be ordered by their complexity. Most simple and fast
     * checks should go first, in order to fail build faster. Most heavy and
     * slow checks should be at the end of the list.
     *
     * @return List of checks
     */
    public Set<Check> all() {
        final Set<Check> checks = new LinkedHashSet<Check>();
        checks.add(new BinaryFilesCheck());
        checks.add(new JigsawCssCheck());
        checks.add(new JSStaticCheck());
        checks.add(new FilesStructureCheck());
        checks.add(new WebXmlCheck());
        checks.add(new RexslFilesCheck());
        checks.add(new XhtmlOutputCheck(this.testScope));
        checks.add(new InContainerScriptsCheck(this.testScope));
        checks.add(new JSUnitTestsCheck());
        return checks;
    }

    /**
     * Sets the scope of tests to execute.
     * @param scope Pattern of test name
     */
    public void setTestScope(final String scope) {
        this.testScope = scope;
    }
}
