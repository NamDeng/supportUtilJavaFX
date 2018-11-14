package com.kcube.support.jdk;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.kcube.support.util.AlertUtil;

public class JDK {
	private static final Map<Integer, String> version = new HashMap<>();

	static {
		version.put(46, "JDK 1.4.2");
		version.put(47, "JDK 1.3");
		version.put(48, "JDK 1.4");
		version.put(49, "JDK 1.5");
		version.put(50, "JDK 1.6");
		version.put(51, "JDK 1.7");
		version.put(52, "JDK 1.8");
		version.put(53, "JDK 1.9");
		version.put(54, "JDK 10");
		version.put(55, "JDK 11");
	}

	public static String getVersion(final int majorVersion) {
		if(!version.containsKey(majorVersion))
			AlertUtil.showAndWaitForError("jdk version error!", "jdk 버전이 존재하지 않습니다.");

		return version.get(majorVersion);
	}

	public static boolean isClassFile(String path) {
		int lastIndexOf = path.lastIndexOf(".");
		if(lastIndexOf == -1)
			return false;
		if(!path.substring(lastIndexOf + 1).equals("class"))
			return false;
		return true;
	}

	public static String getClassFileVersion(final String path) {
		final StringJoiner stringJoiner = new StringJoiner(" : ");
		try (DataInputStream in = new DataInputStream(new FileInputStream(path));) {
			int magicNum = in.readInt();
			if (magicNum != 0xcafebabe) {
				stringJoiner.add(Paths.get(path).getFileName().toString());
				stringJoiner.add("올바르지 않은 파일입니다." + System.getProperty("line.separator"));

				return stringJoiner.toString();
			}
			@SuppressWarnings("unused")
			final int minorVersion = in.readUnsignedShort();
			final int majorVersion = in.readUnsignedShort();
			stringJoiner.add(Paths.get(path).getFileName().toString());
			stringJoiner.add(getVersion(majorVersion) + System.getProperty("line.separator"));
		} catch (Exception e) {
			return e.getMessage();
		}
		return stringJoiner.toString();
	}
}

