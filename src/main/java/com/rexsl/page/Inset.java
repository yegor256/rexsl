/**
 * Copyright (c) 2011-2015, ReXSL.com
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
package com.rexsl.page;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.core.Response;

/**
 * Insertion into a page.
 *
 * <p>Post-rendering strategy applied to a {@link BasePage} and
 * {@link Response.Builder}. You can define your own insets as anonymous
 * classes, for example:
 *
 * <pre> &#64;Inset.Default({ LinksInset.class, FlashInset.class })
 * public class BaseRs extends BaseResource {
 *   &#64;Inset.Runtime
 *   public Inset ver() {
 *     return new VersionInset("1.0", "", "13-Mar-2013");
 *   }
 *   &#64;Inset.Runtime
 *   public Inset supplementary() {
 *     return new Inset() {
 *       &#64;Override
 *       public void render(final BasePage&lt;?, ?&gt; page,
 *         final Response.ResponseBuilder builder) {
 *         builder.type(MediaType.TEXT_XML);
 *         builder.header(HttpHeaders.VARY, "Cookie");
 *     }
 *   };
 * }</pre>
 *
 * <p>For every new page methods {@code version()} and {@code supplementary()}
 * will be called. Returned insets will be used to help in page rendering.
 * Their {@code render(BasePage,Response.ResponseBuilder)} methods will be
 * used to extend the JAXB page and the JAX-RS response.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @see BasePage
 */
public interface Inset {

    /**
     * Annotates a method of JAX-RS resource that returns an instance
     * of {@code Inset}.
     *
     * <p>The method will be called automatically for every page rendered. The
     * returned {@code Inset} will be used to render the page. If an annotated
     * method returns an object, which is not a instance of {@code Inset} a
     * runtime exception will be thrown.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Runtime {
    }

    /**
     * Annotates a JAX-RS resource, informing the rendering mechanism about
     * all Inset classes required for page rendering.
     *
     * <p>All classes listed in the annotation will be instantiated with
     * one-argument constructors. If such constructor is absent a runtime
     * exception is thrown.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Default {
        /**
         * Class to instantiate.
         */
        Class<? extends Inset>[] value();
    }

    /**
     * Render it into the response builder.
     * @param page The page to render
     * @param builder The builder
     */
    void render(BasePage<?, ?> page, Response.ResponseBuilder builder);

}
