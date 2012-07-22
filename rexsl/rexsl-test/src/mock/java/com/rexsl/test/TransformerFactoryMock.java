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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import org.mockito.Mockito;

/**
 * Mock for TransformerFactory. Returns Transformer which throws
 * IllegalStateException.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @checkstyle FinalParametersCheck (500 lines)
 * @checkstyle DesignForExtensionCheck (500 lines)
 * @checkstyle ParameterNameCheck (500 lines)
 * @checkstyle ParameterNumberCheck (500 lines)
 */
@SuppressWarnings({
    "PMD.MethodArgumentCouldBeFinal",
    "PMD.UncommentedEmptyMethod"
}
)
public class TransformerFactoryMock extends TransformerFactory {

    @Override
    public Transformer newTransformer(Source source)
        throws TransformerConfigurationException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transformer newTransformer()
        throws TransformerConfigurationException {
        final Transformer transformer = Mockito.mock(Transformer.class);
        try {
            Mockito
                .doThrow(new TransformerException("mock"))
                .when(transformer)
                .transform(
                    Mockito.any(Source.class),
                    Mockito.any(Result.class)
                );
        } catch (TransformerException ex) {
            throw new TransformerConfigurationException(ex);
        }
        return transformer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Templates newTemplates(Source source)
        throws TransformerConfigurationException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getAssociatedStylesheet(
        Source source,
        String s,
        String s1,
        String s2
    ) throws TransformerConfigurationException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIResolver(URIResolver uriResolver) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URIResolver getURIResolver() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeature(String name, boolean enabled)
        throws TransformerConfigurationException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFeature(String name) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object value) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorListener(ErrorListener errorListener) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }
}
