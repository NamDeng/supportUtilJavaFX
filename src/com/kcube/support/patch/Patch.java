package com.kcube.support.patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private Path webPath;
	private Path srcPath;
	private Path classPath;
	private Path binPath;

	private String workspacePath;
	private String destPath;
	private String projectName;

	private LocalDate baseDate;

	public Patch(String sourcePath, String destPath, String projectName, LocalDate baseDate) {
		this.workspacePath = sourcePath;
		this.destPath = destPath;
		this.projectName = projectName;
		this.baseDate = baseDate;

		init();
	}

	private void init() {
		this.binPath = Paths.get(getPath(workspacePath, "bin"));
		this.srcPath = Paths.get(getPath(workspacePath, "cst", this.projectName, "src"));
		this.webPath = Paths.get(getPath(workspacePath, "cst", this.projectName, "web"));
		this.classPath = Paths.get(getPath(this.destPath, "WEB-INF", "classes"));
	}

	/**
	 * 파일 경로를 만든다.
	 *
	 * @param paths
	 * @return
	 */
	String getPath(String... paths) {
		String lineSeparator = System.getProperty("file.separator");
		StringJoiner sj = new StringJoiner(lineSeparator);
		for (String path : paths) {
			sj.add(path);
		}
		return sj.toString();
	}

	/**
	 * 변경 기준일 이후의 파일 목록을 가져온다.
	 *
	 * @param sourcePath
	 * @param baseDate
	 * @return
	 * @throws Exception
	 */
	List<Path> getCopyFileList(final Path sourcePath) throws Exception {
		final boolean isExists = Files.exists(sourcePath);
		if (isExists == false) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "존재하지 않는 워크스페이스 경로입니다.");
			throw new FileNotFoundException();
		}

		return Files.walk(sourcePath).filter(path -> {
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
					Path destParent = destPath.getParent();
					if (Files.notExists(destParent)) {
						Files.createDirectories(destParent);
					}

					if (JDK.isJavaFile(destPath.toString())) {

						// 확장자를 제외한 파일명 추출
						String fileName = destPath.getFileName().toString();
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
						// 추출한 파일명으로 정규식으로 class파일 binPath에서 찾아서 복사
						final String regex = "(" + fileName + ")[\\$]{0,1}[\\S]*(.class)";
						final Pattern pattern = Pattern.compile(regex);
						final Path binParent = binPath.getParent();
						Files.walk(binParent).forEach(file -> {
							Matcher matcher = pattern.matcher(file.toString());
							if (matcher.find()) {
								final Path dest = destParent.resolve(file.getFileName());
								try {
									Files.copy(file, dest);
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
						Files.copy(binPath, destPath);
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
					Files.copy(path, destPath);
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
