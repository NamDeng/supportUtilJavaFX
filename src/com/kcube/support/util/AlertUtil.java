package com.kcube.support.util;

import com.kcube.support.Support;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtil {

	public static void show(final AlertType alertType, final String content) {
		Alert alert = new Alert(alertType, content);
		alert.setTitle(Support.TITLE);

		alert.show();
	}

	public static void showAndWaitInInformation(final String header, final String content) {
		Alert alert = new Alert(AlertType.INFORMATION, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText(header);

		alert.showAndWait();
	}

	public static void showAndWaitForConfirmation(final String header, final String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText(header);

		alert.showAndWait();
	}

	public static void showAndWaitForWarning(final String content) {
		Alert alert = new Alert(AlertType.WARNING, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText("Warning!");

		alert.showAndWait();
	}

	public static void showAndWaitForWarning(final String header, final String content) {
		Alert alert = new Alert(AlertType.WARNING, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText(header);

		alert.showAndWait();
	}

	public static void showAndWaitForError(final String content) {
		Alert alert = new Alert(AlertType.ERROR, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText("Error!");

		alert.showAndWait();
	}

	public static void showAndWaitForError(final String header, final String content) {
		Alert alert = new Alert(AlertType.ERROR, content);
		alert.setTitle(Support.TITLE);
		alert.setHeaderText(header);

		alert.showAndWait();
	}
}
