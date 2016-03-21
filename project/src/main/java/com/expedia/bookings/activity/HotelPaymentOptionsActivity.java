package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
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
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.fragment.HotelPaymentCreditCardFragment;
import com.expedia.bookings.fragment.HotelPaymentOptionsFragment;
import com.expedia.bookings.fragment.HotelPaymentOptionsFragment.HotelPaymentYoYoListener;
import com.expedia.bookings.fragment.HotelPaymentSaveDialogFragment;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentOptionsActivity extends FragmentActivity implements HotelPaymentYoYoListener {

	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String CREDIT_CARD_FRAGMENT_TAG = "CREDIT_CARD_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	private static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	private static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private HotelPaymentOptionsFragment mOptionsFragment;
	private HotelPaymentCreditCardFragment mCCFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	boolean isUserBucketedForTest;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;
	private YoYoPosition mBeforeSaveDialogPos;

	//Define the states of navigation
	public enum YoYoMode {
		NONE,
		YOYO,
		EDIT
	}

	//Where we want to return to after our action
	public enum YoYoPosition {
		OPTIONS,
		CREDITCARD,
		SAVE
	}

	public interface Validatable {
		boolean attemptToLeave();
	}

	public static Intent gotoOptionsIntent(Context context) {
		Intent intent = new Intent(context, HotelPaymentOptionsActivity.class);
		intent.putExtra(STATE_TAG_MODE, YoYoMode.YOYO.name());
		intent.putExtra(STATE_TAG_DEST, YoYoPosition.OPTIONS.name());
		return intent;
	}

	public static Intent gotoCreditCardEntryIntent(Context context) {
		Intent intent = new Intent(context, HotelPaymentOptionsActivity.class);
		intent.putExtra(STATE_TAG_MODE, YoYoMode.YOYO.name());
		intent.putExtra(STATE_TAG_DEST, YoYoPosition.CREDITCARD.name());
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isUserBucketedForTest = Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHCKOTraveler);

		Bundle bundle = savedInstanceState;
		if (savedInstanceState == null) {
			bundle = getIntent().getExtras();
		}

		//Show the options fragment
		if (bundle != null && bundle.containsKey(STATE_TAG_DEST)) {
			mMode = YoYoMode.valueOf(bundle.getString(STATE_TAG_MODE));
			mPos = YoYoPosition.valueOf(bundle.getString(STATE_TAG_DEST));
			switch (mPos) {
			case OPTIONS:
				displayOptions();
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
				displayCreditCard();
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
				displayCreditCard();
				break;
			case CREDITCARD:
				if (validate(mCCFragment)) {
					if (User.isLoggedIn(this)) {
						displaySaveDialog();
					}
					else {
						setIntentResultOk();
						displayCheckout();
					}
				}
				break;
			case SAVE:
				setIntentResultOk();
				displayCheckout();
				OmnitureTracking.trackPageLoadHotelsCheckoutPaymentEditSave();
				break;
			default:
				Ui.showToast(this, "FAIL");
				break;
			}
		}
		else if (mMode.equals(YoYoMode.EDIT)) {
			switch (mPos) {
			case CREDITCARD:
				if (validate(mCCFragment)) {
					if (User.isLoggedIn(this)
						&& !Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getSaveCardToExpediaAccount()
						&& workingBillingInfoChanged()) {
						displaySaveDialog();
					}
					else {
						Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
							Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
						if (isUserBucketedForTest) {
							setIntentResultOk();
							displayCheckout();
						}
						else {
							displayOptions();
						}
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
			case CREDITCARD:
				//If we are backing up we want to restore the base billing info...
				if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
					Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
						Db.getWorkingBillingInfoManager().getBaseBillingInfo());
				}
				//show options only if user is logged in
				if (!User.isLoggedIn(this)) {
					displayCheckout();
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
			if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard() != null) {
				setIntentResultOk();
			}
			displayCheckout();
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

	@Override
	public void displayOptions() {
		Ui.hideKeyboard(this, InputMethodManager.HIDE_NOT_ALWAYS);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mOptionsFragment = Ui.findSupportFragment(this, OPTIONS_FRAGMENT_TAG);
		if (mOptionsFragment == null) {
			mOptionsFragment = HotelPaymentOptionsFragment.newInstance();
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
	public void displayCreditCard() {
		mPos = YoYoPosition.CREDITCARD;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mCCFragment = Ui.findSupportFragment(this, CREDIT_CARD_FRAGMENT_TAG);
		if (mCCFragment == null) {
			mCCFragment = HotelPaymentCreditCardFragment.newInstance();
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
		DialogFragment newFragment = HotelPaymentSaveDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), SAVE_FRAGMENT_TAG);
	}

	@Override
	public void displayCheckout() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		Db.getWorkingBillingInfoManager().clearWorkingBillingInfo();
		finish();
	}

	public void setIntentResultOk() {
		if (isUserBucketedForTest) {
			setResult(RESULT_OK);
		}
	}

	// Private helper methods

	private boolean canOnlySelectNewCard() {
		// Does the user have cards they could select?
		if (BookingInfoUtils.getStoredCreditCards(this).size() > 0) {
			return false;
		}

		// Has the user manually entered data already?
		HotelPaymentFlowState validationState = HotelPaymentFlowState.getInstance(this);
		BillingInfo billingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
		boolean addressValid = validationState.hasValidBillingAddress(billingInfo);
		boolean cardValid = validationState.hasValidCardInfo(billingInfo);
		if (addressValid && cardValid) {
			return false;
		}

		return true;
	}

}
