package com.kcube.support.patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.util.AlertUtil;
import com.kcube.support.util.CommandUtil;
import com.kcube.support.util.FileUtil;
import com.kcube.support.util.LogFileUtil;
import com.kcube.support.util.StringBuilderUtil;

public class Patch {
	private static final String SRC = "src";
	private static final String WEB = "web";
	private static final String CST = "cst";
	private static final String BIN = "bin";
	private static final String EXT = "ext";

	private static final String WEB_INF = "WEB-INF";
	private static final String CLASSES = "classes";
	private static final String R5_APP = "r5-app";
	private static final String CUSTOMIZE = "customize";

	private Path webPath;
	private Path srcPath;
	private Path classPath;
	private Path binPath;
	private Path confPath;

	private String workspacePath;
	private String destPath;
	private String projectName;

	private LocalDate baseDate;

	public Patch(final String sourcePath, final String destPath, final String projectName,
				final LocalDate baseDate, final String sourceType) {
		this.workspacePath = sourcePath;
		this.destPath = destPath;
		this.projectName = projectName;
		this.baseDate = baseDate;

		init(sourceType);
	}

	/**
	 * 패치 파일 생성을 위한 경로를 초기화한다.
	 *
	 * @param sourceType
	 */
	private void init(final String sourceType) {
		final String SrcPath = sourceType.equals(EXT)
				? getPath(workspacePath, CST, this.projectName, SRC)
				: getPath(workspacePath, this.projectName, SRC);
		final String WebPath = sourceType.equals(EXT)
				? getPath(workspacePath, CST, this.projectName, WEB)
				: getPath(workspacePath, this.projectName, WEB);
		final String binPath = getPath(workspacePath, BIN);
		final String classPath = getPath(this.destPath, WEB_INF, CLASSES);
		final String confPath = getPath(this.destPath, WEB_INF, CLASSES, R5_APP, CUSTOMIZE);

		this.srcPath = Paths.get(SrcPath);
		this.webPath = Paths.get(WebPath);
		this.binPath = Paths.get(binPath);
		this.classPath = Paths.get(classPath);
		this.confPath = Paths.get(confPath);
	}

	/**
	 * 파일 경로를 만든다.
	 *
	 * @param paths
	 * @return
	 */
	String getPath(final String... paths) {
		final String fileSeparator = System.getProperty("file.separator");
		final StringJoiner sj = new StringJoiner(fileSeparator);
		for (String path : paths) {
			sj.add(path);
		}
		return sj.toString();
	}

	/**
	 * 변경 기준일 이후의 파일 목록을 가져온다.
	 *
	 * @param targetPath
	 * @param baseDate
	 * @return
	 * @throws Exception
	 */
	List<Path> getCopyFileList(final Path targetPath) throws Exception {
		final boolean isExists = Files.exists(targetPath);
		if (isExists == false) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "존재하지 않는 워크스페이스 경로입니다.");
			throw new FileNotFoundException();
		}

		return Files.walk(targetPath).filter(path -> {
			long lastModifiedTime = 0L;
			try {
				lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
			} catch (IOException e) {
			}
			final Instant instant = Instant.ofEpochMilli(lastModifiedTime);
			final LocalDate lastModifiedDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

			return lastModifiedDate.isAfter(this.baseDate);
		}).collect(Collectors.toList());
	}

	/**
	 * <pre>
	 * 프로젝스소스/WEB-INF/classes 디렉토리 밑으로 파일을 복사한다.
	 *
	 * [ src 파일 복사 규칙 ]
	 * 1. java 파일명과 매칭되는 class 파일 복사
	 * 2. properties 파일 native2ascii 파일 생성
	 * 3. app 설정 .conf.xml 파일은 customize 밑으로 복사
	 * 4. 그외 파일 복사.
	 * </pre>
	 *
	 * @param destPath
	 * @param fileList
	 * @throws Exception
	 */
	String copySrcFile(final List<Path> fileList, final LogFileUtil log) throws Exception {
		log.writeln("[ src ]");

		final StringBuilderUtil result = new StringBuilderUtil();
		fileList.stream().forEach(new Consumer<Path>() {

			@Override
			public void accept(Path path) {
				// srcPath를 기준으로 상대 경로
				final Path relativePath = getSrcPath().relativize(path);

				// 상대 경로를 기준으로 각 경로를 구한다(workspace bin, classes, conf)
				final Path binPath = getBinPath().resolve(relativePath);
				final Path classesPath = getClassPath().resolve(relativePath);
				try {
					if (FileUtil.isFile(classesPath)) {
						final String fileName = classesPath.getFileName().toString();
						final Path destParent = classesPath.getParent();

						if (FileUtil.checkExteionsion(classesPath.toString(), "java")) {
							if (Files.notExists(destParent)) {
								Files.createDirectories(destParent);
							}

							final String copyResult = copyClassFile(fileName, relativePath, destParent, log);
							result.appendLine(copyResult);
						} else if (FileUtil.checkExteionsion(classesPath.toString(), "properties")) {
							if (Files.notExists(destParent)) {
								Files.createDirectories(destParent);
							}

							final CommandUtil command = new CommandUtil(binPath, classesPath);
							if(command.start() == false)
								throw new Exception();

							result.appendLine(classesPath + " 파일 복사 성공");

							log.writeln(getPath(WEB_INF, CLASSES, relativePath.toString()));
						} else if (FileUtil.isAppConfXML(classesPath)) {
							if (Files.notExists(getConfPath())) {
								Files.createDirectories(getConfPath());
							}

							final Path confPath = getConfPath().resolve(fileName);
							Files.copy(binPath, confPath, StandardCopyOption.REPLACE_EXISTING);
							result.appendLine(confPath + " 파일 복사 성공");

							log.writeln(getPath(WEB_INF, CLASSES, R5_APP, CUSTOMIZE, fileName));
						} else {
							if (Files.notExists(destParent)) {
								Files.createDirectories(destParent);
							}

							Files.copy(binPath, classesPath, StandardCopyOption.REPLACE_EXISTING);
							result.appendLine(classesPath + " 파일 복사 성공");

							log.writeln(getPath(WEB_INF, CLASSES, relativePath.toString()));
						}
					}
				} catch (Exception e) {
					result.appendLine(classesPath + " 파일 복사 실패");
				}
			}
		});

		return result.toString();
	}

	/**
	 * 정규식에 매칭되는 class 파일을  복사
	 *
	 * @param fileName
	 * @param relativePath
	 * @param destParent
	 * @param log
	 * @return
	 * @throws Exception
	 */
	String copyClassFile(final String fileName, final Path relativePath, final Path destParent,
								final LogFileUtil log) throws Exception {

		// 확장자를 제외한 파일명 추출
		final String splitFileName = fileName.substring(0, fileName.lastIndexOf("."));
		// 정규식으로 복사할 class파일 binPath에서 찾아서 복사
		final String regex = "(^(" + splitFileName + ")(.class))|(^(" + splitFileName + ")(\\$){1}[a-zA-Z1-9]*(.class))";
		final Pattern pattern = Pattern.compile(regex);
		final Path binParent = binPath.getParent();

		final StringBuilderUtil result = new StringBuilderUtil();
		Files.walk(binParent).forEach(new Consumer<Path>() {

			@Override
			public void accept(Path sourceFile) {
				final String copyFileName = sourceFile.getFileName().toString();
				final Matcher matcher = pattern.matcher(copyFileName);
				if (matcher.find()) {
					final Path dest = destParent.resolve(sourceFile.getFileName());
					try {
						Files.copy(sourceFile, dest, StandardCopyOption.REPLACE_EXISTING);
						result.appendLine(dest + " 파일 복사 성공");

						log.writeln(getPath(WEB_INF, CLASSES, relativePath.getParent().toString(), copyFileName));
					} catch (Exception e) {
						result.appendLine(dest + " 파일 복사 실패");
					}
				}
			}
		});
		return result.toString();
	}

	/**
	 * web 소스 파일을 복사한다.
	 *
	 * @param destPath
	 * @param fileList
	 * @return
	 * @throws Exception
	 */
	String copyWebFile(final List<Path> fileList, final LogFileUtil log) throws Exception {
		log.writeln("[ web ]");

		final StringBuilderUtil result = new StringBuilderUtil();
		fileList.stream().forEach(new Consumer<Path>() {

			@Override
			public void accept(Path path) {
				// webPath를 기준으로 상대경로
				final Path relativePath = getWebPath().relativize(path);
				// 패치 파일 복사 경로
				final Path destPath = Paths.get(getDestPath()).resolve(relativePath);
				try {
					if (FileUtil.isFile(destPath)) {
						if (Files.notExists(destPath.getParent())) {
							Files.createDirectories(destPath.getParent());
						}
						Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
						result.appendLine(destPath + " 파일 복사 성공");

						log.writeln(relativePath.toString());
					}
				} catch (Exception e) {
					result.appendLine(destPath + " 파일 복사 실패");
				}
			}
		});
		return result.toString();
	}



	/**
	 * 폼 입력 유효성 검사
	 *
	 * @throws ValidationException
	 */
	void validate() throws ValidationException {
		if (StringUtils.isEmpty(this.workspacePath)) {
			AlertUtil.showAndWaitForError("워크스페이스 경로는 필수값입니다.");
			throw new ValidationException("워크스페이스 경로를 입력하지 않았습니다.");
		}
		if (StringUtils.isEmpty(this.destPath)) {
			AlertUtil.showAndWaitForError("패치 파일 생성 경로는 필수값입니다.");
			throw new ValidationException("패치 파일 생성 경로를 입력하지 않았습니다.");
		}
		if (StringUtils.isEmpty(this.projectName)) {
			AlertUtil.showAndWaitForError("프로젝트명은 필수값입니다.");
			throw new ValidationException("프로젝트명을 입력하지 않았습니다.");
		}
		if (this.baseDate == null) {
			AlertUtil.showAndWaitForError("변경 기준일은 필수값입니다.");
			throw new ValidationException("변경 기준일을 입력하지 않았습니다.");
		}
		if(!Files.exists(Paths.get(this.destPath))) {
			AlertUtil.showAndWaitForError("패치 파일 생성 경로가 존재하지 않습니다.");
			throw new ValidationException("패치 파일 생성 경로가 존재하지 않습니다.");
		}
	}

	public Path getWebPath() {
		return webPath;
	}

	public Path getSrcPath() {
		return srcPath;
	}

	public Path getBinPath() {
		return binPath;
	}

	public Path getConfPath() {
		return confPath;
	}

	public String getSourcePath() {
		return workspacePath;
	}

	public String getDestPath() {
		return destPath;
	}

	public String getProjectName() {
		return projectName;
	}

	public LocalDate getBaseDate() {
		return baseDate;
	}

	public Path getClassPath() {
		return classPath;
	}
}
