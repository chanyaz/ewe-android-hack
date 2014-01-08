package com.expedia.bookings.test.tests.pageModels.common;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.CheckBox;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestDriver;
import com.expedia.bookings.test.utils.TestPreferences;

public class SettingsScreen extends ScreenActions {

	private static final String TAG = "SettingsScreen POM";
	private static final int SELECT_POS_STRING_ID = R.string.preference_point_of_sale_title;
	private static final int CLEAR_PRIVATE_DATE_STRING_ID = R.string.clear_private_data;
	private static final int OK_STRING_ID = R.string.ok;
	private static final int ACCEPT_STRING_ID = R.string.accept;
	private static final int SPOOF_HOTEL_BOOKING_CHECKBOX_ID = R.id.preference_spoof_booking_checkbox;
	private static final int SUPPRESS_FLIGHT_BOOKING_CHECKBOX_ID = R.id.preference_suppress_flight_booking_checkbox;
	private static final int CANCEL_STRING_ID = R.string.cancel;

	private static final int COUNTRY_STRING_ID = R.string.preference_point_of_sale_title;
	private static final String SELECT_API_STRING_ID = "Select API";
	private static final String SERVER_PROXY_STRING_ID = "Server/Proxy Address";
	private static final String STUB_CONFIGURATION_PAGE_STRING_ID = "Stub Configuration Page";
	private static final String SPOOF_HOTELS_BOOKING_STRING_ID = "Spoof hotel bookings";
	private static final String SUPPRESS_FLIGHTS_BOOKING_STRING_ID = "Suppress Flight Bookings";

	public SettingsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access
	public String clearPrivateDataString() {
		return mRes.getString(CLEAR_PRIVATE_DATE_STRING_ID);
	}

	public String OKString() {
		return mRes.getString(OK_STRING_ID);
	}

	public String cancelString() {
		return mRes.getString(CANCEL_STRING_ID);
	}

	public String AcceptString() {
		return mRes.getString(ACCEPT_STRING_ID);
	}

	public String country() {
		return getString(COUNTRY_STRING_ID);
	}

	// Object interaction

	public void clickCountryString() {
		clickOnText(country());
	}

	public void clickToClearPrivateData() {
		clickOnText(clearPrivateDataString());
	}

	public void clickOKString() {
		clickOnText(OKString());
	}

	public void clickCancelString() {
		clickOnText(cancelString());
	}

	public void clickAcceptString() {
		clickOnText(AcceptString());
	}

	public void clickSelectAPIString() {
		scrollToTop();
		clickOnText(SELECT_API_STRING_ID);
	}

	public void clickServerProxyAddressString() {
		clickOnText(SERVER_PROXY_STRING_ID);
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
				if (currentCheckBox.getId() == SPOOF_HOTEL_BOOKING_CHECKBOX_ID) {
					if (!currentCheckBox.isChecked()) {
						clickOnText(SPOOF_HOTELS_BOOKING_STRING_ID);
					}
					spoofBookingsDone = true;
				}
				else if (currentCheckBox.getId() == SUPPRESS_FLIGHT_BOOKING_CHECKBOX_ID) {
					if (!currentCheckBox.isChecked()) {
						clickOnText(SUPPRESS_FLIGHTS_BOOKING_STRING_ID);
					}
					suppressFlightsDone = true;
				}
			}
		}
	}

	public void selectPOSFromLocale(Locale l) {
		String countrySelection = mRes.getString(mLocaleUtils.LOCALE_TO_COUNTRY.get(l));
		delay(1);
		clickOnText(countrySelection);
		delay(1);
		if (searchText(OKString())) {
			clickOKString();
		}
		else if (searchText(AcceptString())) {
			clickAcceptString();
		}
		else {
			if (searchText("OK", 1, false, true)) {
				clickOnText("OK");
			}
			else {
				enterLog(TAG, "Trying to move on without an 'OK' click");
			}
		}
	}
}
