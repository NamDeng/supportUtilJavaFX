package com.kcube.support.jdk;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

import com.kcube.support.Support;
import com.kcube.support.main.MainStage;
import com.kcube.support.util.AlertUtil;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class JDKController {

	@FXML
	private ListView<String> fileList;

	@FXML
	private TextArea resultArea;

	@FXML
	public void initialize() {
		initListener();
	}

	/**
	 * 사용할 이벤트 리스너를 설정한다.
	 */
	private void initListener() {
		fileList.getItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
				print();
			}
		});
	}

	/**
	 * class 파일의 JDK 버전을 출력한다.
	 *
	 * @param event
	 */
	private void print() {
		final int size = fileList.getItems().size();

		if (size != 0) {
			final StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < size; i++) {
				final String path = fileList.getItems().get(i);
				if (path != null && path.length() != 0) {

					try (DataInputStream in = new DataInputStream(new FileInputStream(path));) {

						int magicNum = in.readInt();
						if (magicNum != 0xcafebabe) {
							stringBuilder.append(Paths.get(path).getFileName());
							stringBuilder.append(" : 유효하지 않은 클래스 파일입니다.");
							stringBuilder.append(System.getProperty("line.separator"));
							resultArea.setText(stringBuilder.toString());

							continue;
						}
						@SuppressWarnings("unused")
						final int minorVersion = in.readUnsignedShort();
						final int majorVersion = in.readUnsignedShort();
						stringBuilder.append(Paths.get(path).getFileName());
						stringBuilder.append(" : ");
						stringBuilder.append(JDK.getVersion(majorVersion));
						stringBuilder.append(System.getProperty("line.separator"));
					} catch (Exception e) {
						resultArea.setText(e.getMessage());
					}
				}
			}
			resultArea.setText(stringBuilder.toString());
		} else {
			resultArea.clear();
		}
	}

	/**
	 * 메인 화면으로 이동
	 *
	 * @param event
	 */
	public void moveMainPage(ActionEvent event) {
		MainStage.showChangePage(Support.MAIN_PAGE_URL);
	}

	/**
	 * 드래그 오버가능 하도록 변경.
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
	 * 파일을 드래그 드랍방식으로 추가한다.
	 *
	 * @param event
	 */
	public void onDragDropFile(DragEvent event) {
		final Dragboard board = event.getDragboard();

		boolean success = false;
		if (board.hasFiles()) {
			success = true;
			for (File file : board.getFiles()) {
				final String path = file.getAbsolutePath();
				final String ext = path.substring(path.lastIndexOf(".") + 1);

				if(!ext.equals("class"))
					AlertUtil.showAndWaitForError(file.getName(), "파일을 추가할 수 없습니다. \nclass 파일만 추가할 수 있습니다.");

				fileList.getItems().add(path);
			}
		}
		event.setDropCompleted(success);
		event.consume();
	}

	/**
	 * 선택한 class 파일을 추가하는 이벤트
	 *
	 * @param event
	 */
	public void addFiles(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Class 파일 탐색기");
		fileChooser.setInitialDirectory(new File("C:\\"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("class files", "*.class"));

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		if (selectedFiles != null) {
			for (int i = 0; i < selectedFiles.size(); i++) {
				fileList.getItems().add(selectedFiles.get(i).getAbsolutePath());
			}
		}
	}

	/**
	 * 선택한 class 파일을 제거하는 이벤트
	 *
	 * @param event
	 */
	public void deleteFiles(ActionEvent event) {
		final int selectedIdx = fileList.getSelectionModel().getSelectedIndex();
		if (selectedIdx == -1)
			AlertUtil.showAndWaitForWarning("파일 선택", "선택된 파일이 없습니다.");

		final int newSelectedIdx =
				(selectedIdx == fileList.getItems().size() - 1)
				? selectedIdx - 1
				: selectedIdx;

		fileList.getItems().remove(selectedIdx);
		fileList.getSelectionModel().select(newSelectedIdx);
	}
}