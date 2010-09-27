/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.common.crypto;

import goldengate.common.exception.CryptoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * This class handles method to crypt and decrypt using the chosen algorithm.<br>
 *
 * <br>
 * Usage:<br>
 * <ul>
 * <li>Create a Key object: KeyObject key = new KeyObject();</li>
 * <li>Create a key:
 * <ul>
 * <li>Generate: key.generateKey();<br>
 * The method key.getSecretKeyInBytes() allow getting the key in Bytes.</li>
 * <li>From an external source: key.setSecretKey(arrayOfBytes);</li>
 * </ul></li>
 * <li>To crypt a String in a Base64 format: String myStringCrypt = key.cryptToString(myString);</li>
 * <li>To decrypt one string from Base64 format to the original String: String myStringDecrypt = key.decryptStringInString(myStringCrypte);</li>
 * </ul>
 *
 * @author frederic bregier
 *
 */
public abstract class KeyObject {
    /**
     * The True Key associated with this object
     */
    Key secretKey;
    /**
     * Base64 encoder
     */
    BASE64Encoder encoder = new BASE64Encoder();
    /**
     * Base64 decoder
     */
    BASE64Decoder decoder = new BASE64Decoder();

    /**
     * Empty constructor
     */
    public KeyObject() {
    }

    /**
     *
     * @return the algorithm used (Java name)
     */
    public abstract String getAlgorithm();
    /**
     *
     * @return the instance used (Java name)
     */
    public abstract String getInstance();
    /**
     *
     * @return the size for the algorithm key
     */
    public abstract int getKeySize();
    /**
     * @return the key associated with this object
     */
    public Key getSecretKey() {
        return secretKey;
    }

    /**
     * Returns the key as an array of bytes in order to be stored somewhere else
     * and retrieved using the setSecretKey(byte[] keyData) method.
     */
    public byte[] getSecretKeyInBytes() {
        return secretKey.getEncoded();
    }

    /**
     * Set the secretKey
     *
     * @param secretKey
     */
    public void setSecretKey(Key secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Reconstruct a key from an array of bytes
     */
    public void setSecretKey(byte[] keyData) {
        secretKey = new SecretKeySpec(keyData, getAlgorithm());
    }

    /**
     * Create a Key from a File
     * @param file
     * @throws CryptoException
     * @throws IOException
     */
    public void setSecretKey(File file) throws CryptoException, IOException {
        if (file.canRead()) {
            int len = (int)file.length();
            byte []key = new byte[len];
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(file);
            int read = 0;
            int offset = 0;
            while (read > 0) {
                read = inputStream.read(key, offset, len);
                offset += read;
                if (offset < len) {
                    len -= read;
                } else {
                    break;
                }
            }
            if (read < -1) {
                // wrong
                throw new CryptoException("Wrong size when reading crypto file");
            }
            this.setSecretKey(key);
        } else {
            throw new CryptoException("Cannot read crypto file");
        }
    }
    /**
     * Generate a key from nothing
     * @throws Exception
     */
    public void generateKey() throws Exception {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(getAlgorithm());
            keyGen.init(getKeySize());
            secretKey = keyGen.generateKey();
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }

    /**
     * Crypt one array of bytes and returns the crypted array of bytes
     *
     * @param plaintext
     * @return the crypted array of bytes
     * @throws Exception
     */
    public byte[] crypt(byte[] plaintext) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(getInstance());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }

    /**
     * Crypt one String and returns the crypted array of bytes
     *
     * @param plaintext
     * @return the crypted array of bytes
     * @throws Exception
     */
    public byte[] crypt(String plaintext) throws Exception {
        return crypt(plaintext.getBytes());
    }

    /**
     * Crypt one String and returns the crypted String as Base64 format
     *
     * @param plaintext
     * @return the crypted String as Base64 format
     * @throws Exception
     */
    public String cryptToString(String plaintext) throws Exception {
        byte []result = crypt(plaintext.getBytes());
        return encoder.encode(result);
    }

    /**
     * Decrypt an array of bytes and returns the uncrypted array of bytes
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public byte[] decryptInBytes(byte[] ciphertext) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }

    /**
     * Decrypt an array of bytes and returns the uncrypted String
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public String decryptInString(byte[] ciphertext) throws Exception {
        return new String(decryptInBytes(ciphertext));
    }

    /**
     * Decrypt a String as Base64 format representing a crypted array of bytes and
     * returns the uncrypted String
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public String decryptStringInString(String ciphertext) throws Exception {
        byte[] arrayBytes = decoder.decodeBuffer(ciphertext);
        return new String(decryptInBytes(arrayBytes));
    }
}
