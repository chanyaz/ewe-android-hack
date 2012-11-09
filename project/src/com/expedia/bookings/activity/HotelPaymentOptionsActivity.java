package com.expedia.bookings.activity;

import android.content.Context;
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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.HotelPaymentAddressFragment;
import com.expedia.bookings.fragment.HotelPaymentCreditCardFragment;
import com.expedia.bookings.fragment.HotelPaymentOptionsFragment;
import com.expedia.bookings.fragment.HotelPaymentOptionsFragment.HotelPaymentYoYoListener;
import com.expedia.bookings.fragment.HotelPaymentSaveDialogFragment;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentOptionsActivity extends SherlockFragmentActivity implements HotelPaymentYoYoListener {
	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ADDRESS_FRAGMENT_TAG = "ADDRESS_FRAGMENT_TAG";
	public static final String CREDIT_CARD_FRAGMENT_TAG = "CREDIT_CARD_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private HotelPaymentOptionsFragment mOptionsFragment;
	private HotelPaymentAddressFragment mAddressFragment;
	private HotelPaymentCreditCardFragment mCCFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;
	private YoYoPosition mBeforeSaveDialogPos;

	//Define the states of navigation
	public enum YoYoMode {
		NONE, YOYO, EDIT
	}

	//Where we want to return to after our action
	public enum YoYoPosition {
		OPTIONS, ADDRESS, CREDITCARD, SAVE
	}

	public interface Validatable {
		public boolean attemptToLeave();
	}

	private void hideKeyboard() {
		if (this.getCurrentFocus() != null) {
			//Oh silly stupid InputMethodManager...
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void displayOptions() {
		hideKeyboard();

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

	public void displayAddress() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mAddressFragment = Ui.findSupportFragment(this, ADDRESS_FRAGMENT_TAG);
		if (mAddressFragment == null) {
			mAddressFragment = HotelPaymentAddressFragment.newInstance();
		}
		if (!mAddressFragment.isAdded()) {
			ft.replace(android.R.id.content, mAddressFragment, ADDRESS_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.ADDRESS;
		supportInvalidateOptionsMenu();
	}

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

	public void displaySaveDialog() {
		mBeforeSaveDialogPos = mPos;
		mPos = YoYoPosition.SAVE;
		supportInvalidateOptionsMenu();
		DialogFragment newFragment = HotelPaymentSaveDialogFragment.newInstance();
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

	public void displayCheckout() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		Db.getWorkingBillingInfoManager().clearWorkingBillingInfo(this);
		finish();
	}

	private boolean workingBillingInfoChanged() {
		if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
			return Db.getWorkingBillingInfoManager().getWorkingBillingInfo()
					.compareTo(Db.getWorkingBillingInfoManager().getBaseBillingInfo()) != 0;
		}
		return false;
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
				OmnitureTracking.trackPageLoadHotelsCheckoutPaymentEditSave(getApplicationContext());
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
				//If we are backing up we want to restore the base billing info...
				if (Db.getWorkingBillingInfoManager().getBaseBillingInfo() != null) {
					Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(
							Db.getWorkingBillingInfoManager().getBaseBillingInfo());
				}
				displayOptions();
				break;
			case CREDITCARD:
				displayAddress();
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

		//If we have a working BillingInfo object that was cached we try to load it from disk
		WorkingBillingInfoManager billMan = Db.getWorkingBillingInfoManager();
		if (billMan.getAttemptToLoadFromDisk() && billMan.hasBillingInfoOnDisk(this)) {
			//Load working billing info from disk
			billMan.loadWorkingBillingInfoFromDisk(this);
			if (mPos.compareTo(YoYoPosition.OPTIONS) == 0) {
				//If we don't have a saved state, but we do have a saved temp billingInfo go ahead to the entry screens
				mPos = YoYoPosition.ADDRESS;
				mMode = YoYoMode.YOYO;
			}
		}
		else {
			//If we don't load from disk, then we delete the file
			billMan.deleteWorkingBillingInfoFile(this);
		}

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

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
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

	public void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		String titleStr = getString(R.string.payment_method);
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
				titleStr = getString(R.string.payment_method);
				actionBar.setTitle(titleStr);
			}
		}
		else {
			actionBar.setTitle(titleStr);
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