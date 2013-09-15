/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.Mac;


/**
 * This class handles methods to crypt (not decrypt) messages with HmacSha256 algorithm (very efficient:
 * 105000/s).<br>
 * <br>
 * Usage:<br>
 * <ul>
 * <li>Create a HmacSha256 object: HmacSha256 key = new HmacSha256();</li>
 * <li>Create a key:
 * <ul>
 * <li>Generate: key.generateKey();<br>
 * The method key.getSecretKeyInBytes() allow getting the key in Bytes.</li>
 * <li>From an external source: key.setSecretKey(arrayOfBytes);</li>
 * </ul>
 * </li>
 * <li>To crypt a String in a Base64 format: String myStringCrypt = key.cryptToString(myString);</li>
 * </ul>
 * 
 * @author frederic bregier
 * 
 */
public class HmacSha256 extends KeyObject {
	public final static int KEY_SIZE = 128;
	public final static String ALGO = "HmacSHA256";
	public final static String INSTANCE = ALGO;
	public final static String EXTENSION = "hs2";

	/*
	 * (non-Javadoc)
	 * @see atlas.cryptage.KeyObject#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		return ALGO;
	}

	/*
	 * (non-Javadoc)
	 * @see atlas.cryptage.KeyObject#getInstance()
	 */
	@Override
	public String getInstance() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see atlas.cryptage.KeyObject#getKeySize()
	 */
	@Override
	public int getKeySize() {
		return KEY_SIZE;
	}

	/* (non-Javadoc)
	 * @see org.waarp.common.crypto.KeyObject#toCrypt()
	 */
	@Override
	public Cipher toCrypt() {
		throw new IllegalArgumentException("Cannot be used for HmacSha256");
	}

	/* (non-Javadoc)
	 * @see org.waarp.common.crypto.KeyObject#crypt(byte[])
	 */
	@Override
	public byte[] crypt(byte[] plaintext) throws Exception {
		Mac mac = Mac.getInstance(ALGO);
		mac.init(secretKey);
		return mac.doFinal(plaintext);
	}

	/* (non-Javadoc)
	 * @see org.waarp.common.crypto.KeyObject#toDecrypt()
	 */
	@Override
	public Cipher toDecrypt() {
		throw new IllegalArgumentException("Cannot be used for HmacSha256");
	}

	/* (non-Javadoc)
	 * @see org.waarp.common.crypto.KeyObject#decrypt(byte[])
	 */
	@Override
	public byte[] decrypt(byte[] ciphertext) throws Exception {
		throw new IllegalArgumentException("Cannot be used for HmacSha256");
	}

	/**
	 * This method allows to test the correctness of this class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String plaintext = null;
		if (args.length != 0) {
			plaintext = args[0];
		}
		if (plaintext == null || plaintext.length() == 0) {
			plaintext = "This is a try for a very long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long String";
		}
		System.out.println("plaintext = " + plaintext);
		HmacSha256 hmacSha256 = new HmacSha256();
		// Generate a key
		hmacSha256.generateKey();
		// get the generated key
		byte[] secretKey = hmacSha256.getSecretKeyInBytes();
		// crypt one text
		byte[] ciphertext = hmacSha256.crypt(plaintext);
		// print the cipher
		System.out.println("ciphertext = " + hmacSha256.encodeHex(ciphertext));

		// Test the set Key
		hmacSha256.setSecretKey(secretKey);

		// same on String only
		int nb = 100000;
		int k = 0;
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < nb; i++) {
			String cipherString = hmacSha256.cryptToHex(plaintext);
			k += cipherString.length();
			// System.out.println("cipherString = " + cipherString);
		}
		long time2 = System.currentTimeMillis();
		System.out.println("Total time in ms: " + (time2 - time1) + " or "
				+ (nb * 1000 / (time2 - time1)) + " crypt/s for "+ (k/nb));
	}

}
