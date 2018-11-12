package com.kcube.support.unicode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.kcube.support.Support;
import com.kcube.support.main.MainStage;
import com.kcube.support.util.AlertUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class UnicodeController {

	@FXML
	private TextField encodeField;

	@FXML
	private TextField decodeField;

	@FXML
	private TextArea resultArea;

	@FXML
	private TextField filePathField;

	@FXML
	private ToggleGroup option;

	@FXML
	private RadioButton hangul;

	@FXML
	private RadioButton unicode;

	@FXML
	public void initialize() {
		initListener();
	}

	private void initListener() {
		encodeField.textProperty().addListener((observable, oldValue, newValue) -> {
			printEncodedUnicode(encodeField.getText());
		});

		decodeField.textProperty().addListener((observable, oldValue, newValue) -> {
			printDecodeHangul(decodeField.getText());
		});

		filePathField.textProperty().addListener((observable, oldValue, newValue) -> {
			printConvertCharacterInFile(filePathField.getText());
		});

		option.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				printConvertCharacterInFile(filePathField.getText());
			}
		});
	}

	/**
	 * 검색 키워드를 하이라이트 표시한다.
	 *
	 * @param keyword
	 */
	private void markSearchKeyword(String keyword) {
		final String result = resultArea.getText();
		if (StringUtils.isEmpty(result))
			AlertUtil.showAndWaitForWarning("변환결과 조회", "변환 결과가 없습니다.\n properties 파일을 추가해주세요.");

		if (StringUtils.isEmpty(keyword))
			AlertUtil.showAndWaitForWarning("변환결과 조회", "키워드를 입력하지 않았습니다.");

		Pattern pattern = Pattern.compile(keyword);
		Matcher matcher = pattern.matcher(result);

		boolean found = matcher.find(0);
		if (found && result.indexOf(keyword) == -1)
			AlertUtil.showAndWaitForWarning("변환결과 조회", keyword + " 검색결과가 없습니다");

		resultArea.selectRange(matcher.start(), matcher.end());
		resultArea.setFocusTraversable(true);
	}

	/**
	 * 파일을 읽어 선택 옵션에 맞게 문자 변환 결과를 보여준다. ( 한글, 유니코드 )
	 *
	 * @param path
	 */
	private void printConvertCharacterInFile(String path) {
		if (StringUtils.isEmpty(path))
			AlertUtil.showAndWaitForWarning("파일 선택", "선택된 파일이 없습니다.");

		final StringBuilder stringBuilder = new StringBuilder();
		try (InputStreamReader in = new InputStreamReader(new FileInputStream(path), "UTF8");
				BufferedReader buffReader = new BufferedReader(in)) {

			String line = "";
			if (hangul.isSelected()) {
				while ((line = buffReader.readLine()) != null) {
					stringBuilder.append(StringEscapeUtils.unescapeJava(line));
					stringBuilder.append(System.getProperty("line.separator"));
				}
			} else if (unicode.isSelected()) {
				while ((line = buffReader.readLine()) != null) {
					stringBuilder.append(StringEscapeUtils.escapeJava(line));
					stringBuilder.append(System.getProperty("line.separator"));
				}
			}

		} catch (Exception e) {
			resultArea.setText(e.getMessage());
		}
		resultArea.setText(stringBuilder.toString());
	}

	/**
	 * 한글을 유니코드로 변경한다.
	 *
	 * @throws Exception
	 */
	public void printEncodedUnicode(String hangul) {
		if (StringUtils.isEmpty(hangul))
			AlertUtil.showAndWaitForWarning("한글 입력", "변환할 데이터를 입력해주세요.");

		resultArea.setText(StringEscapeUtils.escapeJava(hangul));
	}

	/**
	 * 유니코드를 한글로 변환한다.
	 *
	 * @throws Exception
	 */
	public void printDecodeHangul(String unicode) {
		if (StringUtils.isEmpty(unicode))
			AlertUtil.showAndWaitForWarning("유니코드 입력", "변환할 데이터를 입력해주세요.");

		resultArea.setText(StringEscapeUtils.unescapeJava(unicode));
	}

	/**
	 * properties 파일을 변환한다.
	 *
	 */
	public void printConvertedCharacter() {
		final String path = filePathField.getText();
		if (StringUtils.isEmpty(path))
			AlertUtil.showAndWaitForWarning("파일 선택", "선택된 파일이 없습니다.");

		final StringBuilder stringBuilder = new StringBuilder();

		try (FileReader fileReader = new FileReader(new File(path));
				BufferedReader buffReader = new BufferedReader(fileReader)) {
			String line = "";
			if (hangul.isSelected()) {
				while ((line = buffReader.readLine()) != null) {
					stringBuilder.append(StringEscapeUtils.unescapeJava(line));
					stringBuilder.append(System.getProperty("line.separator"));
				}
			} else if (unicode.isSelected()) {
				while ((line = buffReader.readLine()) != null) {
					stringBuilder.append(StringEscapeUtils.escapeJava(line));
					stringBuilder.append(System.getProperty("line.separator"));
				}
			}
		} catch (Exception e) {
			resultArea.setText(e.getMessage());
		}
		resultArea.setText(stringBuilder.toString());
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
	 * 드래그 오버가능하도록 설정.
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

			int cnt = 0;
			for (File file : board.getFiles()) {
				if(cnt != 0)
					AlertUtil.showAndWaitForWarning("파일 추가 실패", "한개의 properties 파일만 추가 가능합니다");

				final String path = file.getAbsolutePath();
				final String ext = path.substring(path.lastIndexOf(".") + 1);
				if (!ext.equals("properties"))
					AlertUtil.showAndWaitForError(file.getName(), "파일 추가 실패. \nproperties 파일만 추가할 수 있습니다.");

				filePathField.setText(path);
				cnt++;
			}
		}
		event.setDropCompleted(success);
		event.consume();
	}

	/**
	 * Ctrl + F키 이벤트 설정
	 *
	 * @param event
	 * @throws IOException
	 */
	public void onKeyPress(KeyEvent event) throws IOException {
		if (event.getCode() == KeyCode.F && event.isControlDown()) {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("기술지원");
			dialog.setHeaderText("결과 검색");
			dialog.setContentText("검색 키워드를 입력해주세요.");

			Optional<String> dialogResult = dialog.showAndWait();
			dialogResult.ifPresent(keyword -> markSearchKeyword(keyword));
		}
	}

	/**
	 * 선택한 properties 파일을 추가한다.
	 *
	 * @param event
	 */
	public void addChooseFiles(ActionEvent event) throws Exception {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Properties 파일 탐색기");
		fileChooser.setInitialDirectory(new File("C:\\"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("properties files", "*.properties"));

		final File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			filePathField.setText(file.getAbsolutePath());
		}
	}
}
