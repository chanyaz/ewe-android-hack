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
import android.view.inputmethod.InputMethodManager;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.FlightPaymentAddressFragment;
import com.expedia.bookings.fragment.FlightPaymentCreditCardFragment;
import com.expedia.bookings.fragment.FlightPaymentOptionsFragment;
import com.expedia.bookings.fragment.FlightPaymentOptionsFragment.FlightPaymentYoYoListener;
import com.expedia.bookings.fragment.FlightPaymentSaveDialogFragment;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

public class FlightPaymentOptionsActivity extends FragmentActivity implements FlightPaymentYoYoListener {

	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ADDRESS_FRAGMENT_TAG = "ADDRESS_FRAGMENT_TAG";
	public static final String CREDIT_CARD_FRAGMENT_TAG = "CREDIT_CARD_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	public static final String INTENT_TAG_MODE = "INTENT_TAG_MODE";
	public static final String INTENT_TAG_DEST = "INTENT_TAG_DEST";

	private FlightPaymentOptionsFragment mOptionsFragment;
	private FlightPaymentAddressFragment mAddressFragment;
	private FlightPaymentCreditCardFragment mCCFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;
	private YoYoPosition mBeforeSaveDialogPos;

	private boolean mIsBailing = false;

	//Define the states of navigation
	public enum YoYoMode {
		NONE,
		YOYO,
		EDIT
	}

	//Where we want to return to after our action
	public enum YoYoPosition {
		OPTIONS,
		ADDRESS,
		CREDITCARD,
		SAVE
	}

	public interface Validatable {
		boolean attemptToLeave();
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

		// Recover data if it was flushed from memory
		if (Db.getTripBucket().isEmpty()) {
			if (!Db.loadTripBucket(this)) {
				NavUtils.onDataMissing(this);
			}
		}

		boolean hasPositionData = false;

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
			hasPositionData = true;
		}

		if (getIntent() != null && getIntent().hasExtra(INTENT_TAG_DEST)) {
			mPos = YoYoPosition.valueOf(getIntent().getStringExtra(INTENT_TAG_DEST));
			getIntent().removeExtra(INTENT_TAG_DEST);
			if (getIntent().hasExtra(INTENT_TAG_MODE)) {
				mMode = YoYoMode.valueOf(INTENT_TAG_MODE);
				getIntent().removeExtra(INTENT_TAG_MODE);
			}
			else {
				mMode = YoYoMode.EDIT;
			}
			hasPositionData = true;
		}

		if (hasPositionData) {
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
			if (canOnlySelectNewCard()) {
				mMode = YoYoMode.YOYO;
				if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
					displayAddress();
				}
				else {
					displayCreditCard();
				}
			}
			else {
				displayOptions();
			}
		}

		//Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mMode = YoYoMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		mPos = YoYoPosition.valueOf(savedInstanceState.getString(STATE_TAG_DEST));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();

		//If the save dialog is showing, we close it, and then we show it again from the onCreate method.
		if (mPos.equals(YoYoPosition.SAVE)) {
			this.closeSaveDialog();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_TAG_MODE, mMode.name());
		outState.putString(STATE_TAG_DEST, mPos.name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (!moveBackwards()) {
			super.onBackPressed();
		}
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

	private void displayActionItemBasedOnState() {
		if (mMode == null) {
			return;
		}
		else if (mPos != null && mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				setShowNextButton(false);
				setShowDoneButton(false);
				break;
			case ADDRESS:
				setShowNextButton(true);
				setShowDoneButton(false);
				break;
			case CREDITCARD:
				setShowNextButton(false);
				setShowDoneButton(true);
				break;
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
			if (mPos.compareTo(YoYoPosition.OPTIONS) == 0) {
				setShowNextButton(false);
				setShowDoneButton(false);
			}
			else {
				setShowNextButton(false);
				setShowDoneButton(true);
			}
		}
	}

	private void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		String titleStr = getString(R.string.checkout_enter_payment_details);
		if (mPos != null) {
			switch (mPos) {
			case ADDRESS:
				titleStr = getString(R.string.billing_address);
				actionBar.setTitle(titleStr);
				break;
			case CREDITCARD:
				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
				actionBar.setCustomView(R.layout.action_bar_card_info);
				break;
			case SAVE:
			case OPTIONS:
			default:
				titleStr = getString(R.string.checkout_enter_payment_details);
				actionBar.setTitle(titleStr);
			}
		}
		else {
			actionBar.setTitle(titleStr);
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// YoYo listener

	@Override
	public void moveForward() {
		if (mMode.equals(YoYoMode.YOYO)) {
			switch (mPos) {
			case OPTIONS:
				if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
					displayAddress();
				}
				else {
					displayCreditCard();
				}
				break;
			case ADDRESS:
				if (validate(mAddressFragment)) {
					displayCreditCard();
				}
				break;
			case CREDITCARD:
				if (validate(mCCFragment)) {
					Db.getTripBucket().getFlight().getFlightTrip().setShowFareWithCardFee(true);
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
				OmnitureTracking.trackPageLoadFlightCheckoutPaymentEditSave();
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
					if (User.isLoggedIn(this)
						&& !Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getSaveCardToExpediaAccount()
						&& workingBillingInfoChanged()) {
						displaySaveDialog();
					}
					else {
						Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
							Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
						displayOptions();
					}
				}
				break;
			case CREDITCARD:
				if (validate(mCCFragment)) {
					Db.getTripBucket().getFlight().getFlightTrip().setShowFareWithCardFee(true);
					if (User.isLoggedIn(this)
						&& !Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getSaveCardToExpediaAccount()
						&& workingBillingInfoChanged()) {
						displaySaveDialog();
					}
					else {
						Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
							Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
						displayOptions();
					}
				}
				break;
			case SAVE:
				Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
					Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
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

	// moveForward helper methods

	private boolean validate(Validatable validatable) {
		if (validatable == null) {
			return false;
		}
		else {
			return validatable.attemptToLeave();
		}
	}

	private boolean workingBillingInfoChanged() {
		if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
			return Db.getWorkingBillingInfoManager().getWorkingBillingInfo()
				.compareTo(Db.getWorkingBillingInfoManager().getBaseBillingInfo()) != 0;
		}
		return false;
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
			case ADDRESS:
				//If we are backing up we want to restore the base billing info...
				if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
					Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
						Db.getWorkingBillingInfoManager().getBaseBillingInfo());
				}
				displayOptions();
				break;
			case CREDITCARD:
				if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
					displayAddress();
				}
				else {
					displayOptions();
				}

				break;
			case SAVE:
				closeSaveDialog();
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
			case CREDITCARD:
				//If we are backing up we want to restore the base billing info...
				if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
					Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
						Db.getWorkingBillingInfoManager().getBaseBillingInfo());
				}
				displayOptions();
				break;
			case SAVE:
				//Back on save means cancel
				if (mBeforeSaveDialogPos != null) {
					switch (mBeforeSaveDialogPos) {
					case ADDRESS:
						displayAddress();
						break;
					case CREDITCARD:
						displayCreditCard();
						break;
					default:
						displayOptions();
					}
				}
				else {
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
			mOptionsFragment = FlightPaymentOptionsFragment.newInstance();
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
	public void displayAddress() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mAddressFragment = Ui.findSupportFragment(this, ADDRESS_FRAGMENT_TAG);
		if (mAddressFragment == null) {
			mAddressFragment = FlightPaymentAddressFragment.newInstance();
		}
		if (!mAddressFragment.isAdded()) {
			ft.replace(android.R.id.content, mAddressFragment, ADDRESS_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.ADDRESS;
		supportInvalidateOptionsMenu();
	}

	@Override
	public void displayCreditCard() {
		mPos = YoYoPosition.CREDITCARD;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mCCFragment = Ui.findSupportFragment(this, CREDIT_CARD_FRAGMENT_TAG);
		if (mCCFragment == null) {
			mCCFragment = FlightPaymentCreditCardFragment.newInstance();
		}
		if (!mCCFragment.isAdded()) {
			ft.replace(android.R.id.content, mCCFragment, CREDIT_CARD_FRAGMENT_TAG);
			ft.commit();
		}
		supportInvalidateOptionsMenu();
	}

	@Override
	public void displaySaveDialog() {
		mBeforeSaveDialogPos = mPos;
		mPos = YoYoPosition.SAVE;
		supportInvalidateOptionsMenu();
		DialogFragment newFragment = FlightPaymentSaveDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), SAVE_FRAGMENT_TAG);

	}

	@Override
	public void displayCheckout() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		Db.getWorkingBillingInfoManager().clearWorkingBillingInfo();

		Intent gotoCheckoutOverviewIntent = new Intent(FlightPaymentOptionsActivity.this,
			FlightTripOverviewActivity.class);
		gotoCheckoutOverviewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(gotoCheckoutOverviewIntent);
	}

	// Private helper methods

	private boolean canOnlySelectNewCard() {
		// Does the user have cards they could select?
		if (BookingInfoUtils.getStoredCreditCards(this).size() > 0) {
			return false;
		}

		//Has the user manually entered data already?
		FlightPaymentFlowState validationState = FlightPaymentFlowState.getInstance(this);
		boolean addressValid = validationState.hasValidBillingAddress(Db.getWorkingBillingInfoManager()
			.getWorkingBillingInfo());
		boolean cardValid = validationState
			.hasValidCardInfo(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		if (addressValid || cardValid) {
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

}
