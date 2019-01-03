package com.kcube.support.util;

/**
 * 커스텀 StringBuilder class
 *
 * @author whnam
 */
public class StringBuilderUtil {

	private StringBuilder stringBuilder;

	public StringBuilderUtil() {
		stringBuilder = new StringBuilder();
	}

	public void append(String str) {
		stringBuilder.append(str != null ? str : "");
	}

	public void appendLine(String str) {
		stringBuilder.append(str != null ? str : "").append(System.lineSeparator());
	}

	public String toString() {
		return stringBuilder.toString();
	}
}
