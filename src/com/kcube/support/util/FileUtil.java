package com.kcube.support.util;

import java.nio.file.Path;

public class FileUtil {

	/**
	 *  파일 확장자 확인
	 *
	 * @param path
	 * @param extension
	 * @return
	 */
	public static boolean checkExteionsion(String path, String extension) {
		int lastIndexOf = path.lastIndexOf(".");
		if (lastIndexOf == -1)
			return false;
		if (!path.substring(lastIndexOf + 1).equals(extension))
			return false;

		return true;
	}

	/**
	 * conf.xml 파일인지 확인
	 * @param path
	 * @return
	 */
	public static boolean isAppConfXML(Path path) {
		final String fileName = path.getFileName().toString();

		return fileName.contains(".conf.xml");
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static boolean isFile(final Path path) {
		return path.toString().contains(".");
	}
}
