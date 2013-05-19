/**
 * Copyright (c) 2011-2013, ReXSL.com
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
package com.rexsl.page.auth;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base32;

/**
 * Encrypted identity.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4.8
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString(of = "identity")
@EqualsAndHashCode(of = { "identity", "key", "salt" })
@Loggable(Loggable.DEBUG)
final class Encrypted implements Identity {

    /**
     * Base32 encoder/decoder.
     */
    private static final Base32 CODER = new Base32(80, new byte[] {}, true);

    /**
     * The user.
     */
    private final transient Identity identity;

    /**
     * Security key.
     */
    private final transient String key;

    /**
     * Security salt.
     */
    private final transient String salt;

    /**
     * Public ctor.
     * @param idn The identity to encapsulate
     * @param secret Secret key for encryption
     * @param slt Salt for encryption
     */
    protected Encrypted(@NotNull final Identity idn,
        @NotNull final String secret, @NotNull final String slt) {
        this.identity = idn;
        this.key = secret;
        this.salt = slt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN urn() {
        return this.identity.urn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.identity.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI photo() {
        return this.identity.photo();
    }

    /**
     * Get its value for cookie.
     * @return The value
     */
    public String cookie() {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            final DataOutputStream stream = new DataOutputStream(data);
            stream.writeUTF(this.urn().toString());
            stream.writeUTF(this.name());
            stream.writeUTF(this.photo().toString());
            stream.writeUTF(this.salt);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        return Encrypted.CODER.encodeToString(
            Encrypted.xor(data.toByteArray(), this.key.getBytes())
        );
    }
    /**
     * Decrypt.
     * @param txt The text to decrypt
     * @param key Encryption key
     * @param salt Encryption salt
     * @return Instance of the class
     * @throws Encrypted.DecryptionException If can't decrypt
     */
    public static Encrypted parse(final String txt,
        final String key, final String salt)
        throws Encrypted.DecryptionException {
        if (txt == null) {
            throw new Encrypted.DecryptionException("text can't be NULL");
        }
        final byte[] bytes = Encrypted.CODER.decode(txt);
        final DataInputStream stream = new DataInputStream(
            new ByteArrayInputStream(
                Encrypted.xor(bytes, key.getBytes())
            )
        );
        try {
            final URN urn = new URN(stream.readUTF());
            final String name = stream.readUTF();
            final String photo = stream.readUTF();
            if (!salt.equals(stream.readUTF())) {
                throw new Encrypted.DecryptionException("invalid salt");
            }
            return new Encrypted(
                new Identity.Simple(urn, name, URI.create(photo)),
                key,
                salt
            );
        } catch (URISyntaxException ex) {
            throw new Encrypted.DecryptionException(ex);
        } catch (java.io.IOException ex) {
            throw new Encrypted.DecryptionException(ex);
        }
    }
    /**
     * Thrown by {@link Encrypted#valueOf(String)} if we can't decrypt.
     */
    public static final class DecryptionException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA781EDA1479L;
        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public DecryptionException(@NotNull final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public DecryptionException(@NotNull final Throwable cause) {
            super(cause);
        }
    }
    /**
     * XOR array of bytes.
     * @param input The input to XOR
     * @param secret Secret key
     * @return Encrypted output
     */
    private static byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            System.arraycopy(input, 0, output, 0, input.length);
        } else {
            int spos = 0;
            for (int pos = 0; pos < input.length; ++pos) {
                output[pos] = (byte) (input[pos] ^ secret[spos]);
                ++spos;
                if (spos >= secret.length) {
                    spos = 0;
                }
            }
        }
        return output;
    }
}
