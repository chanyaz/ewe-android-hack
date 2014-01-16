package com.expedia.bookings.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.utils.DebugMenu;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

/**
 * TabletCheckoutActivity: The checkout activity designed for tablet 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletCheckoutActivity extends SherlockFragmentActivity implements IBackButtonLockListener,
		IBackManageable {

	//State
	private static final String STATE_DEBUG_DATA_LOADED = "STATE_DEBUG_DATA_LOADING";

	//Containers..
	private ViewGroup mRootC;

	//Other
	private boolean mBackButtonLocked = false;
	private boolean mTestDataLoaded = false;
	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_checkout);

		//TODO: REMOVE TESTING DATA
		if (savedInstanceState == null || !savedInstanceState.getBoolean(STATE_DEBUG_DATA_LOADED, false)) {
			Db.saveOrLoadDbForTesting(this);
			mTestDataLoaded = true;
		}
		else {
			mTestDataLoaded = true;
		}

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_DEBUG_DATA_LOADED, mTestDataLoaded);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mHockeyPuck.onResume();
	}

	/*
	 * MENU STUFF
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retVal = super.onCreateOptionsMenu(menu);

		DebugMenu.onCreateOptionsMenu(this, menu);

		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onCreateOptionsMenu(menu);
		}

		return retVal;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onPrepareOptionsMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		if (!AndroidUtils.isRelease(this) && mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public void onBackPressed() {
		if (!mBackButtonLocked) {
			if (!mBackManager.doOnBackPressed()) {
				super.onBackPressed();
			}
		}
	}

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			//Our children may do something on back pressed, but if we are left in charge we do nothing
			return false;
		}

	};

	@Override
	public void setBackButtonLockState(boolean locked) {
		mBackButtonLocked = locked;
	}
}
