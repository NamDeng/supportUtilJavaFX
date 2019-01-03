package com.kcube.support.cipher;

import java.security.Key;
import java.security.KeyException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.kcube.support.util.AlertUtil;

public class AES implements Crypto {


	private static final int AES128_KEY_LENGTH = 16;
	private static final int AES192_KEY_LENGTH = 24;
	private static final int AES256_KEY_LENGTH = 32;

	private static final String ALGORITHM = "AES";
	private static final String CHARSET_NAME = "UTF-8";

	private Key secureKey;

	public AES(final String method, final String key) throws Exception {
		validateSecretKey(method, key);

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		this.secureKey = keySpec;
	}

	@Override
	public String encrypt(final String plainText) throws Exception {
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secureKey);

			final byte[] cipherText = cipher.doFinal(plainText.getBytes(CHARSET_NAME));
			final String encryptedString = new String(Base64.getEncoder().encode(cipherText));

			return encryptedString;
		} catch (IllegalArgumentException e) {
			AlertUtil.showAndWaitForError("입력값이 올바르지 않습니다.");
			throw new Exception();
		}
	}

	@Override
	public String decrypt(final String cypherText) throws Exception {
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secureKey);

			final byte[] cipherText = Base64.getDecoder().decode(cypherText.getBytes());
			final String decryptedString = new String(cipher.doFinal(cipherText), CHARSET_NAME);

			return decryptedString;
		} catch (IllegalArgumentException e) {
			AlertUtil.showAndWaitForError("입력값이 올바르지 않습니다.");
			throw new Exception();
		}
	}

	private void validateSecretKey(final String method, final String key) throws KeyException {
		final int length = key.getBytes().length;
		if (method.equals("AES-128")) {
			if (length != AES128_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-128 키 길이는 16 byte 입니다.");
				throw new KeyException("AES-128 키 길이를 확인해주세요.");
			}
		} else if (method.equals("AES-192")) {
			if (length != AES192_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-192 키 길이는 24 byte 입니다.");
				throw new KeyException("AES-192 키 길이를 확인해주세요.");
			}
		} else if (method.equals("AES-256")) {
			if (length != AES256_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-256 키 길이는 32 byte 입니다.");
				throw new KeyException("AES-256 키 길이를 확인해주세요.");
			}
		}
	}
}
