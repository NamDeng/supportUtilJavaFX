package com.kcube.support.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.ValidationException;

/**
 * 로그 파일 생성을 위한 class
 *
 * @author whnam
 */
public class LogFileUtil {
	private PrintWriter out = null;
	private BufferedWriter bufWriter = null;

	public LogFileUtil(String path) throws IOException, ValidationException {
		this.bufWriter = Files.newBufferedWriter(Paths.get(path),
				Charset.forName("UTF-8"),
				StandardOpenOption.WRITE,
				StandardOpenOption.APPEND,
				StandardOpenOption.CREATE);

		out = new PrintWriter(bufWriter);
	}

	public void write(String text) throws Exception {
		out.print(text);
	}

	public void writeln(String text) throws Exception {
		out.println(text);
	}

	public void close() throws Exception {
		out.close();
	}
}
