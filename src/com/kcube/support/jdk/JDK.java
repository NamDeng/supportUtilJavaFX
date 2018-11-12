package com.kcube.support.jdk;

import java.util.HashMap;
import java.util.Map;

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
}
