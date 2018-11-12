package com.kcube.support.cipher;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.kcube.support.util.AlertUtil;

public class AES implements Crypto {

	private static final String CHARSET_NAME = "UTF-8";
	private static final String ALGORITHM = "AES";
	private Key secureKey;

	public AES(String key) throws UnsupportedEncodingException {
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

		this.secureKey = keySpec;
	}

	@Override
	public String encrypt(String plainText) throws Exception {
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secureKey);

			final byte[] cipherText = cipher.doFinal(plainText.getBytes(CHARSET_NAME));
			final String encryptedString = new String(Base64.getEncoder().encode(cipherText));

			return encryptedString;
		} catch (InvalidKeyException e) {
			AlertUtil.showAndWaitForError("개인키 입력 에러", "입력한 키 길이가 올바르지 않습니다.");
			return "";
		}
	}

	@Override
	public String decrypt(String cypherText) throws Exception {
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, secureKey);

	        final byte[] cipherText = Base64.getDecoder().decode(cypherText.getBytes());
	        final String decryptedString = new String(cipher.doFinal(cipherText), CHARSET_NAME);

	        return decryptedString;
		} catch (InvalidKeyException e) {
			AlertUtil.showAndWaitForError("개인키 입력 에러", "입력한 키 길이가 올바르지 않습니다.");
			return "";
		}
	}
}
