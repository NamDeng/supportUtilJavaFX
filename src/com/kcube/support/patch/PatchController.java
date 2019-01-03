package com.kcube.support.patch;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.MainStage;
import com.kcube.support.Support;
import com.kcube.support.util.AlertUtil;
import com.kcube.support.util.LogFileUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
	private TextField projectField;

	@FXML
	private DatePicker baseDateField;

	@FXML
	private ToggleGroup type;

	@FXML
	private RadioButton main;

	@FXML
	private RadioButton app;

	private String sourceType = "ext";

	@FXML
	public void initialize() {
		initListener();
	}

	private void initListener() {
		sourcePathField.textProperty().addListener((observable, oldValue, newValue) -> {
			sourcePathField.setText(newValue);
		});

		destPathField.textProperty().addListener((observable, oldValue, newValue) -> {
			destPathField.setText(newValue);
		});

		type.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				RadioButton selectButton = (RadioButton) newValue.getToggleGroup().getSelectedToggle();
				sourceType = selectButton.getText();
			}
		});
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
		resultArea.setText("");

		final String sourcePath = sourcePathField.getText();
		final String destPath = destPathField.getText();
		final String projectName = projectField.getText();
		final LocalDate baseDate = baseDateField.getValue();

		final Patch patch = new Patch(sourcePath, destPath, projectName, baseDate, sourceType);
		patch.validate();

		final String logPath = destPath + System.getProperty("file.separator") + "patchLog.txt";
		final StringBuilder result = new StringBuilder();
		final LogFileUtil log = new LogFileUtil(logPath);
		log.writeln("========================= " + projectName + " 패치 경로 =========================");

		// patch file copy start!
		final List<Path> srcFileList = patch.getCopyFileList(patch.getSrcPath());
		result.append(patch.copySrcFile(srcFileList, log));

		final List<Path> webFileList = patch.getCopyFileList(patch.getWebPath());
		result.append(patch.copyWebFile(webFileList, log));

		if (webFileList.isEmpty() && srcFileList.isEmpty())
			result.append("변경된 파일이 없습니다. 변경 기준일을 확인해주세요.");

		log.close();
		resultArea.setText(result.toString());
	}

	/**
	 * 드래그 오버
	 *
	 * @param event
	 */
	public void onDragOverWorkPath(DragEvent event) {
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
	public void onDragDropWorkPath(DragEvent event) {
		final Dragboard board = event.getDragboard();

		int cnt = 0;
		final boolean success = false;
		if (board.hasFiles()) {
			for (File file : board.getFiles()) {
				if (cnt != 0) {
					AlertUtil.showAndWaitForWarning("디렉토리 추가 에러", "하나의 디렉토리만 선택 가능합니다.");
					return;
				}

				if (!file.isDirectory()) {
					AlertUtil.showAndWaitForError(file.getName(), "디렉토리 추가 에러. 디렉토리만 선택 가능합니다.");
					return;
				}
				sourcePathField.setText(file.getAbsolutePath());
				cnt++;
			}

		}
		event.setDropCompleted(success);
		event.consume();
	}

	/**
	 * 드래그 오버
	 *
	 * @param event
	 */
	public void onDragOverTargetPath(DragEvent event) {
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
	public void onDragDropTargetPath(DragEvent event) {
		final Dragboard board = event.getDragboard();

		int cnt = 0;
		final boolean success = false;
		if (board.hasFiles()) {
			for (File file : board.getFiles()) {
				if (cnt != 0) {
					AlertUtil.showAndWaitForWarning("디렉토리 추가 에러", "하나의 디렉토리만 선택 가능합니다.");
					return;
				}

				if (!file.isDirectory()) {
					AlertUtil.showAndWaitForError(file.getName(), "디렉토리 추가 에러. 디렉토리만 선택 가능합니다.");
					return;
				}
				destPathField.setText(file.getAbsolutePath());
				cnt++;
			}

		}
		event.setDropCompleted(success);
		event.consume();
	}

	/**
	 * 디렉토리를 추가한다.
	 *
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
