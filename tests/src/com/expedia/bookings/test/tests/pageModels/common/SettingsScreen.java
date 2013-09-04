package com.expedia.bookings.test.tests.pageModels.common;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.expedia.bookings.R;

public class SettingsScreen extends ScreenActions {

	private static int sSelectPOSTextID = R.string.preference_point_of_sale_title;
	private static int sClearPrivateDataTextID = R.string.clear_private_data;
	private static int sOKID = R.string.ok;
	private static int sSpoofHotelBookingsCheckBoxID = R.id.preference_spoof_booking_checkbox;
	private static int sSupressFlightBookingCheckBoxID = R.id.preference_suppress_flight_booking_checkbox;

	private static String sSelectAPI = "Select API";
	private static String sServerProxyAddress = "Server/Proxy Address";
	private static String sStubConfigurationPage = "Stub Configuration Page";
	private static String sSpoofHotelBookingsString = "Spoof hotel bookings";
	private static String sSuppressFlightsBookingString = "Suppress Flight Bookings";

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

	public void setSpoofBookings() {
		boolean spoofBookingsDone = false;
		boolean suppressFlightsDone = false;
		delay(5);
		scrollDown();
		ArrayList<View> currentViews = getCurrentViews();
		for (int i = 0; i < currentViews.size(); i++) {
			if (spoofBookingsDone && suppressFlightsDone) {
				break;
			}
			View currentView = currentViews.get(i);
			if (currentView instanceof CheckBox) {
				CheckBox currentCheckBox = (CheckBox) currentView;
				if (currentCheckBox.getId() == sSpoofHotelBookingsCheckBoxID) {
					if (!currentCheckBox.isChecked()) {
						clickOnText(sSpoofHotelBookingsString);
					}
					spoofBookingsDone = true;
				}
				else if (currentCheckBox.getId() == sSupressFlightBookingCheckBoxID) {
					if (!currentCheckBox.isChecked()) {
						clickOnText(sSuppressFlightsBookingString);
					}
					suppressFlightsDone = true;
				}
			}
		}
	}
}
