package com.kcube.support.patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.jdk.JDK;
import com.kcube.support.unicode.Unicode;
import com.kcube.support.util.AlertUtil;

public class Patch {
	public static final String[] SOURCE_TYPE = { "ext", "app" };

	private static final String SRC = "src";
	private static final String WEB = "web";
	private static final String CST = "cst";
	private static final String BIN = "bin";
	private static final String WEB_INF = "WEB-INF";
	private static final String CLASSES = "classes";

	private Path webPath;
	private Path srcPath;
	private Path classPath;
	private Path binPath;

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
		final String SrcPath = sourceType.equals(SOURCE_TYPE[0])
				? getPath(workspacePath, CST, this.projectName, SRC)
				: getPath(workspacePath, this.projectName, SRC);
		final String WebPath = sourceType.equals(SOURCE_TYPE[0])
				? getPath(workspacePath, CST, this.projectName, WEB)
				: getPath(workspacePath, this.projectName, WEB);
		final String binPath = getPath(workspacePath, BIN);
		final String classPath = getPath(this.destPath, WEB_INF, CLASSES);

		this.srcPath = Paths.get(SrcPath);
		this.webPath = Paths.get(WebPath);
		this.binPath = Paths.get(binPath);
		this.classPath = Paths.get(classPath);
	}

	/**
	 * 파일 경로를 만든다.
	 *
	 * @param paths
	 * @return
	 */
	String getPath(final String... paths) {
		final String lineSeparator = System.getProperty("file.separator");
		final StringJoiner sj = new StringJoiner(lineSeparator);
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
	 * 프로젝스소스/WEB-INF/classes 디렉토리 하위 파일을 복사한다.
	 *
	 * 1. java 파일은 매칭되는 class 파일 복사
	 * 2. properties 파일은 유니코드로 변경 후 파일 생성
	 * 3. 그외 파일은 그냥 복사한다.
	 * </pre>
	 *
	 * @param destPath
	 * @param fileList
	 */
	StringBuilder copySrcFile(final List<Path> fileList) {
		final StringBuilder result = new StringBuilder();
		fileList.stream().forEach(path -> {
			// srcPath 기준 상대 경로
			final Path relativePath = this.srcPath.relativize(path);
			// bin 디렉토리 기준 복사할 소스 경로
			final Path binPath = this.binPath.resolve(relativePath);
			// 패치 파일 복사 경로
			final Path destPath = this.classPath.resolve(relativePath);
			try {
				if (isFile(destPath)) {
					final Path destParent = destPath.getParent();
					if (Files.notExists(destParent)) {
						Files.createDirectories(destParent);
					}

					if (JDK.isJavaFile(destPath.toString())) {
						// 확장자를 제외한 파일명 추출
						String fileName = destPath.getFileName().toString();
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
						// 추출한 파일명으로 정규식으로 class파일 binPath에서 찾아서 복사
						final String regex = "(^(" + fileName + ")(.class))|(^(" + fileName + ")(\\$){1}[a-zA-Z1-9]*(.class))";
						final Pattern pattern = Pattern.compile(regex);
						final Path binParent = binPath.getParent();
						Files.walk(binParent).forEach(file -> {
							Matcher matcher = pattern.matcher(file.getFileName().toString());
							if (matcher.find()) {
								final Path dest = destParent.resolve(file.getFileName());
								try {
									Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
									result.append(dest + " 파일 복사 성공");
								} catch (FileAlreadyExistsException faee) {
									result.append(dest + " 파일이 이미 존재함");
								} catch (IOException e) {
									result.append(dest + " 파일 복사 실패");
								}
								result.append(System.getProperty("line.separator"));
							}
						});
					} else if (Unicode.isPropertiesFile(destPath.toString())) {
						Unicode.convertToUnicodeFile(binPath.toFile(), destPath.toFile());
						result.append(destPath + " 파일 복사 성공");
					} else {
						Files.copy(binPath, destPath, StandardCopyOption.REPLACE_EXISTING);
						result.append(destPath + " 파일 복사 성공");
					}
				}
			} catch (IOException e) {
				result.append(destPath + " 파일 복사 실패");
			}
			result.append(System.getProperty("line.separator"));
		});

		return result;
	}

	/**
	 * web 소스 파일을 복사한다.
	 *
	 * @param destPath
	 * @param fileList
	 * @return
	 */
	StringBuilder copyWebFile(final List<Path> fileList) {
		final StringBuilder result = new StringBuilder();
		fileList.stream().forEach(path -> {
			// webPath를 기준으로 상대경로
			final Path relativePath = this.webPath.relativize(path);
			// 패치 파일 복사 경로
			final Path destPath = Paths.get(this.destPath).resolve(relativePath);
			try {
				if (isFile(destPath)) {
					if (Files.notExists(destPath.getParent())) {
						Files.createDirectories(destPath.getParent());
					}
					Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
					result.append(destPath + " 파일 복사 성공");
				}
			} catch (FileAlreadyExistsException faee) {
				result.append(destPath + " 파일이 이미 존재함");

			} catch (IOException e) {
				result.append(destPath + " 파일 복사 실패");
			}
			result.append(System.getProperty("line.separator"));
		});
		return result;
	}

	/**
	 * 파일인지 확인한다.
	 *
	 * @param path
	 * @return
	 */
	boolean isFile(final Path path) {
		return path.toString().contains(".");
	}

	/**
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
}
