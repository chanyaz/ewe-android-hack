package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.HotelTravelerInfoOneFragment;
import com.expedia.bookings.fragment.HotelTravelerInfoOptionsFragment;
import com.expedia.bookings.fragment.HotelTravelerInfoOptionsFragment.TravelerInfoYoYoListener;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class HotelTravelerInfoOptionsActivity extends FragmentActivity implements TravelerInfoYoYoListener {

	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";
	private static final String STATE_TAG_START_FIRST_NAME = "STATE_TAG_START_FIRST_NAME";
	private static final String STATE_TAG_START_LAST_NAME = "STATE_TAG_START_LAST_NAME";

	private HotelTravelerInfoOptionsFragment mOptionsFragment;
	private HotelTravelerInfoOneFragment mOneFragment;

	private MenuItem mMenuDone;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;

	private int mTravelerIndex;

	//for determining if the name changed...
	private String mStartFirstName = "";
	private String mStartLastName = "";

	//Where we want to return to after our action
	public enum YoYoPosition {
		OPTIONS,
		ONE
	}

	public interface Validatable {
		public boolean validate();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Show the options fragment
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
		}
		else {
			if (canOnlySelectNewTraveller()) {
				mMode = YoYoMode.YOYO;
				displayTravelerEntryOne();
			}
			else {
				mPos = YoYoPosition.OPTIONS;
			}
		}

		//Which traveler are we working with
		mTravelerIndex = getIntent().getIntExtra(Codes.TRAVELER_INDEX, 0);

		//If we have a working traveler that was cached we try to load it from disk...
		WorkingTravelerManager travMan = Db.getWorkingTravelerManager();
		if (travMan.getAttemptToLoadFromDisk() && travMan.hasTravelerOnDisk(this)) {
			//Load up the traveler from disk
			travMan.loadWorkingTravelerFromDisk(this);
			if (mPos.compareTo(YoYoPosition.OPTIONS) == 0) {
				//If we don't have a saved state, but we do have a saved temp traveler go ahead to the entry screens
				mPos = YoYoPosition.ONE;
				mMode = YoYoMode.YOYO;
			}
		}
		else {
			//If we don't load it from disk, then we delete the file.
			travMan.deleteWorkingTravelerFile(this);
		}

		switch (mPos) {
		case OPTIONS:
			displayOptions();
			break;
		case ONE:
			displayTravelerEntryOne();
			break;
		default:
			displayOptions();
		}

		//Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
		mStartFirstName = savedInstanceState.getString(STATE_TAG_START_FIRST_NAME) != null ? savedInstanceState
			.getString(STATE_TAG_START_FIRST_NAME) : "";
		mStartLastName = savedInstanceState.getString(STATE_TAG_START_LAST_NAME) != null ? savedInstanceState
			.getString(STATE_TAG_START_LAST_NAME) : "";

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_TAG_MODE, mMode.name());
		outState.putString(STATE_TAG_DEST, mPos.name());
		outState.putString(STATE_TAG_START_FIRST_NAME, mStartFirstName);
		outState.putString(STATE_TAG_START_LAST_NAME, mStartLastName);
		super.onSaveInstanceState(outState);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ActionBar/Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_yoyo, menu);
		mMenuDone = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_done);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			return moveBackwards();
		}
		case R.id.menu_done:
			moveForward();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			mMenuDone = menu.findItem(R.id.menu_done);

			displayActionBarTitleBasedOnState();
			displayActionItemBasedOnState();

		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		if (!moveBackwards()) {
			super.onBackPressed();
		}
	}

	public void setShowDoneButton(boolean showDone) {
		if (mMenuDone != null) {
			mMenuDone.setEnabled(showDone);
			mMenuDone.setVisible(showDone);
		}
	}

	public void displayActionItemBasedOnState() {
		if (mMode == null) {
			return;
		}
		else if (mPos != null && mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case ONE:
			default:
				setShowDoneButton(true);
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			if (mPos.compareTo(YoYoPosition.OPTIONS) == 0) {
				setShowDoneButton(true);
			}
			else {
				setShowDoneButton(true);
			}
		}
		else if (mMode.equals(YoYoMode.NONE)) {
			setShowDoneButton(false);
		}
	}

	public void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		String titleStr = getString(R.string.traveler_information);
		if (mPos != null) {
			switch (mPos) {
			case ONE:
			case OPTIONS:
			default:
				titleStr = getString(R.string.traveler_information);
			}
		}
		actionBar.setTitle(titleStr);
	}

	public boolean validate(Validatable validatable) {
		if (validatable == null) {
			return false;
		}
		else {
			return validatable.validate();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// YoYo listener

	@Override
	public void moveForward() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS: {
				displayTravelerEntryOne();
				break;
			}
			case ONE: {
				if (validate(mOneFragment)) {
					displayCheckout();
				}
				break;
			}
			default: {
				Log.i("YOYO FAIL - mpos:" + mPos);
				break;
			}
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case ONE:
				if (validate(mOneFragment)) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
						Db.getWorkingTravelerManager().getWorkingTraveler());
					displayOptions();
				}
				break;
			case OPTIONS:
			default:
				Log.i("YOYO FAIL - mpos:" + mPos);
				break;
			}
		}
		else if (mMode.equals(YoYoMode.NONE)) {
			displayCheckout();
		}
	}

	@Override
	public void setMode(YoYoMode mode) {
		mMode = mode;
	}

	@Override
	public boolean moveBackwards() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				displayCheckout();
				break;
			case ONE:
				//If we are backing up we want to restore the base traveler...
				if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
						Db.getWorkingTravelerManager().getBaseTraveler());
				}
				displayOptions();
				break;
			default:
				Ui.showToast(this, "FAIL");
				return false;
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case ONE:
				//If we are backing up we want to restore the base traveler...
				if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
						Db.getWorkingTravelerManager().getBaseTraveler());
				}
				displayOptions();
				break;
			case OPTIONS:
			default:
				Ui.showToast(this, "FAIL");
				return false;
			}
		}
		else if (mMode.equals(YoYoMode.NONE)) {
			displayCheckout();
		}
		return true;
	}

	@Override
	public void displayOptions() {
		Ui.hideKeyboard(this, InputMethodManager.HIDE_NOT_ALWAYS);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOptionsFragment = Ui.findSupportFragment(this, OPTIONS_FRAGMENT_TAG);
		if (mOptionsFragment == null) {
			mOptionsFragment = HotelTravelerInfoOptionsFragment.newInstance();
		}
		if (!mOptionsFragment.isAdded()) {
			ft.replace(android.R.id.content, mOptionsFragment, OPTIONS_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.OPTIONS;
		mMode = YoYoMode.NONE;
		supportInvalidateOptionsMenu();
	}

	@Override
	public void displayTravelerEntryOne() {
		mStartFirstName = Db.getWorkingTravelerManager().getWorkingTraveler().getFirstName();
		mStartLastName = Db.getWorkingTravelerManager().getWorkingTraveler().getLastName();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOneFragment = Ui.findSupportFragment(this, ONE_FRAGMENT_TAG);
		if (mOneFragment == null) {
			mOneFragment = HotelTravelerInfoOneFragment.newInstance();
		}
		if (!mOneFragment.isAdded()) {
			ft.replace(android.R.id.content, mOneFragment, ONE_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.ONE;
		supportInvalidateOptionsMenu();
	}

	@Override
	public void displayCheckout() {
		//First we commit our traveler stuff...
		Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerIndex, this);
		Db.getWorkingTravelerManager().clearWorkingTraveler(this);
		finish();
	}

	private boolean canOnlySelectNewTraveller() {
		if (BookingInfoUtils.getAlternativeTravelers(this).size() > 0) {
			return false;
		}
		HotelTravelerFlowState validationState = HotelTravelerFlowState.getInstance(this);
		Traveler travelerinfo = Db.getWorkingTravelerManager().getWorkingTraveler();
		boolean travelervalid = validationState.hasValidTraveler(travelerinfo);
		return !travelervalid;
	}
}
