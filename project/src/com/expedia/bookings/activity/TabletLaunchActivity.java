package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.TabletLaunchControllerFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
	IBackManageable, IMeasurementProvider, FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final int REQUEST_SETTINGS = 1234;

	private static final String FTAG_CONTROLLER_FRAGMENT = "CONTROLLER_FRAGMENT";

	// Containers
	private ViewGroup mRootC;

	// Fragments
	TabletLaunchControllerFragment mControllerFragment;

	// HockeyApp
	private HockeyPuck mHockeyPuck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		mRootC = Ui.findView(this, R.id.root_layout);

		android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mControllerFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_CONTROLLER_FRAGMENT, manager, transaction, this,
			R.id.root_layout, false);

		transaction.commit();
		manager.executePendingTransactions();//These must be finished before we continue..

		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
		OmnitureTracking.trackPageLoadLaunchScreen(this);
		LaunchDb.getCollections(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Events.register(this);

		GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(this);
		gpsd.startChecking();

		mHockeyPuck.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
		Events.unregister(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mHockeyPuck.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SETTINGS && resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			// TODO reset the state of the SuggestionFragments such that it redraws again, and won't show the recents
		}
	}

	@Override
	public void onBackPressed() {
		if (!mBackManager.doOnBackPressed()) {
			super.onBackPressed();
		}
	}

	/*
	 * Action bar
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// We only want to show the menu items in the default launch state (not details or waypoint)
		if (mControllerFragment.shouldDisplayMenu()) {
			getMenuInflater().inflate(R.menu.menu_launch_tablet, menu);
			getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);
			if (!AndroidUtils.isRelease(this)) {
				DebugMenu.onCreateOptionsMenu(this, menu);
				mHockeyPuck.onCreateOptionsMenu(menu);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mControllerFragment.shouldDisplayMenu()) {
			if (!AndroidUtils.isRelease(this)) {
				DebugMenu.onPrepareOptionsMenu(this, menu);
				mHockeyPuck.onPrepareOptionsMenu(menu);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			mBackManager.doOnBackPressed();
			return true;
		}
		case R.id.menu_your_trips: {
			startActivity(ItineraryActivity.createIntent(this));
			return true;
		}
		case R.id.menu_settings: {
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		}
		case R.id.menu_about: {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
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
	 * FragmentAvailabilityUtils.IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_CONTROLLER_FRAGMENT) {
			frag = mControllerFragment;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_CONTROLLER_FRAGMENT) {
			frag = new TabletLaunchControllerFragment();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		// Nothing to do here
	}

	/*
	 * MeasureableFragmentListener
	 */

	@Override
	public void canMeasure(Fragment fragment) {
		if (mControllerFragment != null && mControllerFragment.isMeasurable()) {
			updateContentSize(mRootC.getWidth(), mRootC.getHeight());
		}
	}

	/*
	 * IBackManageable
	 */

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

	/*
	 * IMeasurementProvider
	 */

	private int mLastReportedWidth = -1;
	private int mLastReportedHeight = -1;
	private ArrayList<IMeasurementListener> mMeasurementListeners = new ArrayList<>();

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {

		if (totalWidth != mLastReportedWidth || totalHeight != mLastReportedHeight) {
			boolean isLandscape = totalWidth > totalHeight;

			mLastReportedWidth = totalWidth;
			mLastReportedHeight = totalHeight;

			for (IMeasurementListener listener : mMeasurementListeners) {
				listener.onContentSizeUpdated(totalWidth, totalHeight, isLandscape);
			}
		}
	}

	@Override
	public void registerMeasurementListener(IMeasurementListener listener, boolean fireListener) {
		mMeasurementListeners.add(listener);
		if (fireListener && mLastReportedWidth >= 0 && mLastReportedHeight >= 0) {
			listener.onContentSizeUpdated(mLastReportedWidth, mLastReportedHeight,
				mLastReportedWidth > mLastReportedHeight);
		}
	}

	@Override
	public void unRegisterMeasurementListener(IMeasurementListener listener) {
		mMeasurementListeners.remove(listener);
	}

}
