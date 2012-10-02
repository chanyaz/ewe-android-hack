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
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.HotelTravelerInfoOneFragment;
import com.expedia.bookings.fragment.HotelTravelerInfoOptionsFragment;
import com.expedia.bookings.fragment.HotelTravelerInfoOptionsFragment.TravelerInfoYoYoListener;
import com.expedia.bookings.fragment.HotelTravelerInfoTwoFragment;
import com.expedia.bookings.fragment.HotelTravelerSaveDialogFragment;
import com.expedia.bookings.utils.Ui;

public class HotelTravelerInfoOptionsActivity extends SherlockFragmentActivity implements TravelerInfoYoYoListener {
	public static final String OPTIONS_FRAGMENT_TAG = "OPTIONS_FRAGMENT_TAG";
	public static final String ONE_FRAGMENT_TAG = "ONE_FRAGMENT_TAG";
	public static final String TWO_FRAGMENT_TAG = "TWO_FRAGMENT_TAG";
	public static final String SAVE_FRAGMENT_TAG = "SAVE_FRAGMENT_TAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_DEST = "STATE_TAG_DEST";

	private HotelTravelerInfoOptionsFragment mOptionsFragment;
	private HotelTravelerInfoOneFragment mOneFragment;
	private HotelTravelerInfoTwoFragment mTwoFragment;

	private MenuItem mMenuDone;
	private MenuItem mMenuNext;

	private YoYoMode mMode = YoYoMode.NONE;
	private YoYoPosition mPos = YoYoPosition.OPTIONS;

	//Where we want to return to after our action
	private enum YoYoPosition {
		OPTIONS, ONE, TWO, SAVE
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
		displayActionBarTitleBasedOnState();
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

	public void displayActionBarTitleBasedOnState() {
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		String titleStr = getString(R.string.traveler_information);
		if (mPos != null) {
			switch (mPos) {
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
			case SAVE:
				closeSaveDialog();
				displayTravelerEntryTwo();
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
			mOptionsFragment = HotelTravelerInfoOptionsFragment.newInstance();
		}
		if (!mOptionsFragment.isAdded()) {
			ft.replace(android.R.id.content, mOptionsFragment, OPTIONS_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.OPTIONS;
		mMode = YoYoMode.NONE;
		displayActionBarTitleBasedOnState();
		displayActionItemBasedOnState();
	}

	@Override
	public void displayTravelerEntryOne() {
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
		displayActionBarTitleBasedOnState();
		displayActionItemBasedOnState();

	}

	@Override
	public void displayTravelerEntryTwo() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mTwoFragment = Ui.findSupportFragment(this, TWO_FRAGMENT_TAG);
		if (mTwoFragment == null) {
			mTwoFragment = HotelTravelerInfoTwoFragment.newInstance();
		}
		if (!mTwoFragment.isAdded()) {
			ft.replace(android.R.id.content, mTwoFragment, TWO_FRAGMENT_TAG);
			ft.commit();
		}
		mPos = YoYoPosition.TWO;
		displayActionBarTitleBasedOnState();
		displayActionItemBasedOnState();

	}

	@Override
	public void displaySaveDialog() {
		mPos = YoYoPosition.SAVE;
		displayActionItemBasedOnState();
		DialogFragment newFragment = HotelTravelerSaveDialogFragment.newInstance();
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
