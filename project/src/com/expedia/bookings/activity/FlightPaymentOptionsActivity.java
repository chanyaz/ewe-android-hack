package com.expedia.bookings.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightPaymentAddressFragment;
import com.expedia.bookings.fragment.FlightPaymentCreditCardFragment;
import com.expedia.bookings.fragment.FlightPaymentOptionsFragment;
import com.expedia.bookings.fragment.FlightPaymentSaveDialogFragment;
import com.expedia.bookings.fragment.FlightPaymentOptionsFragment.FlightPaymentYoYoListener;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class FlightPaymentOptionsActivity extends SherlockFragmentActivity implements FlightPaymentYoYoListener {

	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ADDRESS_FRAGMENT_TAG = "ADDRESS_FRAGMENT_TAG";
	public static final String CREDIT_CARD_FRAGMENT_TAG = "CREDIT_CARD_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private FlightPaymentOptionsFragment mOptionsFragment;
	private FlightPaymentAddressFragment mAddressFragment;
	private FlightPaymentCreditCardFragment mCCFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;

	//Define the states of navigation
	public enum YoYoMode {
		NONE, YOYO, EDIT
	}

	//Where we want to return to after our action
	private enum YoYoPosition {
		OPTIONS, ADDRESS, CREDITCARD, SAVE
	}

	public interface Validatable {
		public boolean attemptToLeave();
	}

	public void displayOptions() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOptionsFragment = Ui.findSupportFragment(this, OPTIONS_FRAGMENT_TAG);
		if (mOptionsFragment == null) {
			mOptionsFragment = FlightPaymentOptionsFragment.newInstance();
		}
		ft.replace(android.R.id.content, mOptionsFragment, OPTIONS_FRAGMENT_TAG);
		ft.commit();
		mPos = YoYoPosition.OPTIONS;
		mMode = YoYoMode.NONE;
		displayActionItemBasedOnState();
	}

	public void displayAddress() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mAddressFragment = Ui.findSupportFragment(this, ADDRESS_FRAGMENT_TAG);
		if (mAddressFragment == null) {
			mAddressFragment = FlightPaymentAddressFragment.newInstance();
		}
		ft.replace(android.R.id.content, mAddressFragment, ADDRESS_FRAGMENT_TAG);
		ft.commit();
		mPos = YoYoPosition.ADDRESS;
		displayActionItemBasedOnState();
	}

	public void displayCreditCard() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mCCFragment = Ui.findSupportFragment(this, CREDIT_CARD_FRAGMENT_TAG);
		if (mCCFragment == null) {
			mCCFragment = FlightPaymentCreditCardFragment.newInstance();
		}
		ft.replace(android.R.id.content, mCCFragment, CREDIT_CARD_FRAGMENT_TAG);
		ft.commit();
		mPos = YoYoPosition.CREDITCARD;
		displayActionItemBasedOnState();
	}

	public void displaySaveDialog() {
		DialogFragment newFragment = FlightPaymentSaveDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), SAVE_FRAGMENT_TAG);
		mPos = YoYoPosition.SAVE;
		displayActionItemBasedOnState();
	}

	public void displayCheckout() {
		finish();
	}

	public void moveForward() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				displayAddress();
				break;
			case ADDRESS:
				if (validate(mAddressFragment)) {
					displayCreditCard();
				}
				break;
			case CREDITCARD:
				if (validate(mCCFragment)) {
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
			case ADDRESS:
				if (validate(mAddressFragment)) {
					displayOptions();
				}
				break;
			case CREDITCARD:
				if (validate(mCCFragment)) {
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

	public void setMode(YoYoMode mode) {
		mMode = mode;
	}

	public boolean moveBackwards() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				displayCheckout();
				break;
			case ADDRESS:
				displayOptions();
				break;
			case CREDITCARD:
				displayAddress();
				break;
			case SAVE:
				displayCreditCard();
				break;
			default:
				Ui.showToast(this, "FAIL");
				return false;
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case ADDRESS:
				displayOptions();
				break;
			case CREDITCARD:
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

	public boolean validate(Validatable validatable) {
		if (validatable == null) {
			return false;
		}
		else {
			return validatable.attemptToLeave();
		}
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
			case ADDRESS:
				displayAddress();
				break;
			case CREDITCARD:
				displayCreditCard();
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
			case ADDRESS:
				setMenuItemVisibilities(false);
				break;
			case CREDITCARD:
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

}