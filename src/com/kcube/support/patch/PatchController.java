package com.kcube.support.patch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.Support;
import com.kcube.support.main.MainStage;
import com.kcube.support.util.AlertUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;

public class PatchController {

	@FXML
	private TextField sourcePathField;

	@FXML
	private TextField destPathField;

	@FXML
	private TextArea resultArea;

	@FXML
	private TextField filePathField;

	@FXML
	private Button sourceBtn;

	@FXML
	private Button destBtn;

	@FXML
	private DatePicker baseDateField;


	@FXML
	public void initialize() {
		initListener();
	}

	private void initListener() {

	}

	/**
	 * 메인 화면으로 이동
	 *
	 * @param event
	 */
	public void moveMainPage(ActionEvent event) throws Exception {
		MainStage.showChangePage(Support.MAIN_PAGE_URL);
	}

	/**
	 *
	 * @param event
	 * @throws Exception
	 */
	public void addSourceDirectory(ActionEvent event) throws Exception {
		final String path = getDirectoryPath();
		sourcePathField.setText(path);
	}

	/**
	 *
	 * @param event
	 * @throws Exception
	 */
	public void addDestDirectory(ActionEvent event) throws Exception {
		final String path = getDirectoryPath();
		destPathField.setText(path);
	}

	/**
	 *
	 * @param event
	 * @throws Exception
	 */
	public void makePatchFile(ActionEvent event) throws Exception {
		final String sourcePath = sourcePathField.getText();
		final String destPath = destPathField.getText();
		final LocalDate baseDate = baseDateField.getValue();

		final boolean isPass = validateInput(sourcePath, destPath, baseDate);
		if(isPass == false) return;

		final List<Path> fileList = getCopyFileList(sourcePath, baseDate);
		copy(destPath, fileList);
	}

	/**
	 *
	 * @param sourcePath
	 * @param baseDate
	 * @return
	 * @throws Exception
	 */
	private List<Path> getCopyFileList(final String sourcePath, final LocalDate baseDate) throws Exception {
		final Path path = Paths.get(sourcePath);
		final boolean isExists = Files.exists(path);
		if(isExists == false) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "소스 파일 경로는 필수값입니다.");
			throw new FileNotFoundException();
		}

		return Files.walk(path).filter(p -> {
			long lastModifiedTime = 0L;
			try {
				lastModifiedTime = Files.getLastModifiedTime(p).toMillis();
			} catch (IOException e) {
			}
			final Instant instant = Instant.ofEpochMilli(lastModifiedTime);
			final LocalDate modifiedDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

			return modifiedDate.isAfter(baseDate);
		}).collect(Collectors.toList());
	}

	/**
	 *
	 * @param destPath
	 * @param fileList
	 */
	private void copy(final String destPath, final List<Path> fileList) {
		fileList.stream().forEach(System.out::println);
	}

	/**
	 *
	 * @param sourcePath
	 * @param destPath
	 * @param baseDate
	 * @return
	 */
	private boolean validateInput(final String sourcePath, final String destPath, final LocalDate baseDate) {
		if(StringUtils.isEmpty(sourcePath)) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "소스 파일 경로는 필수값입니다.");
			return false;
		}
		if(StringUtils.isEmpty(destPath)) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "패치 파일 생성 위치 경로는 필수값입니다.");
			return false;
		}
		if(baseDate == null) {
			AlertUtil.showAndWaitForError("패치 파일 생성 에러", "파일 생성 기준일은 필수값입니다.");
			return false;
		}
		return true;
	}

	/**
	 * 드래그 오버
	 *
	 * @param event
	 */
	public void onDragOver(DragEvent event) {
		Dragboard board = event.getDragboard();

		if (board.hasFiles()) {
			event.acceptTransferModes(TransferMode.ANY);
		}
		event.consume();
	}

	/**
	 * 파일을 드래그 드랍방식으로 파추가한다.
	 *
	 * @param event
	 */
	public void onDragDropFile(DragEvent event) {
		final Dragboard board = event.getDragboard();

		boolean success = false;
		if (board.hasFiles()) {
		}
		event.setDropCompleted(success);
		event.consume();
	}

	/**
	 * 디렉토리를 추가한다.
	 * @param event
	 */
	public String getDirectoryPath() throws Exception {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("디렉토리 선택");
		directoryChooser.setInitialDirectory(new File("C:\\"));
		File dir = directoryChooser.showDialog(null);

		return StringUtils.isEmpty(dir.getAbsolutePath()) ? " " : dir.getAbsolutePath();
	}
}
