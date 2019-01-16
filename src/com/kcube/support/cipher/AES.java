package com.kcube.support.cipher;

import java.security.Key;
import java.security.KeyException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import com.kcube.support.util.AlertUtil;

public class AES implements Crypto {


	private static final int AES128_KEY_LENGTH = 16;
	private static final int AES192_KEY_LENGTH = 24;
	private static final int AES256_KEY_LENGTH = 32;

	private static final String ALGORITHM = "AES";
	private static final String CHARSET_NAME = "UTF-8";

	private Key secretKey;
	private int secretKeyLength;

	public AES(final String method, final String key) throws Exception {
		validateSecretKey(method, key);

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		this.secretKey = keySpec;
	}

	@Override
	public String encrypt(final String plainText) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] cipherText = cipher.doFinal(plainText.getBytes(CHARSET_NAME));
			return bytesToHex(cipherText);
		} catch (IllegalBlockSizeException | IllegalArgumentException e) {
			AlertUtil.showAndWaitForError("입력값을 확인해주세요.");
			throw new Exception();
		}
	}

	@Override
	public String decrypt(final String cypherText) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			return new String(cipher.doFinal(hexToBytes(cypherText)));
		} catch (IllegalBlockSizeException | IllegalArgumentException e) {
			AlertUtil.showAndWaitForError("입력값을 확인해주세요.");
			throw new Exception();
		}
	}

	private void validateSecretKey(final String method, final String key) throws KeyException {
		secretKeyLength = key.getBytes().length;
		if (method.equals("AES-128")) {
			if (secretKeyLength != AES128_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-128 키 길이는 16 byte 입니다.");
				throw new KeyException("AES-128 키 길이를 확인해주세요.");
			}
		} else if (method.equals("AES-192")) {
			if (secretKeyLength != AES192_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-192 키 길이는 24 byte 입니다.");
				throw new KeyException("AES-192 키 길이를 확인해주세요.");
			}
		} else if (method.equals("AES-256")) {
			if (secretKeyLength != AES256_KEY_LENGTH) {
				AlertUtil.showAndWaitForError("키 길이를 확인해주세요.\nAES-256 키 길이는 32 byte 입니다.");
				throw new KeyException("AES-256 키 길이를 확인해주세요.");
			}
		}
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	private String bytesToHex(byte[] data)
	{
		if (data == null)
		{
			return null;
		}
		int len = data.length;
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < len; i++)
		{
			if ((data[i] & 0xFF) < 16)
			{
				str.append("0").append(Integer.toHexString(data[i] & 0xFF));
			}
			else
			{
				str.append(Integer.toHexString(data[i] & 0xFF));
			}
		}

		return str.toString();
	}

	/**
	 * hex string을 bytes array로 변환한다.
	 */
	public static byte[] hexToBytes(String str)
	{
		if (str == null || str.length() < 2)
		{
			return null;
		}
		int len = str.length() / 2;
		byte[] buffer = new byte[len];
		for (int i = 0; i < len; i++)
		{
			buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
		}
		return buffer;

	}
}
