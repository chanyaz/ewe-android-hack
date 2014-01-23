package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;
import com.robotium.solo.Solo;

public class LaunchActionBar extends ScreenActions {
	private static final int sSettingsStringID = R.string.Settings;
	private static final int sInfoStringID = R.string.Info;
	private static final int sTripsStringID = R.string.trips;
	private static final int sShopStringID = R.string.shop;
	private static final int sLogOutStringID = R.string.log_out;
	private static final int sAddItinButtonID = R.id.add_itinerary;

	public LaunchActionBar(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
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
