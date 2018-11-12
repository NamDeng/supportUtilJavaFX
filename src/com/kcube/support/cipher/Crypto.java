package com.kcube.support.cipher;

public interface Crypto {

	/**
	 *
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	String encrypt(String plainText) throws Exception;

	/**
	 *
	 * @param cypherText
	 * @return
	 * @throws Exception
	 */
	String decrypt(String cypherText) throws Exception;
}
