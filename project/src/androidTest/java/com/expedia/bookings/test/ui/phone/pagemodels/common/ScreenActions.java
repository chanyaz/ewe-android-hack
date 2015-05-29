package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.mobiata.android.Log;

public class ScreenActions {

	public static void enterLog(String tag, String logText) {
		Log.v(tag, logText);
	}

	public static void delay(int seconds) {
		seconds = seconds * 1000;
		try {
			Thread.sleep(seconds);
		}
		catch (InterruptedException e) {
			//ignore
		}
	}
}
