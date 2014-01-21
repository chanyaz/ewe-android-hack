package com.expedia.bookings.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.TabletCheckoutControllerFragment;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.utils.DebugMenu;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

/**
 * TabletCheckoutActivity: The checkout activity designed for tablet 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletCheckoutActivity extends SherlockFragmentActivity implements IBackButtonLockListener,
		IBackManageable, CheckoutInformationListener {

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, TabletCheckoutActivity.class);
		intent.putExtra(ARG_LOB, lob.name());
		return intent;
	}

	//Args
	private static final String ARG_LOB = "ARG_LOB";

	//State
	private static final String STATE_DEBUG_DATA_LOADED = "STATE_DEBUG_DATA_LOADING";

	//Containers..
	private ViewGroup mRootC;

	//Fragments
	TabletCheckoutControllerFragment mFragCheckoutController;

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

		loadCachedData(true);

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);

		//Fragments
		FragmentManager manager = getSupportFragmentManager();
		mFragCheckoutController = (TabletCheckoutControllerFragment) manager
				.findFragmentById(R.id.tablet_checkout_controller_fragment);

		//Args
		if (getIntent().hasExtra(ARG_LOB)) {
			try {
				LineOfBusiness lob = LineOfBusiness.valueOf(getIntent().getStringExtra(ARG_LOB));
				mFragCheckoutController.setCheckoutMode(lob);
			}
			catch (Exception ex) {
				Log.e("Exception parsing lob from intent.", ex);
			}

		}

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

	/*
	 * CHECKOUT INFORMATION LISTENER
	 */

	@Override
	public void checkoutInformationIsValid() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkoutInformationIsNotValid() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBillingInfoChange() {
		// TODO Auto-generated method stub

	}

	/*
	 * CACHED DATA LOADING...
	 */
	private boolean mLoadedDbInfo = false;

	private void loadCachedData(boolean wait) {
		if (!mLoadedDbInfo) {
			CheckoutDataLoader.CheckoutDataLoadedListener listener = new CheckoutDataLoader.CheckoutDataLoadedListener() {
				@Override
				public void onCheckoutDataLoaded(boolean wasSuccessful) {
					mLoadedDbInfo = wasSuccessful;
				}
			};
			CheckoutDataLoader.getInstance().loadCheckoutData(this, true, true, listener, wait);
		}
	}

}
