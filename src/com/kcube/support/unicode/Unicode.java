package com.kcube.support.unicode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
		try (FileReader fileReader = new FileReader(source);
				FileWriter fileWriter = new FileWriter(dest);
				BufferedReader buffReader = new BufferedReader(fileReader)) {
			String line = "";

			while ((line = buffReader.readLine()) != null) {
				fileWriter.write(StringEscapeUtils.escapeJava(line));
				fileWriter.write(System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
