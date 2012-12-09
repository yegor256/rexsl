/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 */
package ${package}.rexsl.xhtml

import com.rexsl.test.XhtmlMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

MatcherAssert.assertThat(
    rexsl.document,
    XhtmlMatchers.hasXPaths(
        '//xhtml:div[@id="version"]',
        '//xhtml:div[@id="version" and contains(.,"123")]',
        '//xhtml:div[@id="version" and contains(.,"16ms")]'
    )
)
