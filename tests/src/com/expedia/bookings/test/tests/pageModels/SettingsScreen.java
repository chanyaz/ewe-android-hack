package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.R;

public class SettingsScreen extends ScreenActions {

	private static int sSelectPOSTextID = R.string.preference_point_of_sale_title;
	private static int sClearPrivateDataTextID = R.string.clear_private_data;
	private static int sOKID = R.string.ok;

	private static String sSelectAPI = "Select API";
	private static String sServerProxyAddress = "Server/Proxy Address";
	private static String sStubConfigurationPage = "Stub Configuration Page";

	public SettingsScreen(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}

	// Object access
	public String clearPrivateDataString() {
		return mRes.getString(sClearPrivateDataTextID);
	}

	public String OKString() {
		return mRes.getString(sOKID);
	}

	// Object interaction

	public void clickToClearPrivateData() {
		clickOnText(clearPrivateDataString());
	}

	public void clickOKString() {
		clickOnText(OKString());
	}

	public void clickSelectAPIString() {
		scrollToTop();
		clickOnText(sSelectAPI);
	}

	public void clickServerProxyAddressString() {
		clickOnText(sServerProxyAddress);
	}

	public void clearServerEditText() {
		clearEditText(0);
	}

	public void enterServerText(String text) {
		enterText(0, text);
	}

}
