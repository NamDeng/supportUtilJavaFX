package com.kcube.support.cipher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA implements Crypto {

	private String method;

	public SHA(String method) {
		this.method = method;
	}

	@Override
	public String encrypt(String plainText) throws NoSuchAlgorithmException {
		final MessageDigest messageDigest = MessageDigest.getInstance(method);
		messageDigest.update(plainText.getBytes());
		final byte digestBytes[] = messageDigest.digest();

		final StringBuffer hashCodeBuffer = new StringBuffer();
		for (int i = 0; i < digestBytes.length; i++) {
			hashCodeBuffer.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return hashCodeBuffer.toString();
	}

	@Override
	public String decrypt(String cypherText) throws Exception {
		return null;
	}
}
