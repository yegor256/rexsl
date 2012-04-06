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
package com.rexsl.trap;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Mocker of notifier.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 */
public final class NotifierMocker implements Notifier {

    /**
     * All instances of this class, in order of instantiating.
     */
    private static final List<NotifierMocker> INSTANCES =
        new LinkedList<NotifierMocker>();

    /**
     * Properties.
     */
    private final transient Properties props;

    /**
     * Public ctor.
     * @param prps The properties
     */
    public NotifierMocker(final Properties prps) {
        this.props = prps;
        NotifierMocker.INSTANCES.add(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final String report) {
        // nothing to do
    }

    /**
     * Get recent instance instantiated and removes it from the pool
     * (returns NULL if there are no more instances).
     * @return The instance or NULL
     */
    public static NotifierMocker poll() {
        NotifierMocker notifier = null;
        if (!NotifierMocker.INSTANCES.isEmpty()) {
            final int pos = NotifierMocker.INSTANCES.size() - 1;
            notifier = NotifierMocker.INSTANCES.get(pos);
            NotifierMocker.INSTANCES.remove(pos);
        }
        return notifier;
    }

    /**
     * Get properties.
     * @return The props
     */
    public Properties properties() {
        return this.props;
    }

}
