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
		mSolo.pressMenuItem(2);
		delay(5);

		//Robotium handles different OS versions' menus in different ways
		String settings = mRes.getString(R.string.Settings);
		if (mSolo.searchText(settings, true)) {
			mSolo.clickOnText(settings);
			delay(1);
		}

		mSolo.scrollDown();
		ArrayList<View> a = mSolo.getCurrentViews();
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
	}

	@Override
	public void selectLocation(String location) {
		EditText locationEditText = (EditText) mSolo.getView(R.id.location_edit_text);

		enterLog(TAG, "Selecting location: " + location);

		mSolo.clickOnView(locationEditText);
		mSolo.clearEditText(locationEditText);
		mSolo.typeText(locationEditText, location);
	}

	public void pressToSearch() {
		View searchForHotelsBtn = mSolo.getView(R.id.search_button);
		mSolo.clickOnView(searchForHotelsBtn);
		enterLog(TAG, "Pressed search button");
	}

	@Override
	public void selectHotel(int index) {
		enterLog(TAG, "Selecting hotel from list with index: " + index);
		mSolo.clickInList(index);
	}

	public void pressSeeDetails() {
		View seeDetailsBtn = mSolo.getView(R.id.more_button);
		mSolo.clickOnView(seeDetailsBtn);
		enterLog(TAG, "Pressed to see hotel details");
	}

	public void pressSelectRoom() {
		delay();
		String selectRoomButton = mSolo.getString(R.string.select_room);
		mSolo.clickOnText(selectRoomButton);
		enterLog(TAG, "Pressed to select first room");
	}

	public void pressBookingInfo() {
		View enterBookingInfoBtn = mSolo.getView(R.id.complete_booking_info_button);
		mSolo.clickOnView(enterBookingInfoBtn);
		enterLog(TAG, "Pressed to see booking info");
	}

	//Button to get to login screen
	public void pressLoginButton() {
		String expediaAccount = mRes.getString(R.string.expedia_account);
		mSolo.clickOnText(expediaAccount);
		enterLog(TAG, "Pressed to log in for booking");
	}

	public void enterLoginCredentials() {
		mSolo.typeText(0, mUser.mLoginEmail);
		delay(1);
		mSolo.typeText((EditText) mSolo.getView(R.id.password_edit_text), mUser.mLoginPassword);
		mSolo.clickOnView(mSolo.getView(R.id.log_in_with_expedia_btn));
		enterLog(TAG, "Enter login credentials and pressed button");
	}

	public void enterCCVAndZIP() {
		mSolo.enterText((EditText) mSolo.getView(R.id.security_code_edit_text), mUser.mCCV);
		mSolo.enterText(mSolo.getEditText(mRes.getString(R.string.address_postal_code_hint)), mUser.mZIPCode);
		enterLog(TAG, "Entered CCV: " + mUser.mCCV + "and ZIP " + mUser.mZIPCode);
	}

	public void enterBookingInfo() {
		EditText firstName = (EditText) mSolo.getView(R.id.first_name_edit_text);
		EditText lastName = (EditText) mSolo.getView(R.id.last_name_edit_text);
		EditText phoneNumber = (EditText) mSolo.getView(R.id.edit_phone_number);
		EditText email = (EditText) mSolo.getView(R.id.edit_email_address);
		EditText ccNumber = (EditText) mSolo.getView(R.id.edit_creditcard_number);
		EditText ccExpirationMonth = (EditText) mSolo.getView(R.id.expiration_month_edit_text);
		EditText ccExpirationYear = (EditText) mSolo.getView(R.id.expiration_year_edit_text);
		EditText ccCCV = (EditText) mSolo.getView(R.id.security_code_edit_text);
		EditText zipCode = (EditText) mSolo.getView(R.id.edit_address_postal_code);

		mSolo.enterText(firstName, mUser.mFirstName);
		mSolo.enterText(lastName, mUser.mLastName);

		enterLog("!!! ", "111" + mUser.mPhoneNumber);
		mSolo.enterText(phoneNumber, mUser.mPhoneNumber);

		enterLog("!!! ", "111" + mUser.mLoginEmail);
		mSolo.enterText(email, mUser.mLoginEmail);

		enterLog("!!! ", "111" + mUser.mCreditCardNumber);
		mSolo.enterText(ccNumber, mUser.mCreditCardNumber);

		enterLog("!!! ", "111" + mUser.mCardExpMonth);
		mSolo.enterText(ccExpirationMonth, mUser.mCardExpMonth);

		enterLog("!!! ", "111" + mUser.mCardExpYear);
		mSolo.enterText(ccExpirationYear, mUser.mCardExpYear);

		enterLog("!!! ", "111" + mUser.mCCV);
		mSolo.enterText(ccCCV, mUser.mCCV);

		enterLog("!!! ", "111" + mUser.mZIPCode);
		mSolo.enterText(zipCode, mUser.mZIPCode);
	}

	public void pressToConfirmAndBook() {
		View confirmAndBook = mSolo.getView(R.id.menu_confirm_book);
		mSolo.clickOnView(confirmAndBook);
		enterLog(TAG, "Pressed to confirm & book. Waiting...");
		mSolo.waitForDialogToClose(20000);
	}
}
