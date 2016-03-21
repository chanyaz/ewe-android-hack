package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.BirthDateInvalidDialog;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.fragment.FlightTravelerInfoOneFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener;
import com.expedia.bookings.fragment.FlightTravelerInfoThreeFragment;
import com.expedia.bookings.fragment.FlightTravelerInfoTwoFragment;
import com.expedia.bookings.fragment.FlightTravelerSaveDialogFragment;
import com.expedia.bookings.fragment.OverwriteExistingTravelerDialogFragment;
import com.expedia.bookings.interfaces.IDialogForwardBackwardListener;
import com.expedia.bookings.model.WorkingTravelerManager.ITravelerUpdateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class FlightTravelerInfoOptionsActivity extends FragmentActivity implements TravelerInfoYoYoListener,
	IDialogForwardBackwardListener {

	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";
	public static final String TWO_FRAGMENT_TAG = "TWO_FRAGMENT_TAG";
	public static final String THREE_FRAGMENT_TAG = "THREE_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";
	private static final String STATE_TAG_START_FIRST_NAME = "STATE_TAG_START_FIRST_NAME";
	private static final String STATE_TAG_START_LAST_NAME = "STATE_TAG_START_LAST_NAME";
	private static final String STATE_TAG_SKIP_OVERVIEW = "STATE_TAG_SKIP_OVERVIEW";

	private static final String DIALOG_SAVING_TRAVELER = "DIALOG_SAVING_TRAVELER";
	private static final String FTAG_INVALID_BIRTHDATE_DIALOG = "FTAG_INVALID_BIRTHDATE_DIALOG";

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

	private boolean mSkipBackStackOverview = false;

	//for determining if the name changed...
	private String mStartFirstName = "";
	private String mStartLastName = "";

	private boolean mIsBailing = false;

	//Where we want to return to after our action
	private enum YoYoPosition {
		OPTIONS, ONE, TWO, THREE, SAVE, SAVING, OVERWRITE_TRAVELER
	}

	public interface Validatable {
		boolean validate();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Recover data if it was flushed from memory
		if (Db.getTripBucket().isEmpty()) {
			boolean wasSuccess = Db.loadTripBucket(this);
			if (!wasSuccess || Db.getTripBucket().getFlight() == null) {
				finish();
				mIsBailing = true;
			}
		}

		super.onCreate(savedInstanceState);

		if (mIsBailing) {
			return;
		}

		//Which traveler are we working with
		mTravelerIndex = getIntent().getIntExtra(Codes.TRAVELER_INDEX, 0);

		//Show the options fragment
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
		}
		else {
			if (canOnlySelectNewTraveler()) {
				mPos = YoYoPosition.ONE;
				mMode = YoYoMode.YOYO;
				mSkipBackStackOverview = true;
			}
			else {
				mPos = YoYoPosition.OPTIONS;
			}
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
		case OVERWRITE_TRAVELER:
			displayOverwriteDialog();
			break;
		case SAVING:
			if (Db.getWorkingTravelerManager() != null
					&& Db.getWorkingTravelerManager().isCommittingTravelerToAccount()) {
				displaySavingDialog();
			}
			else {
				displayOptions();
			}
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
		mSkipBackStackOverview = savedInstanceState.getBoolean(STATE_TAG_SKIP_OVERVIEW);

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);

		//If the save dialog is showing, we close it, and then we show it again from the onCreate method.
		if (mPos.equals(YoYoPosition.SAVE)) {
			this.closeSaveDialog();
		}

		//If the overwrite dialog is showing, we close it
		if (mPos.equals(YoYoPosition.OVERWRITE_TRAVELER)) {
			this.closeOverwriteDialog();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_TAG_MODE, mMode.name());
		outState.putString(STATE_TAG_DEST, mPos.name());
		outState.putString(STATE_TAG_START_FIRST_NAME, mStartFirstName);
		outState.putString(STATE_TAG_START_LAST_NAME, mStartLastName);
		outState.putBoolean(STATE_TAG_SKIP_OVERVIEW, mSkipBackStackOverview);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (!moveBackwards()) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mOneFragment != null) {
			mOneFragment.onInteraction();
		}

		return super.dispatchTouchEvent(ev);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ActionBar/Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_yoyo, menu);
		mMenuNext = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_next);
		mMenuDone = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_done);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return moveBackwards();
		case R.id.menu_next:
		case R.id.menu_done:
			moveForward();
			return true;
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

	private void displayActionItemBasedOnState() {
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
				if (Db.getTripBucket().getFlight().getFlightTrip().isInternational() || Db.getTripBucket().getFlight().getFlightTrip().isPassportNeeded()) {
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
			case OVERWRITE_TRAVELER:
			case SAVING:
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

	private void setShowNextButton(boolean showNext) {
		if (mMenuNext != null) {
			mMenuNext.setEnabled(showNext);
			mMenuNext.setVisible(showNext);
		}
	}

	private void setShowDoneButton(boolean showDone) {
		if (mMenuDone != null) {
			mMenuDone.setEnabled(showDone);
			mMenuDone.setVisible(showDone);
		}
	}

	private void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = getActionBar();
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
			case OVERWRITE_TRAVELER:
			case SAVING:
			case OPTIONS:
			default:
				titleStr = getString(R.string.traveler_information);
			}
		}
		actionBar.setTitle(titleStr);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// YoYo listener

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
				// If the section didn't validate, and it is because the birthday did not match
				// the passenger category of the traveler, show the dialog.
				else if (!mOneFragment.isBirthdateAligned()) {
					showInvalidBirthdateDialog();
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					if (Db.getTripBucket().getFlight().getFlightTrip().isInternational() || Db.getTripBucket().getFlight().getFlightTrip().isPassportNeeded()) {
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
				if (workingTravelerRequiresOverwritePrompt()) {
					//The user already has a traveler named this...
					displayOverwriteDialog();
				}
				else {
					OmnitureTracking.trackPageLoadFlightTravelerEditSave();
					displayCheckout();
				}
				break;
			case OVERWRITE_TRAVELER:
				OmnitureTracking.trackPageLoadFlightTravelerEditSave();
				displayCheckout();
				break;
			default:
				Log.i("YOYO FAIL - mpos:" + mPos);
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
						else if (workingTravelerDiffersFromBase()
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
				// If the section didn't validate, and it is because the birthday did not match
				// the passenger category of the traveler, show the dialog.
				else if (!mOneFragment.isBirthdateAligned()) {
					showInvalidBirthdateDialog();
				}
				break;
			case TWO:
				if (validate(mTwoFragment)) {
					if (User.isLoggedIn(this) && workingTravelerDiffersFromBase()
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
					if (User.isLoggedIn(this) && workingTravelerDiffersFromBase()
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
				if (workingTravelerRequiresOverwritePrompt()) {
					//The user already has a traveler named this...
					displayOverwriteDialog();
				}
				else {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
							Db.getWorkingTravelerManager().getWorkingTraveler());
					displayOptions();
				}
				break;
			case OVERWRITE_TRAVELER:
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(
						Db.getWorkingTravelerManager().getWorkingTraveler());
				displayOptions();
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

	// moveForward helper methods

	private boolean validate(Validatable validatable) {
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
			return !Strings.equals(mStartFirstName.trim(), working.getFirstName().trim())
				|| !Strings.equals(mStartLastName.trim(), working.getLastName().trim());
		}
		return false;
	}

	private boolean workingTravelerDiffersFromBase() {
		return Db.getWorkingTravelerManager().workingTravelerDiffersFromBase();
	}

	private boolean workingTravelerRequiresOverwritePrompt() {
		return BookingInfoUtils.travelerRequiresOverwritePrompt(this, Db.getWorkingTravelerManager().getWorkingTraveler());
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
				if (mSkipBackStackOverview) {
					displayCheckout();
				}
				else {
					displayOptions();
				}
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
			case OVERWRITE_TRAVELER:
				closeOverwriteDialog();
				displaySaveDialog();
				break;
			case SAVING:
				if (!Db.getWorkingTravelerManager().isCommittingTravelerToAccount()) {
					//if we aren't actually saving this is a bunk state, and we let people leave...
					displayOptions();
				}
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
				if (mBeforeSaveDialogPos != null) {
					switch (mBeforeSaveDialogPos) {
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
				}
				else {
					displayOptions();
				}
				break;
			case OVERWRITE_TRAVELER:
				closeOverwriteDialog();
				displaySaveDialog();
				break;
			case SAVING:
				if (!Db.getWorkingTravelerManager().isCommittingTravelerToAccount()) {
					//if we aren't actually saving this is a bunk state, and we let people leave...
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
		Ui.hideKeyboard(this, InputMethodManager.HIDE_NOT_ALWAYS);

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
		mStartFirstName = Db.getWorkingTravelerManager().getWorkingTraveler().getFirstName();
		mStartLastName = Db.getWorkingTravelerManager().getWorkingTraveler().getLastName();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOneFragment = Ui.findSupportFragment(this, ONE_FRAGMENT_TAG);
		if (mOneFragment == null) {
			mOneFragment = FlightTravelerInfoOneFragment.newInstance(mTravelerIndex);
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

	@Override
	public void displayCheckout() {
		//First we commit our traveler stuff...
		// TODO consider background kill / resume scenario, this code crashes out because Db.getTravelers() is not valid
		FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
		PassengerCategory passengerCategoryToSave = Db.getTravelers().get(mTravelerIndex).getPassengerCategory(searchParams);
		Traveler trav = Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerIndex);
		// If we're going back to checkout without the working traveler having a birthdate,
		// we retain the previously set birthdate.
		if (trav.getBirthDate() == null) {
			trav.setPassengerCategory(passengerCategoryToSave);
		}
		Db.getWorkingTravelerManager().clearWorkingTraveler();
		if (trav.getSaveTravelerToExpediaAccount() && User.isLoggedIn(this)) {
			if (trav.hasTuid()) {
				//Background save..
				Db.getWorkingTravelerManager().commitTravelerToAccount(this, trav, false, null);
				finish();
			}
			else {
				//Display spinner and wait...
				displaySavingDialog();
				ITravelerUpdateListener travelerupdatedListener = new ITravelerUpdateListener() {
					@Override
					public void onTravelerUpdateFinished() {
						ThrobberDialog df = Ui.findSupportFragment(
								FlightTravelerInfoOptionsActivity.this,
								DIALOG_SAVING_TRAVELER);
						if (df != null) {
							df.dismiss();
						}
						finish();
					}

					@Override
					public void onTravelerUpdateFailed() {
						//TODO: we should maybe do more, however all of the local information will still be submitted as checkout info,
						//	so the account update
						Log.e("Saving traveler failure.");
						finish();
					}
				};

				Db.getWorkingTravelerManager().commitTravelerToAccount(this, trav, false, travelerupdatedListener);
			}
		}
		else {
			finish();
		}
	}

	// Private helper methods

	private boolean canOnlySelectNewTraveler() {
		//Is the user logged in and has associated travelers?
		if (BookingInfoUtils.getAlternativeTravelers(this).size() > 0) {
			return false;
		}

		//Does the current traveler have a name entered?
		Traveler currentTraveler = Db.getTravelers().get(mTravelerIndex);
		if (currentTraveler.hasName()) {
			return false;
		}

		return true;
	}

	private void closeSaveDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment dialog = getSupportFragmentManager().findFragmentByTag(SAVE_FRAGMENT_TAG);
		if (dialog != null) {
			ft.remove(dialog);
		}
		ft.commit();
	}

	private void displaySavingDialog() {
		mPos = YoYoPosition.SAVING;
		ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.saving_traveler));
		df.setCancelable(false);
		df.show(this.getSupportFragmentManager(), DIALOG_SAVING_TRAVELER);
	}

	private void displayOverwriteDialog() {
		mPos = YoYoPosition.OVERWRITE_TRAVELER;
		OverwriteExistingTravelerDialogFragment df = OverwriteExistingTravelerDialogFragment.newInstance();
		df.show(this.getSupportFragmentManager(), OverwriteExistingTravelerDialogFragment.TAG);
	}

	private void closeOverwriteDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment dialog = getSupportFragmentManager().findFragmentByTag(OverwriteExistingTravelerDialogFragment.TAG);
		if (dialog != null) {
			ft.remove(dialog);
		}
		ft.commit();
	}

	private void showInvalidBirthdateDialog() {
		BirthDateInvalidDialog dialog = BirthDateInvalidDialog.newInstance(true);
		dialog.show(getSupportFragmentManager(), FTAG_INVALID_BIRTHDATE_DIALOG);
	}

	@Subscribe
	public void onInvalidBirthdateEdit(Events.BirthDateInvalidEditSearch event) {
		Ui.hideKeyboard(this);
		NavUtils.goToFlights(this, true, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}


	/////////////////////////////////////////////
	// IDialogMoveForwardBackwardListener - wire up the save and overwrite dialogs

	@Override
	public void onDialogMoveForward() {
		moveForward();
	}

	@Override
	public void onDialogMoveBackwards() {
		moveBackwards();
	}
}
