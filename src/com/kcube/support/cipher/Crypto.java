package com.kcube.support.cipher;

public interface Crypto {

	/**
	 *
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	String encrypt(final String plainText) throws Exception;

	/**
	 *
	 * @param cypherText
	 * @return
	 * @throws Exception
	 */
	String decrypt(final String cypherText) throws Exception;
}
