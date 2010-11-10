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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    Key secretKey = null;
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
     *
     * @return True if this key is ready to be used
     */
    public boolean keyReady() {
        return secretKey != null;
    }
    /**
     * Returns the key as an array of bytes in order to be stored somewhere else
     * and retrieved using the setSecretKey(byte[] keyData) method.
     * @return the key as an array of bytes (or null if not ready)
     */
    public byte[] getSecretKeyInBytes() {
        if (keyReady()) {
            return secretKey.getEncoded();
        } else {
            return null;
        }
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
            DataInputStream dis = new DataInputStream(inputStream);
            dis.readFully(key);
            dis.close();
            this.setSecretKey(key);
        } else {
            throw new CryptoException("Cannot read crypto file");
        }
    }
    /**
     * Save a Key to a File
     * @param file
     * @throws CryptoException
     * @throws IOException
     */
    public void saveSecretKey(File file) throws CryptoException, IOException {
        if (keyReady() && ((!file.exists()) || file.canWrite())) {
            byte []key = getSecretKeyInBytes();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(key);
            outputStream.flush();
            outputStream.close();
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
        if (! keyReady()) {
            throw new CryptoException("Key not Ready");
        }
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
     * Crypt one array of bytes and returns the crypted String as Base64 format
     *
     * @param plaintext
     * @return the crypted String as Base64 format
     * @throws Exception
     */
    public String cryptToBase64(byte[] plaintext) throws Exception {
        byte []result = crypt(plaintext);
        return encoder.encode(result);
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
    public String cryptToBase64(String plaintext) throws Exception {
        return cryptToBase64(plaintext.getBytes());
    }

    /**
     * Decrypt an array of bytes and returns the uncrypted array of bytes
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        if (! keyReady()) {
            throw new CryptoException("Key not Ready");
        }
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
        return new String(decrypt(ciphertext));
    }

    /**
     * Decrypt a String as Base64 format representing a crypted array of bytes and
     * returns the uncrypted array of bytes
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public byte[] decryptBase64InBytes(String ciphertext) throws Exception {
        byte[] arrayBytes = decoder.decodeBuffer(ciphertext);
        return decrypt(arrayBytes);
    }
    /**
     * Decrypt an array of bytes as Base64 format representing a crypted array of bytes and
     * returns the uncrypted array of bytes
     *
     * @param ciphertext
     * @return the uncrypted array of bytes
     * @throws Exception
     */
    public byte[] decryptBase64InBytes(byte[] ciphertext) throws Exception {
        byte[] arrayBytes = decoder.decodeBuffer(new String(ciphertext));
        return decrypt(arrayBytes);
    }
    /**
     * Decrypt a String as Base64 format representing a crypted array of bytes and
     * returns the uncrypted String
     *
     * @param ciphertext
     * @return the uncrypted String
     * @throws Exception
     */
    public String decryptBase64InString(String ciphertext) throws Exception {
        return new String(decryptBase64InBytes(ciphertext));
    }

    /**
     * Decode from a file containing a BASE64 crypted string
     * @param file
     * @return the decoded uncrypted content of the file
     * @throws Exception
     */
    public byte[] decryptBase64File(File file) throws Exception {
        byte [] byteKeys = new byte[(int) file.length()];
        FileInputStream inputStream = null;
        DataInputStream dis = null;
        try {
            inputStream = new FileInputStream(file);
            dis = new DataInputStream(inputStream);
            dis.readFully(byteKeys);
            dis.close();
            String skey = new String(byteKeys);
            // decrypt it
            byteKeys = decryptBase64InBytes(skey);
            return byteKeys;
        } catch (IOException e) {
            try {
                if (dis != null) {
                    dis.close();
                } else if (inputStream != null)
                    inputStream.close();
            } catch (IOException e1) {
            }
            throw e;
        }
    }

    /**
    *
    * @param encoded
    * @return the array of bytes from encoded String (BASE64)
    */
   public byte[] decodeBase64(String encoded) {
       try {
           return decoder.decodeBuffer(encoded);
       } catch (IOException e) {
           return null;
       }
   }

    /**
     *
     * @param bytes
     * @return The encoded array of bytes in BASE64
     */
    public String encodeBase64(byte[] bytes) {
        return encoder.encode(bytes);
    }
}
