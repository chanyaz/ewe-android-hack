package com.expedia.bookings.test.utils;

import java.util.ArrayList;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;

public class TabletsRobotHelper extends HotelsRobotHelper {

	private Solo mSolo;
	private Resources mRes;
	private HotelsUserData mUser;

	public UserLocaleUtils mLocaleUtils;

	public static final String TAG = "Tablets Robotium Helper";

	public TabletsRobotHelper(Solo solo, Resources res) {
		this(solo, res, new HotelsUserData());
	}

	//Constructor for user created book user container
	public TabletsRobotHelper(Solo solo, Resources res, HotelsUserData customUser) {
		super(solo, res, customUser);

		mUser = customUser;
		mSolo = solo;
		mRes = res;

	}

	@Override
	public void setSpoofBookings() {
		boolean spoofBookingsDone = false;
		boolean suppressFlightsDone = false;

		screenshot("Tablet - main screen");

		mSolo.sendKey(Solo.MENU);
		mSolo.clickOnText(mRes.getString(R.string.Settings));

		screenshot("Tablet - settings");

		delay(1);
		mSolo.scrollDown();
		ArrayList<View> a = mSolo.getCurrentViews();
		Log.v("!!!", "!!! " + a.size());
		for (int i = 0; i < a.size(); i++) {
			Log.v("!!!", "!!! " + a.toString());
			if (spoofBookingsDone && suppressFlightsDone) {
				break;
			}
			View currentView = a.get(i);
			if (currentView instanceof CheckBox) {
				CheckBox currentCheckBox = (CheckBox) currentView;
				if (currentCheckBox.getId() == R.id.preference_spoof_booking_checkbox) {
					if (!currentCheckBox.isChecked()) {
						mSolo.clickOnText("Spoof hotel bookings");
					}
					spoofBookingsDone = true;
				}
				else if (currentCheckBox.getId() == R.id.preference_suppress_flight_booking_checkbox) {
					if (!currentCheckBox.isChecked()) {
						mSolo.clickOnText("Suppress Flight Bookings");
					}
					suppressFlightsDone = true;
				}
			}
		}
		mSolo.goBack();
	}

	@Override
	public void launchFlights() {
		mSolo.clickOnText(mRes.getString(R.string.nav_flights));
		screenshot("Tablet Flights search");
	}

	@Override
	public void selectLocation(String location) {
		EditText locationEditText = (EditText) mSolo.getView(R.id.location_edit_text);

		enterLog(TAG, "Selecting location: " + location);

		mSolo.clickOnView(locationEditText);
		mSolo.clearEditText(locationEditText);
		mSolo.typeText(locationEditText, location);
		landscape();
		portrait();
	}

	public void pressToSearch() {
		View searchForHotelsBtn = mSolo.getView(R.id.search_button);
		try {
			mSolo.clickOnView(searchForHotelsBtn);
		}
		catch (Error e) {
			enterLog(TAG,
					"Error when trying to press to search for hotels. " +
							"Keyboard may be displayed, so trying to close it.");
			mSolo.goBack();
			delay(1);
			mSolo.clickOnView(searchForHotelsBtn);
		}

		enterLog(TAG, "Pressed search button");
		landscape();
		portrait();

		int counter = 0;
		String loadingHotels = mRes.getString(R.string.loading_hotels);
		while (mSolo.searchText(loadingHotels, true) && counter < 10) {
			delay(3);
			counter++;
		}
	}

	@Override
	public void selectHotel(int index) {
		enterLog(TAG, "Selecting hotel from list with index: " + index);
		screenshot("Tablet: Search results");
		mSolo.clickInList(index);
		landscape();
		portrait();
	}

	public void pressSeeDetails() {
		View seeDetailsBtn = mSolo.getView(R.id.more_button);
		mSolo.clickOnView(seeDetailsBtn);
		enterLog(TAG, "Pressed to see hotel details");
		delay(1);
		screenshot("Tablet: Hotel details");
		landscape();
		portrait();
	}

	public void pressSelectRoom() {
		delay();
		String selectRoomButton = mSolo.getString(R.string.select_room);
		mSolo.clickOnText(selectRoomButton);
		enterLog(TAG, "Pressed to select first room");
		screenshot("Tablet: Room info");
		landscape();
		portrait();
	}

	public void pressBookingInfo() {
		View enterBookingInfoBtn = mSolo.getView(R.id.complete_booking_info_button);
		mSolo.clickOnView(enterBookingInfoBtn);
		enterLog(TAG, "Pressed to see booking info");
		landscape();
		portrait();
	}

	//Button to get to login screen
	public void pressLoginButton() {
		screenshot("Tablet: Booking screen");
		String expediaAccount = mRes.getString(R.string.expedia_account);
		mSolo.clickOnText(expediaAccount);
		enterLog(TAG, "Pressed to log in for booking");
		landscape();
		portrait();
	}

	public void enterLoginCredentials() {
		mSolo.typeText(0, mUser.mLoginEmail);
		delay(1);
		mSolo.typeText((EditText) mSolo.getView(R.id.password_edit_text), mUser.mLoginPassword);
		delay(1);

		landscape();
		portrait();

		delay(1);
		screenshot("Tablet: Log in screen");
		mSolo.clickOnView(mSolo.getView(R.id.log_in_with_expedia_btn));
		enterLog(TAG, "Enter login credentials and pressed button");
	}

	public void runHotelHappyPath() {
		ignoreSweepstakesActivity();
		delay(5);
		setSpoofBookings();
		selectLocation(mUser.mHotelSearchCity);
		pressToSearch();

		selectHotel(3);
		delay();

		pressSeeDetails();
		mSolo.scrollDown();
		mSolo.scrollToTop();

		pressSelectRoom();
		pressBookingInfo();
		delay();

		try {
			logInAndBook(true, true);
		}
		catch (Exception e) {
			Log.e(TAG, "Failed while logging in/booking.", e);
		}
	}
}
