package com.kcube.support.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommandUtil {

	private List<String> command;
	private Process process;
	private ProcessBuilder processBuilder;

	public CommandUtil(List<String> commandList) {
		this.command = commandList;

		this.processBuilder.command(this.command);
	}

	public CommandUtil(Path source, Path dest) {
		List<String> commandList = makeEncodingCommand(source, dest);

		this.processBuilder = new ProcessBuilder(commandList);
	}

	/**
	 * java native2ascii encode
	 *
	 * @param source
	 * @param dest
	 * @return
	 */
	private List<String> makeEncodingCommand(Path source, Path dest) {
		List<String> commandList = new ArrayList<>();
		commandList.add("native2ascii");
		commandList.add("-encoding");
		commandList.add("utf8");
		commandList.add(source.toString());
		commandList.add(dest.toString());

		return commandList;
	}

	/**
	 * start command
	 *
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean start() throws IOException, InterruptedException {
		this.process = this.processBuilder.start();

		return this.process.waitFor() == 0 ? true : false;
	}
}
