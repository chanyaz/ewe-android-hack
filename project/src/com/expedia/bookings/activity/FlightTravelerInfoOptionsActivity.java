package com.expedia.bookings.activity;

import android.content.Context;
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
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightTravelerInfoOneFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener;
import com.expedia.bookings.fragment.FlightTravelerInfoThreeFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoTwoFragment;
import com.expedia.bookings.fragment.FlightTravelerSaveDialogFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;

public class FlightTravelerInfoOptionsActivity extends SherlockFragmentActivity implements TravelerInfoYoYoListener {
	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";
	public static final String TWO_FRAGMENT_TAG = "TWO_FRAGMENT_TAG";
	public static final String THREE_FRAGMENT_TAG = "THREE_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private Context mContext;

	private FlightTravelerInfoOptionsFragment mOptionsFragment;
	private FlightTravelerInfoOneFragment mOneFragment;
	private FlightTravelerInfoTwoFragment mTwoFragment;
	private FlightTravelerInfoThreeFragment mThreeFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;
	private YoYoPosition mBeforeSaveDialogPos;

	private int mTravelerIndex;

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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			mMenuNext = menu.findItem(R.id.menu_next);
			mMenuDone = menu.findItem(R.id.menu_done);

			displayActionBarTitleBasedOnState();
			displayActionItemBasedOnState();

		}

		return super.onPrepareOptionsMenu(menu);
	}

	public void setShowNextButton(boolean showNext) {
		if (mMenuNext != null) {
			mMenuNext.setEnabled(showNext);
			mMenuNext.setVisible(showNext);
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
				setShowNextButton(true);
				setShowDoneButton(false);
				break;
			case TWO:
				if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
					setShowNextButton(true);
					setShowDoneButton(false);
				}
				else {
					setShowNextButton(false);
					setShowDoneButton(true);
				}
				break;
			case THREE:
				setShowNextButton(false);
				setShowDoneButton(true);
				break;
			case OPTIONS:
				setShowNextButton(false);
				setShowDoneButton(false);
				break;
			case SAVE:
			default:
				setShowNextButton(false);
				setShowDoneButton(true);
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			if (mPos.compareTo(YoYoPosition.OPTIONS) == 0) {
				setShowNextButton(false);
				setShowDoneButton(false);
			}
			else {
				setShowNextButton(false);
				setShowDoneButton(true);
			}
		}
		else if (mMode.equals(YoYoMode.NONE)) {
			setShowNextButton(false);
			setShowDoneButton(false);
		}
	}

	public void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		String titleStr = getString(R.string.traveler_information);
		if (mPos != null) {
			switch (mPos) {
			case THREE:
				titleStr = getString(R.string.passport);
				break;
			case ONE:
			case TWO:
			case SAVE:
			case OPTIONS:
			default:
				titleStr = getString(R.string.traveler_information);
			}
		}
		actionBar.setTitle(titleStr);
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

		mContext = this;

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
			}
		}

		//Show the options fragment
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
		}
		else {
			mPos = YoYoPosition.OPTIONS;
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

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
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

	private boolean workingTravelerNameChanged() {
		if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
			Traveler working = Db.getWorkingTravelerManager().getWorkingTraveler();
			Traveler base = Db.getWorkingTravelerManager().getBaseTraveler();
			if (base.getFirstName().trim().compareTo(working.getFirstName().trim()) == 0
					&& base.getLastName().trim().compareTo(working.getLastName().trim()) == 0) {
				return false;
			}
			else {
				return true;
			}
		}
		return false;
	}

	private boolean workingTravelerChanged() {
		if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
			return Db.getWorkingTravelerManager().getWorkingTraveler()
					.compareTo(Db.getWorkingTravelerManager().getBaseTraveler()) != 0;
		}
		return false;
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
					if (Db.getWorkingTravelerManager().getWorkingTraveler().getSaveTravelerToExpediaAccount()) {
						if (workingTravelerNameChanged()) {
							//If we had already set the save flag, but we now changed the first or last name, we now unset the save flag, and show the dialog again in the future
							Db.getWorkingTravelerManager().getWorkingTraveler().resetTuid();//If we changed the name, we don't consider them the same traveler
							Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(false);
						}
					}
					displayTravelerEntryTwo();
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
						displayTravelerEntryThree();
					}
					else {
						if (User.isLoggedIn(this)
								&& !Db.getWorkingTravelerManager().getWorkingTraveler()
										.getSaveTravelerToExpediaAccount()) {
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
					if (User.isLoggedIn(this)
							&& !Db.getWorkingTravelerManager().getWorkingTraveler().getSaveTravelerToExpediaAccount()) {
						displaySaveDialog();
					}
					else {
						displayCheckout();
					}
				}
				break;
			case SAVE:
				OmnitureTracking.trackPageLoadFlightTravelerEditSave(mContext);
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
					if (User.isLoggedIn(this)) {
						if (workingTravelerNameChanged()) {
							//If we changed the name, we don't consider them the same traveler
							Db.getWorkingTravelerManager().getWorkingTraveler().resetTuid();
							displaySaveDialog();
						}
						else if (workingTravelerChanged()
								&& !Db.getWorkingTravelerManager().getWorkingTraveler()
										.getSaveTravelerToExpediaAccount()) {
							//If the traveler changed and we weren't saving before, ask again.
							displaySaveDialog();
						}
						else {
							Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
									Db.getWorkingTravelerManager().getWorkingTraveler());
							displayOptions();
						}
					}
					else {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
								Db.getWorkingTravelerManager().getWorkingTraveler());
						displayOptions();
					}
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					if (User.isLoggedIn(this) && workingTravelerChanged()
							&& !Db.getWorkingTravelerManager().getWorkingTraveler().getSaveTravelerToExpediaAccount()) {
						displaySaveDialog();
					}
					else {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
								Db.getWorkingTravelerManager().getWorkingTraveler());
						displayOptions();
					}
				}
				break;
			case THREE:
				if (validate(mThreeFragment)) {
					if (User.isLoggedIn(this) && workingTravelerChanged()
							&& !Db.getWorkingTravelerManager().getWorkingTraveler().getSaveTravelerToExpediaAccount()) {
						displaySaveDialog();
					}
					else {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
								Db.getWorkingTravelerManager().getWorkingTraveler());
						displayOptions();
					}
				}
				break;
			case SAVE:
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
						Db.getWorkingTravelerManager().getWorkingTraveler());
				displayOptions();
				break;
			case OPTIONS:
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
				//If we are backing up we want to restore the base traveler...
				if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
							Db.getWorkingTravelerManager().getBaseTraveler());
				}
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
				//If we are backing up we want to restore the base traveler...
				if (Db.getWorkingTravelerManager().getBaseTraveler() != null) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
							Db.getWorkingTravelerManager().getBaseTraveler());
				}
				displayOptions();
				break;
			case SAVE:
				if(mBeforeSaveDialogPos != null){
					switch(mBeforeSaveDialogPos){
					case ONE:
						displayTravelerEntryOne();
						break;
					case TWO:
						displayTravelerEntryTwo();
						break;
					case THREE:
						displayTravelerEntryThree();
						break;
					default:
						displayOptions();
					}
				}else{
					displayOptions();
				}
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
		supportInvalidateOptionsMenu();
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
		supportInvalidateOptionsMenu();

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
		supportInvalidateOptionsMenu();

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
		supportInvalidateOptionsMenu();

	}

	@Override
	public void displaySaveDialog() {
		mBeforeSaveDialogPos = mPos;
		mPos = YoYoPosition.SAVE;
		supportInvalidateOptionsMenu();
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
		Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerIndex);
		Db.getWorkingTravelerManager().clearWorkingTraveler(this);
		finish();
	}
}
