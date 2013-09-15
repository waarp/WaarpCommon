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

/**
 * This class handles methods to crypt and decrypt messages with AES algorithm (very efficient:
 * 60000/s).<br>
 * <br>
 * Usage:<br>
 * <ul>
 * <li>Create a Aes object: Aes key = new Aes();</li>
 * <li>Create a key:
 * <ul>
 * <li>Generate: key.generateKey();<br>
 * The method key.getSecretKeyInBytes() allow getting the key in Bytes.</li>
 * <li>From an external source: key.setSecretKey(arrayOfBytes);</li>
 * </ul>
 * </li>
 * <li>To crypt a String in a Base64 format: String myStringCrypt = key.cryptToString(myString);</li>
 * <li>To decrypt one string from Base64 format to the original String: String myStringDecrypt =
 * key.decryptStringInString(myStringCrypte);</li>
 * </ul>
 * 
 * @author frederic bregier
 * 
 */
public class Aes extends KeyObject {
	/**
	 * This value could be between 32 and 128 due to license limitation.
	 */
	public final static int KEY_SIZE = 128; // [32..448]
	public final static String ALGO = "AES";
	public final static String INSTANCE = "AES/ECB/PKCS5Padding";
	public final static String EXTENSION = "aes";

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
		Aes aes = new Aes();
		// Generate a key
		aes.generateKey();
		// get the generated key
		byte[] secretKey = aes.getSecretKeyInBytes();
		// crypt one text
		byte[] ciphertext = aes.crypt(plaintext);
		// print the cipher
		System.out.println("ciphertext = " + aes.encodeHex(ciphertext));

		// Test the set Key
		aes.setSecretKey(secretKey);
		// decrypt the cipher
		String plaintext2 = aes.decryptInString(ciphertext);
		// print the result
		System.out.println("plaintext2 = " + plaintext2);
		if (!plaintext2.equals(plaintext))
			System.out.println("Error: plaintext2 != plaintext");

		// same on String only
		int nb = 100000;
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < nb; i++) {
			String cipherString = aes.cryptToHex(plaintext);
			// System.out.println("cipherString = " + cipherString);
			String plaintext3 = aes.decryptHexInString(cipherString);
			// System.out.println("plaintext3 = " + plaintext3);
			if (!plaintext3.equals(plaintext))
				System.out.println("Error: plaintext3 != plaintext");
		}
		long time2 = System.currentTimeMillis();
		System.out.println("Total time in ms: " + (time2 - time1) + " or "
				+ (nb * 1000 / (time2 - time1)) + " crypt or decrypt/s");
	}

}
