package com.kcube.support;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainStage extends Application {
	private static BorderPane rootLayout;
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception{
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(Support.TITLE);

		initMainLayout();
		showMainPage();
	}

	private void initMainLayout() {
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainStage.class.getResource(Support.MAIN_LAYOUT_URL));
			rootLayout = (BorderPane) loader.load();

			final Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(getClass().getResource(Support.MAIN_CSS_URL).toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void showMainPage() {
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainStage.class.getResource(Support.MAIN_PAGE_URL));
			final AnchorPane root = (AnchorPane) loader.load();

			rootLayout.setCenter(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void showChangePage(String fileName){
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainStage.class.getResource(fileName));
			final AnchorPane view = (AnchorPane) loader.load();

			rootLayout.setCenter(view);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
