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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.constraints.NotNull;

/**
 * Policy of assertion, used by {@link TestResponse}.
 *
 * <p>Assertion policy is used in {@link TestResponse} in order to validate
 * REST response before further processing. Consider this example:
 *
 * <pre>
 * String data = RestTester.start(new URI("http://example.com"))
 *   .assertThat(retryOnFailure)
 *   .assertXPath("//data")
 *   .xpath("//data/text()")
 *   .get(0);
 * </pre>
 *
 * <p>This code will 1) retrieve the document from the provided
 * {@link java.net.URI}, 2) make sure that it has an XML node addressed by
 * {@code "//data"} XPath, 3) retrieve all nodes by that XPath, and
 * 4) use the first element in the found collection of texts. But, before
 * doing all this a {@code retryOnFailure} assertion policy will be used:
 *
 * <pre> AssertionPolicy retryOnFailure = new AssertionPolicy() {
 *   private boolean valid;
 *   &#64;Override
 *   public void assertThat(TestResponse response) {
 *     if (response.getStatus() != HttpURLConnection.HTTP_OK) {
 *       throw new AssertionError("invalid HTTP status, will try again");
 *     }
 *     this.valid = true;
 *   }
 *   &#64;Override
 *   public boolean isRetryNeeded(int attempt) {
 *     return !this.valid;
 *   }
 * }</pre>
 *
 * <p>{@link AssertionPolicy#assertThat(TestResponse)} will be called right
 * after the document is retrieved from the server. Actually, it will be called
 * even earlier, even before the HTTP request is made. In the example above
 * the request will be made inside {@code response.getStatus()}. If it will
 * fail for some reason (host not found, for example) - an exception will be
 * thrown and {@code this.valid} will stay in {@code FALSE} status. Thus, it
 * is a bullet-proof design, where you can control everything, including
 * network errors.
 *
 * <p>Implementation of this interface need NOT be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.4
 */
public interface AssertionPolicy {

    /**
     * Annotates a policy that should not produce a full exception trace
     * in log if it fails.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Quiet {
    }

    /**
     * Make an assertion and return nothing or throw {@link AssertionError}
     * if some problem is found.
     *
     * <p>Validity information about the response should be collected in
     * this method and stored in the object's variable. Later you should use
     * it in {@link #isRetryNeeded(int)} in order to inform
     * the caller of whether it
     * should retry the request or not.
     *
     * <p>If the method doesn't throw {@link AssertionError} it means that
     * everything went fine and {@link #isRetryNeeded(int)} will
     * never be called.
     *
     * @param response The response to assert
     */
    void assertThat(@NotNull TestResponse response);

    /**
     * Do we need to re-fetch the page and try again?
     *
     * <p>This method is called by
     * {@link TestResponse#assertThat(AssertionPolicy)} before the next
     * attempt, in order to check whether it is required. {@link TestResponse}
     * keeps track of attempts (so you don't have to do it here) and will stop
     * making them after {@link TestResponse#MAX_ATTEMPTS}. In most cases
     * you don't need to store this attempt number locally.
     * If {@code retryAgain()} returns {@code TRUE} {@link TestResponse}
     * will try again, otherwise it will throw an exception.
     *
     * @param attempt Number of attempt we're trying to make (will not ask for
     *  attempt #0, but will start from #1)
     * @return Yes, we should try again (if {@code TRUE})
     */
    boolean isRetryNeeded(int attempt);

}
