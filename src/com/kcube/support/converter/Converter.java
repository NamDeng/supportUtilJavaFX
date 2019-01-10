package com.kcube.support.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringEscapeUtils;

public class Converter {

	/**
	 * 파일 내용을 Unicode 변경하여 생성한다.
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
