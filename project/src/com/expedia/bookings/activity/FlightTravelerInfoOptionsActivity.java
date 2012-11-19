package com.expedia.bookings.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
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
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.model.WorkingTravelerManager.ITravelerUpdateListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightTravelerInfoOptionsActivity extends SherlockFragmentActivity implements TravelerInfoYoYoListener {
	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";
	public static final String TWO_FRAGMENT_TAG = "TWO_FRAGMENT_TAG";
	public static final String THREE_FRAGMENT_TAG = "THREE_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";
	private static final String STATE_TAG_START_FIRST_NAME = "STATE_TAG_START_FIRST_NAME";
	private static final String STATE_TAG_START_LAST_NAME = "STATE_TAG_START_LAST_NAME";

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

	//for determining if the name changed...
	private String mStartFirstName = "";
	private String mStartLastName = "";

	//Where we want to return to after our action
	private enum YoYoPosition {
		OPTIONS, ONE, TWO, THREE, SAVE, SAVING, OVERWRITE_TRAVELER
	}

	public interface Validatable {
		public boolean validate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_yoyo, menu);
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
			case OVERWRITE_TRAVELER:
			case SAVING:
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
		outState.putString(STATE_TAG_START_FIRST_NAME, mStartFirstName);
		outState.putString(STATE_TAG_START_LAST_NAME, mStartLastName);
		super.onSaveInstanceState(outState);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
			}
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
			}
			else {
				mPos = YoYoPosition.OPTIONS;
			}
		}

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

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	public boolean canOnlySelectNewTraveler() {

		//Is the user logged in and has associated travelers?
		if (User.isLoggedIn(this) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null
				&& Db.getUser().getAssociatedTravelers().size() > 0) {
			return false;
		}

		//Does the current traveler have a name entered?
		Traveler currentTraveler = Db.getTravelers().get(mTravelerIndex);
		if (currentTraveler.hasName()) {
			return false;
		}

		return true;
	}

	@Override
	public void onPause() {
		super.onPause();

		//If the save dialog is showing, we close it, and then we show it again from the onCreate method.
		if (mPos.equals(YoYoPosition.SAVE)) {
			this.closeSaveDialog();
		}

		//If the overwrite dialog is showing, we close it
		if (mPos.equals(YoYoPosition.OVERWRITE_TRAVELER)) {
			this.closeOverwriteDialog();
		}
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
			if (mStartFirstName.trim().compareTo(working.getFirstName().trim()) == 0
					&& mStartLastName.trim().compareTo(working.getLastName().trim()) == 0) {
				return false;
			}
			else {
				return true;
			}
		}
		return false;
	}

	private boolean workingTravelerDiffersFromBase() {
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
				if (workingTravelerRequiresOverwritePrompt()) {
					//The user already has a traveler named this...
					displayOverwriteDialog();
				}
				else {
					OmnitureTracking.trackPageLoadFlightTravelerEditSave(mContext);
					displayCheckout();
				}
				break;
			case OVERWRITE_TRAVELER:
				OmnitureTracking.trackPageLoadFlightTravelerEditSave(mContext);
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

	public boolean workingTravelerRequiresOverwritePrompt() {
		boolean travelerAlreadyExistsOnAccount = false;
		Traveler workingTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		if (workingTraveler.getSaveTravelerToExpediaAccount() && User.isLoggedIn(mContext)
				&& !workingTraveler.hasTuid()) {
			//If we want to save, and we're logged in, and we have a new traveler
			//We have to check if that travelers name matches an existing traveler
			if (Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null
					&& Db.getUser().getAssociatedTravelers().size() > 0) {
				for (Traveler trav : Db.getUser().getAssociatedTravelers()) {
					if (workingTraveler.compareNameTo(trav) == 0) {
						//A traveler with this name already exists on the account. Foo. ok so lets show a dialog and be all like "Hey yall, you wanna overwrite your buddy dave bob?"
						travelerAlreadyExistsOnAccount = true;
						break;
					}
				}
			}
		}
		return travelerAlreadyExistsOnAccount;
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
		hideKeyboard();

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

	private void displaySavingDialog() {
		mPos = YoYoPosition.SAVING;
		SavingTravelerDialogFragment df = new SavingTravelerDialogFragment();
		df.show(this.getSupportFragmentManager(), SavingTravelerDialogFragment.TAG);
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

	@Override
	public void displayCheckout() {
		//First we commit our traveler stuff...
		Traveler trav = Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerIndex, this);
		Db.getWorkingTravelerManager().clearWorkingTraveler(this);
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
						SavingTravelerDialogFragment df = Ui.findSupportFragment(
								FlightTravelerInfoOptionsActivity.this,
								SavingTravelerDialogFragment.TAG);
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

	private void hideKeyboard() {
		if (this.getCurrentFocus() != null) {
			//Oh silly stupid InputMethodManager...
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public static class SavingTravelerDialogFragment extends DialogFragment {

		public static final String TAG = SavingTravelerDialogFragment.class.getName();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog pd = new ProgressDialog(getActivity());
			pd.setMessage(getString(R.string.saving_traveler));
			pd.setCanceledOnTouchOutside(false);
			return pd;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			// If the dialog is canceled without finishing loading, don't show this page.
			getActivity().finish();
		}
	}

	public static class OverwriteExistingTravelerDialogFragment extends DialogFragment {

		public static final String TAG = OverwriteExistingTravelerDialogFragment.class.getName();

		TravelerInfoYoYoListener mListener;

		public static OverwriteExistingTravelerDialogFragment newInstance() {
			OverwriteExistingTravelerDialogFragment frag = new OverwriteExistingTravelerDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String workingTravelerName = Db.getWorkingTravelerManager().getWorkingTraveler().getFirstName() + " "
					+ Db.getWorkingTravelerManager().getWorkingTraveler().getLastName();

			AlertDialog pd = new AlertDialog.Builder(getActivity())
					.setCancelable(false)
					.setTitle(R.string.cant_save_traveler)
					.setMessage(
							String.format(getString(R.string.you_already_have_traveler_TEMPLATE), workingTravelerName))
					.setPositiveButton(R.string.overwrite, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//We want to overwrite, so we go through, find the traveler with the same name and steal his/her tuid
							if (User.isLoggedIn(getActivity()) && Db.getUser() != null
									&& Db.getUser().getAssociatedTravelers() != null) {
								for (Traveler trav : Db.getUser().getAssociatedTravelers()) {
									if (Db.getWorkingTravelerManager().getWorkingTraveler().compareNameTo(trav) == 0) {
										//We find the traveler with the same name, and steal his tuid
										Db.getWorkingTravelerManager().getWorkingTraveler().setTuid(trav.getTuid());
									}
								}
							}
							mListener.moveForward();
						}

					})
					.setNegativeButton(R.string.dont_save, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(false);
							mListener.moveForward();
						}
					}).create();
			pd.setCanceledOnTouchOutside(false);
			return pd;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			if (!(activity instanceof TravelerInfoYoYoListener)) {
				throw new RuntimeException(
						"OverwriteExistingTravelerDialogFragment activity must implement TravelerInfoYoYoListener!");
			}

			mListener = (TravelerInfoYoYoListener) activity;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			if (mListener != null) {
				mListener.moveBackwards();
			}
		}
	}
}
