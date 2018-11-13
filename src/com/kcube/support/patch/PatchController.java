package com.kcube.support.patch;

import java.io.File;
import java.time.LocalDate;

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

	public void makePatchFile(ActionEvent event) throws Exception {
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

		return dir.getAbsolutePath();
	}
}
