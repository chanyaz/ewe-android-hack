package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightTravelerInfoOneFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener;
import com.expedia.bookings.fragment.FlightTravelerInfoThreeFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoTwoFragment;
import com.expedia.bookings.fragment.FlightTravelerSaveDialogFragment;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

public class FlightTravelerInfoOptionsActivity extends SherlockFragmentActivity implements TravelerInfoYoYoListener {
	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";
	public static final String TWO_FRAGMENT_TAG = "TWO_FRAGMENT_TAG";
	public static final String THREE_FRAGMENT_TAG = "THREE_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private FlightTravelerInfoOptionsFragment mOptionsFragment;
	private FlightTravelerInfoOneFragment mOneFragment;
	private FlightTravelerInfoTwoFragment mTwoFragment;
	private FlightTravelerInfoThreeFragment mThreeFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;

	//Where we want to return to after our action
	private enum YoYoPosition {
		OPTIONS, ONE, TWO, THREE, SAVE
	}

	public interface Validatable {
		public boolean validate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_yoyo, menu);
		mMenuNext = menu.findItem(R.id.menu_next);
		mMenuNext.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveForward();
			}
		});

		mMenuDone = menu.findItem(R.id.menu_done);
		mMenuDone.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveForward();
			}
		});
		displayActionItemBasedOnState();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return moveBackwards();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void setMenuItemVisibilities(boolean showDone) {
		if (mMenuNext != null) {
			mMenuNext.setVisible(!showDone);
			mMenuNext.setEnabled(!showDone);

		}
		if (mMenuDone != null) {
			mMenuDone.setVisible(showDone);
			mMenuDone.setEnabled(showDone);
		}
	}

	public void displayActionItemBasedOnState() {
		if (mMode == null) {
			return;
		}
		else if (mPos != null && mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case ONE:
				setMenuItemVisibilities(false);
				break;
			case TWO:
				setMenuItemVisibilities(!User.isLoggedIn(this));
				break;
			case THREE:
				setMenuItemVisibilities(!User.isLoggedIn(this));
			case SAVE:
			case OPTIONS:
			default:
				setMenuItemVisibilities(true);
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			setMenuItemVisibilities(true);
		}
		else if (mMode.equals(YoYoMode.NONE)) {
			//TODO: This should set both to invisible, but then they never return, so for now we display done
			setMenuItemVisibilities(true);
		}
	}

	@Override
	public void onBackPressed() {
		if (!moveBackwards()) {
			super.onBackPressed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_TAG_MODE, mMode.name());
		outState.putString(STATE_TAG_DEST, mPos.name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		//Show the options fragment
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
			switch (mPos) {
			case OPTIONS:
				displayOptions();
				break;
			case ONE:
				displayTravelerEntryOne();
				break;
			case TWO:
				displayTravelerEntryTwo();
				break;
			case THREE:
				displayTravelerEntryThree();
				break;
			case SAVE:
				displaySaveDialog();
				break;
			default:
				displayOptions();
			}
		}
		else {
			displayOptions();
		}

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = trip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	public boolean validate(Validatable validatable) {
		if (validatable == null) {
			return false;
		}
		else {
			return validatable.validate();
		}
	}

	//////////////////////////////////////////
	////

	@Override
	public void moveForward() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				displayTravelerEntryOne();
				break;
			case ONE:
				if (validate(mOneFragment)) {
					displayTravelerEntryTwo();
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
						displayTravelerEntryThree();
					}
					else {
						if (User.isLoggedIn(this)) {
							displaySaveDialog();
						}
						else {
							displayCheckout();
						}
					}
				}
				break;
			case THREE:
				if (validate(mThreeFragment)) {
					if (User.isLoggedIn(this)) {
						displaySaveDialog();
					}
					else {
						displayCheckout();
					}
				}
				break;
			case SAVE:
				displayCheckout();
				break;
			default:
				Ui.showToast(this, "FAIL");
				break;
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case ONE:
				if (validate(mOneFragment)) {
					displayOptions();
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					displayOptions();
				}
				break;
			case THREE:
				if (validate(mThreeFragment)) {
					displayOptions();
				}
				break;
			case OPTIONS:
			case SAVE:
			default:
				Ui.showToast(this, "FAIL");
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
				displayOptions();
				break;
			case TWO:
				displayTravelerEntryOne();
				break;
			case THREE:
				displayTravelerEntryTwo();
				break;
			case SAVE:
				closeSaveDialog();
				displayTravelerEntryThree();
				break;
			default:
				Ui.showToast(this, "FAIL");
				return false;
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case ONE:
			case TWO:
			case THREE:
				displayOptions();
				break;
			case OPTIONS:
			case SAVE:
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
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOptionsFragment = Ui.findSupportFragment(this, OPTIONS_FRAGMENT_TAG);
		if (mOptionsFragment == null) {
			mOptionsFragment = FlightTravelerInfoOptionsFragment.newInstance();
		}
		if (!mOptionsFragment.isAdded()) {
			ft.replace(android.R.id.content, mOptionsFragment, OPTIONS_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.OPTIONS;
		mMode = YoYoMode.NONE;
		displayActionItemBasedOnState();
	}

	@Override
	public void displayTravelerEntryOne() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOneFragment = Ui.findSupportFragment(this, ONE_FRAGMENT_TAG);
		if (mOneFragment == null) {
			mOneFragment = FlightTravelerInfoOneFragment.newInstance();
		}
		if (!mOneFragment.isAdded()) {
			ft.replace(android.R.id.content, mOneFragment, ONE_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.ONE;
		displayActionItemBasedOnState();

	}

	@Override
	public void displayTravelerEntryTwo() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mTwoFragment = Ui.findSupportFragment(this, TWO_FRAGMENT_TAG);
		if (mTwoFragment == null) {
			mTwoFragment = FlightTravelerInfoTwoFragment.newInstance();
		}
		if (!mTwoFragment.isAdded()) {
			ft.replace(android.R.id.content, mTwoFragment, TWO_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.TWO;
		displayActionItemBasedOnState();

	}

	@Override
	public void displayTravelerEntryThree() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mThreeFragment = Ui.findSupportFragment(this, THREE_FRAGMENT_TAG);
		if (mThreeFragment == null) {
			mThreeFragment = FlightTravelerInfoThreeFragment.newInstance();
		}
		if (!mThreeFragment.isAdded()) {
			ft.replace(android.R.id.content, mThreeFragment, THREE_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.THREE;
		displayActionItemBasedOnState();

	}

	@Override
	public void displaySaveDialog() {
		mPos = YoYoPosition.SAVE;
		displayActionItemBasedOnState();
		DialogFragment newFragment = FlightTravelerSaveDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), SAVE_FRAGMENT_TAG);
	}

	private void closeSaveDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment dialog = getSupportFragmentManager().findFragmentByTag(SAVE_FRAGMENT_TAG);
		if (dialog != null) {
			ft.remove(dialog);
		}
		ft.commit();
	}

	@Override
	public void displayCheckout() {
		finish();
	}
}
