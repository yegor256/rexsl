/**
 * Copyright (c) 2011-2014, ReXSL.com
 * All rights reserved.
 */
package ${package};

import com.rexsl.page.BasePage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base RESTful page.
 *
 * <p>All other JAXB pages are inherited from this class, in runtime,
 * by means of {@link com.rexsl.page.PageBuilder}.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class EmptyPage extends BasePage<EmptyPage, BaseRs> {

}
