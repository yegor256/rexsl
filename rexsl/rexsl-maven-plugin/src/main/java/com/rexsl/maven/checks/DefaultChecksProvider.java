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
import com.rexsl.maven.ChecksProvider;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider of checks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (100 lines)
 */
public final class DefaultChecksProvider implements ChecksProvider {

    /**
     * Test scope.
     */
    private transient String test;

    /**
     * Check to perform.
     */
    private transient Class<? extends Check> check;

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Check> all() {
        final Set<Check> checks = new LinkedHashSet<Check>();
        this.addCheck(checks, new BinaryFilesCheck());
        this.addCheck(checks, new JigsawCssCheck());
        this.addCheck(checks, new JSStaticCheck());
        this.addCheck(checks, new FilesStructureCheck());
        this.addCheck(checks, new WebXmlCheck());
        this.addCheck(checks, new RexslFilesCheck());
        this.addCheck(checks, new XhtmlOutputCheck(this.test));
        this.addCheck(checks, new InContainerScriptsCheck(this.test));
        this.addCheck(checks, new JSUnitTestsCheck());
        return checks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTest(final String scope) {
        this.test = scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCheck(final String chck) {
        if (chck != null) {
            try {
                this.check = (Class<? extends Check>) Class.forName(
                    String.format("com.rexsl.maven.checks.%s", chck)
                );
            } catch (ClassNotFoundException cnfe) {
                throw new IllegalArgumentException(cnfe);
            }
        }
    }

    /**
     * Adds a Check object to the Check set. If the check variable is not
     * null it will only add the check if it pertains to that class.
     *
     * @param checks The set of checks.
     * @param chck The check to be added.
     */
    private void addCheck(final Set<Check> checks, final Check chck) {
        if (this.check == null) {
            checks.add(chck);
        } else if (chck.getClass().equals(this.check)) {
            checks.add(chck);
        }
    }

}
