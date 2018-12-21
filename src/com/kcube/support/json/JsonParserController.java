package com.kcube.support.json;

import com.kcube.support.MainStage;
import com.kcube.support.Support;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class JsonParserController {

	@FXML
	private WebView webView;

	@FXML
	private ProgressBar loadProgress;

	@FXML
	public void initialize() {
		WebEngine webEngine = webView.getEngine();
		createProgressReport(webEngine);

		webEngine.load(Support.JSON_PARSER_ONLINE_URL);
	}

	/**
	 * 메인 화면으로 이동
	 *
	 * @param event
	 */
	public void moveMainPage(ActionEvent event) {
		MainStage.showChangePage(Support.MAIN_PAGE_URL);
	}

	private void createProgressReport(WebEngine engine) {
		final LongProperty startTime = new SimpleLongProperty();
		final LongProperty endTime = new SimpleLongProperty();

		loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		loadProgress.progressProperty().bind(engine.getLoadWorker().progressProperty());

		engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState,
					Worker.State state) {
				switch (state) {
				case RUNNING:
					startTime.set(System.nanoTime());
					break;

				case SUCCEEDED:
					endTime.set(System.nanoTime());
					loadProgress.setVisible(false);
					break;
				}
			}
		});
	}
}
