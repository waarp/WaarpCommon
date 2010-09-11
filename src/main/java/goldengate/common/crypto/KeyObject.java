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

import java.math.BigInteger;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

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
 * <li>To crypt a String in a BigInteger.toString() format: String myStringCrypt = key.cryptToString(myString);</li>
 * <li>To decrypt one string from BigInteger.toString() format to the original String: String myStringDecrypt = key.decryptStringInString(myStringCrypte);</li>
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
     * Generate a key from nothing
     * @throws Exception
     */
    public void generateKey() throws Exception {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(getInstance());
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
            Cipher cipher = Cipher.getInstance(getAlgorithm());
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
     * Crypt one String and returns the crypted String as Integer format
     *
     * @param plaintext
     * @return the crypted String as Integer format
     * @throws Exception
     */
    public String cryptToString(String plaintext) throws Exception {
        byte []result = crypt(plaintext.getBytes());
        return new BigInteger(result).toString();
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
     * Decrypt a String as Integer format representing a crypted array of bytes and
     * returns the uncrypted String
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public String decryptStringInString(String ciphertext) throws Exception {
        BigInteger integer = new BigInteger(ciphertext);
        byte[] arrayBytes = integer.toByteArray();
        return new String(decryptInBytes(arrayBytes));
    }
}
