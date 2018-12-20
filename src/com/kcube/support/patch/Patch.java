package com.kcube.support.patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.util.AlertUtil;

public class Patch {

	private Path webPath;
	private Path srcPath;
	private Path binPath;
	private String sourcePath;
	private String destPath;
	private String projectName;
	private LocalDate baseDate;

	public Patch(String sourcePath, String destPath, String projectName, LocalDate baseDate) {
		this.sourcePath = sourcePath;
		this.destPath = destPath;
		this.projectName = projectName;
		this.baseDate = baseDate;

		init();
	}

	private void init() {
		this.binPath = Paths.get(getPath(sourcePath, "bin"));
		this.srcPath = Paths.get(getPath(sourcePath, "cst", this.projectName, "src"));
		this.webPath = Paths.get(getPath(sourcePath, "cst", this.projectName, "web"));
	}

	/**
	 * 파일 경로를 만든다.
	 *
	 * @param paths
	 * @return
	 */
	private String getPath(String... paths) {
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
	public List<Path> getCopyFileList(final Path sourcePath) throws Exception {
		final boolean isExists = Files.exists(sourcePath);
		if (isExists == false) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "디렉토리가 존재하지 않습니다.");
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
	 *
	 * @param destPath
	 * @param fileList
	 */
	public StringBuilder copySrcFile(final List<Path> fileList) {
		String destPath = this.destPath;
		final StringBuilder result = new StringBuilder();
		fileList.stream().forEach(sourcePath -> {
			// web 디렉토리 하위 파일을 목적지로 카피한다.
		});
		return result;
	}

	/**
	 *
	 * @param destPath
	 * @param fileList
	 * @return
	 */
	public StringBuilder copyWebFile(final List<Path> fileList) {
		final StringBuilder result = new StringBuilder();
		fileList.stream().forEach(path -> {
			// webPath를 기준으로 상대경로
			final Path temp = this.webPath.relativize(path);
			// 패치 파일 경로
			final Path destPath = Paths.get(this.destPath).resolve(temp);
			try {
				if (isFile(destPath)) {
					if (Files.notExists(destPath.getParent())) {
						result.append(destPath.getParent() + " 디렉토리 생성");
						result.append(System.getProperty("line.separator"));
						Files.createDirectories(destPath.getParent());
					}
					Files.copy(path, destPath);
					result.append(path + " 파일 복사 성공");
				}
			} catch (IOException e) {
				e.printStackTrace();
				result.append(path + " 파일 복사 실패");
			}
			result.append(System.getProperty("line.separator"));
		});
		return result;
	}

	public boolean isFile(final Path path) {
		return path.toString().contains(".");
	}

	/**
	 *
	 * @throws ValidationException
	 */
	public void validate() throws ValidationException {
		if (StringUtils.isEmpty(this.sourcePath)) {
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
		return sourcePath;
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
