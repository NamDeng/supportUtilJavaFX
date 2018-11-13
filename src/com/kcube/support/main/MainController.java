package com.kcube.support.main;

import com.kcube.support.Support;

import javafx.event.ActionEvent;

public class MainController {

	/**
	 * JDK 버전 확인 화면으로 이동
	 *
	 * @param event
	 */
	public void moveJdkPage(ActionEvent event) {
		MainStage.showChangePage(Support.JDK_PAGE_URL);
	}

	/**
	 * 캐릭터셋 변환 화면으로 이동
	 *
	 * @param event
	 */
	public void moveUnicodePage(ActionEvent event) {
		MainStage.showChangePage(Support.UNICODE_PAGE_URL);
	}

	/**
	 * 암복호화 화면으로 이동
	 *
	 * @param event
	 */
	public void moveCipherPage(ActionEvent event) {
		MainStage.showChangePage(Support.CIPER_PAGE_URL);
	}

	/**
	 * 패치 준비 화면으로 이동
	 *
	 * @param event
	 */
	public void movePatchPage(ActionEvent event) {
		MainStage.showChangePage(Support.PATCH_PAGE_URL);
	}
}
