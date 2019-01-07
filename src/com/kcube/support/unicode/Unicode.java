package com.kcube.support.unicode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringEscapeUtils;

public class Unicode {

	/**
	 * properties 파일인지 확인한다.
	 *
	 * @param path
	 * @return
	 */
	public static boolean isPropertiesFile(final String path) {
		int lastIndexOf = path.lastIndexOf(".");
		if (lastIndexOf == -1)
			return false;
		if (!path.substring(lastIndexOf + 1).equals("properties"))
			return false;

		return true;
	}

	/**
	 *
	 * properties 파일 내용을 Unicode로 변경하여 파일을 생성한다.
	 *
	 * @param source
	 * @param dest
	 */
	public static void convertToUnicodeFile(final File source, final File dest) {
		try (BufferedReader buffReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8));
			BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dest), StandardCharsets.UTF_8))) {

			String line = "";
			while ((line = buffReader.readLine()) != null) {
				bufferedWriter.write(StringEscapeUtils.escapeJava(line));
				bufferedWriter.write(System.lineSeparator());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
