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
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Random;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
@EqualsAndHashCode(of = { "identity", "key" })
@Loggable(Loggable.DEBUG)
@Loggable.Quiet
final class Encrypted implements Identity {

    /**
     * Base32 encoder/decoder.
     */
    private static final Base32 CODER = new Base32(80, new byte[] {}, true);

    /**
     * Random generator.
     */
    private static final Random RND = new SecureRandom();

    /**
     * The user.
     */
    private final transient Identity identity;

    /**
     * Security key.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param idn The identity to encapsulate
     * @param secret Secret key for encryption
     */
    Encrypted(@NotNull final Identity idn,
        @NotNull final String secret) {
        this.identity = idn;
        this.key = secret;
    }

    @Override
    public URN urn() {
        return this.identity.urn();
    }

    @Override
    public String name() {
        return this.identity.name();
    }

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
        final DataOutputStream stream = new DataOutputStream(data);
        try {
            stream.writeUTF(this.urn().toString());
            stream.writeUTF(this.name());
            stream.writeUTF(this.photo().toString());
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return StringUtils.join(
            Encrypted.CODER.encodeToString(
                Encrypted.xor(
                    Encrypted.salt(data.toByteArray()),
                    this.key.getBytes(Charsets.UTF_8)
                )
            ).split("(?<=\\G.{8})"),
            "-"
        );
    }

    /**
     * Decrypt.
     * @param txt The text to decrypt
     * @param ekey Encryption key
     * @return Instance of the class
     * @throws Encrypted.DecryptionException If can't decrypt
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    public static Encrypted parse(final String txt, final String ekey)
        throws Encrypted.DecryptionException {
        if (txt == null) {
            throw new Encrypted.DecryptionException("text can't be NULL");
        }
        final byte[] bytes = Encrypted.CODER.decode(txt.replaceAll("- ", ""));
        final DataInputStream stream = new DataInputStream(
            new ByteArrayInputStream(
                Encrypted.unsalt(
                    Encrypted.xor(bytes, ekey.getBytes(Charsets.UTF_8))
                )
            )
        );
        try {
            final URN urn = new URN(stream.readUTF());
            final String name = stream.readUTF();
            final String photo = stream.readUTF();
            return new Encrypted(
                new Identity.Simple(urn, name, URI.create(photo)),
                ekey
            );
        } catch (final URISyntaxException ex) {
            throw new Encrypted.DecryptionException(ex);
        } catch (final IOException ex) {
            throw new Encrypted.DecryptionException(ex);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Salt the string.
     * @param text Original text to salt
     * @return Salted string
     */
    @SuppressWarnings("PMD.AvoidArrayLoops")
    private static byte[] salt(final byte[] text) {
        final byte size = (byte) Encrypted.RND.nextInt(Tv.TEN);
        final byte[] output = new byte[text.length + size + 2];
        output[0] = size;
        byte sum = (byte) 0;
        for (int idx = 0; idx < (int) size; ++idx) {
            output[idx + 1] = (byte) Encrypted.RND.nextInt();
            sum += output[idx + 1];
        }
        System.arraycopy(text, 0, output, size + 1, text.length);
        output[output.length - 1] = sum;
        return output;
    }

    /**
     * Un-salt the string.
     * @param text Salted text
     * @return Original text
     * @throws Encrypted.DecryptionException If salt is wrong
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    private static byte[] unsalt(final byte[] text)
        throws Encrypted.DecryptionException {
        if (text.length == 0) {
            throw new Encrypted.DecryptionException("empty input");
        }
        final int size = text[0];
        if (text.length < size + 2) {
            throw new Encrypted.DecryptionException(
                String.format(
                    "not enough bytes, text length is %d while %d required",
                    text.length, size + 2
                )
            );
        }
        byte sum = (byte) 0;
        for (int idx = 0; idx < size; ++idx) {
            sum += text[idx + 1];
        }
        if (text[text.length - 1] != sum) {
            throw new Encrypted.DecryptionException("checksum failure");
        }
        final byte[] output = new byte[text.length - size - 2];
        System.arraycopy(text, size + 1, output, 0, output.length);
        return output;
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

}
