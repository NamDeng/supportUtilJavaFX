package com.kcube.support.cipher;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.kcube.support.Support;
import com.kcube.support.main.MainStage;
import com.kcube.support.util.AlertUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class CipherController {

	@FXML
	private Label infoLabel;

	@FXML
	private TextField base64Field;

	@FXML
	private TextField shaField;

	@FXML
	private TextField aesField;

	@FXML
	private TextField secureKeyField;

	@FXML
	private TextArea resultArea;

	@FXML
	private Button base64Btn;

	@FXML
	private Button shaBtn;

	@FXML
	private Button aesBtn;

	@FXML
	private ToggleGroup base64Group;

	@FXML
	private RadioButton base64Encoder;

	@FXML
	private RadioButton base64Decoder;

	@FXML
	private ToggleGroup shaGroup;

	@FXML
	private RadioButton sha224Encryption;

	@FXML
	private RadioButton sha256Encryption;

	@FXML
	private RadioButton sha384Encryption;

	@FXML
	private RadioButton sha512Encryption;

	@FXML
	private ToggleGroup aesGroup;

	@FXML
	private RadioButton aes128Encryption;

	@FXML
	private RadioButton aes128Decryption;

	@FXML
	private RadioButton aes192Encryption;

	@FXML
	private RadioButton aes192Decryption;

	@FXML
	private RadioButton aes256Encryption;

	@FXML
	private RadioButton aes256Decryption;

	@FXML
	public void initialize() {
		initComponent();
		initListener();
	}

	/**
	 * 컴포넌트 초기화
	 */
	private void initComponent() {
		infoLabel.setWrapText(true);
	}

	/**
	 * 리스너 초기화
	 */
	private void initListener() {
		base64Field.textProperty().addListener((observable, oldValue, newValue) -> {
			resultArea.setText(convertBase64(newValue));
		});

		shaField.textProperty().addListener((observable, oldValue, newValue) -> {
			resultArea.setText(convertSHA(newValue));
		});

		base64Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				resultArea.setText(convertBase64(base64Field.getText()));
			}
		});

		shaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				resultArea.setText(convertSHA(shaField.getText()));
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
	public void printBase64Result(ActionEvent event) throws Exception {
		final String input = base64Field.getText();
		if (StringUtils.isEmpty(input))
			AlertUtil.showAndWaitForWarning("변환결과 조회", "변환값을 입력하지 않았습니다.");

		final String result = convertBase64(input);
		resultArea.setText(result);

	}

	/**
	 *
	 * @param input
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String convertBase64(final String input) {
		String result = null;
		try {
			if (base64Encoder.isSelected()) {
				result = new String(Base64.getEncoder().encode(input.getBytes("UTF-8")));
			} else if (base64Decoder.isSelected()) {
				try {
					result = new String(Base64.getDecoder().decode(input.getBytes("UTF-8")));
				} catch (IllegalArgumentException e) {
					result = "base64 디코딩 입력값이 올바르지 않습니다.\n";
					result += "Error code : " + e.getMessage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 *
	 * @param event
	 * @throws Exception
	 */
	public void printSHAResult(ActionEvent event) throws Exception {
		final String input = shaField.getText();
		if (StringUtils.isNotEmpty(input)) {

			final String result = convertSHA(input);
			resultArea.setText(result);
		} else {
			AlertUtil.showAndWaitForWarning("변환결과 조회", "변환값을 입력하지 않았습니다.");
		}

	}

	/**
	 *
	 * @param input
	 * @return
	 * @throws Exception
	 */
	private String convertSHA(final String input) {
		String type = "";
		if (sha224Encryption.isSelected()) {
			type = "SHA-224";
		} else if (sha256Encryption.isSelected()) {
			type = "SHA-256";
		} else if (sha384Encryption.isSelected()) {
			type = "SHA-384";
		} else if (sha512Encryption.isSelected()) {
			type = "SHA-512";
		}
		Crypto crypto = new SHA(type);
		String result = "";
		try {
			result = crypto.encrypt(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 *
	 * @param event
	 * @throws Exception
	 */
	public void printAESResult(ActionEvent event) throws Exception {
		final String input = aesField.getText();
		final String secureKey = secureKeyField.getText();

		Crypto crypto = new AES(secureKey);
		String result = null;
		if (aes128Encryption.isSelected() || aes192Encryption.isSelected() || aes256Encryption.isSelected()) {
			result = crypto.encrypt(input);
		} else if (aes128Decryption.isSelected() || aes192Decryption.isSelected() || aes256Decryption.isSelected()) {
			result = crypto.decrypt(input);
		}
		resultArea.setText(result);
	}
}
