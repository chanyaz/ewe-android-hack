package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class LaunchActionBar extends ScreenActions {
	private static int sSettingsStringID = R.string.Settings;
	private static int sInfoStringID = R.string.Info;
	private static int sTripsStringID = R.string.trips;
	private static int sShopStringID = R.string.shop;
	private static int sLogOutStringID = R.string.log_out;
	private static int sAddItinButtonID = R.id.add_itinerary;

	public LaunchActionBar(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}

	public String settingsString() {
		return mRes.getString(sSettingsStringID);
	}

	public String infoString() {
		return mRes.getString(sInfoStringID);
	}

	public String logOutString() {
		return mRes.getString(sLogOutStringID);
	}

	public String tripsString() {
		return mRes.getString(sTripsStringID);
	}

	public String shopString() {
		return mRes.getString(sShopStringID);
	}
	
	public View addItinButton() {
		return getView(sAddItinButtonID);
	}

	/*
	 * ActionBar tabs
	 * They don't have IDs, so we must reference by string ID
	 */

	public void pressShop() {
		clickOnText(shopString());
	}

	public void pressTrips() {
		clickOnText(tripsString());
	}

	/*
	 * Menu interactions
	 */
	
	public void openMenuDropDown() {
		sendKey(Solo.MENU);
	}

	public void pressSettings() {
		clickOnText(settingsString());
	}

	public void pressInfo() {
		clickOnText(infoString());
	}

	public void pressLogOut() {
		clickOnText(logOutString());
	}
	
	/*
	 * Other actions
	 */
	
	public void pressAddItinButton() {
		clickOnView(addItinButton());
	}

}
