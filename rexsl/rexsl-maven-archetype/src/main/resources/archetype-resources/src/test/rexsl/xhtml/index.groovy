/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.xhtml

import com.rexsl.test.XhtmlMatchers
import org.hamcrest.MatcherAssert

MatcherAssert.assertThat(
    rexsl.document,
    XhtmlMatchers.hasXPaths(
        '//xhtml:div[@id="version"]',
        '//xhtml:div[@id="version" and contains(.,"123")]',
        '//xhtml:div[@id="version" and contains(.,"16ms")]'
    )
)
