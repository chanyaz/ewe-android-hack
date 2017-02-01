package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.launch.fragment.TabletLaunchControllerFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.FacebookEvents;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.DebugMenuFactory;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
	IBackManageable, IMeasurementProvider, FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String FTAG_CONTROLLER_FRAGMENT = "CONTROLLER_FRAGMENT";

	// Containers
	private ViewGroup mRootC;

	// Fragments
	TabletLaunchControllerFragment mControllerFragment;

	// when the system permission dialog is displayed, our activity is paused so we have to grab the response
	// and post it after we resume and our fragments are available for events
	private Events.PermissionEvent delayedPermissionEvent;
	private boolean busRegistered;

	private DebugMenu debugMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);
		AdTracker.trackLaunch();
		debugMenu = DebugMenuFactory.newInstance(this, TabletPreferenceActivity.class);

		mRootC = Ui.findView(this, R.id.root_layout);

		android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mControllerFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_CONTROLLER_FRAGMENT, manager, transaction, this,
			R.id.root_layout, false);

		transaction.commit();
		manager.executePendingTransactions();//These must be finished before we continue..
		Intent intent = getIntent();
		LineOfBusiness lineOfBusiness = (LineOfBusiness) intent.getSerializableExtra(Codes.LOB_NOT_SUPPORTED);
		if (lineOfBusiness != null) {
			CharSequence errorMessage = null;
			if (lineOfBusiness == LineOfBusiness.CARS) {
				errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message)
					.put("lob", getString(R.string.Car))
					.format();
			}
			else if (lineOfBusiness == LineOfBusiness.LX) {
				errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message)
					.put("lob", getString(R.string.Activity))
					.format();
			}
			showLOBNotSupportedAlertMessage(this, errorMessage, R.string.ok);
		}

		AbacusHelperUtils.downloadBucket(this);
		OmnitureTracking.trackPageLoadLaunchScreen();
	}

	@Override
	protected void onResume() {
		super.onResume();
		FacebookEvents.Companion.activateAppIfEnabledInConfig(this);
		Events.register(this);
		busRegistered = true;
		Sp.loadSearchParamsFromDisk(this);
		LaunchDb.getCollections(this);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		if (delayedPermissionEvent != null) {
			Events.post(delayedPermissionEvent);
			delayedPermissionEvent = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);
		busRegistered = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_SETTINGS && resultCode == Constants.RESULT_CHANGED_PREFS) {
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
			debugMenu.onCreateOptionsMenu(menu);
		}

		int actionBarLogo = ProductFlavorFeatureConfiguration.getInstance().getLaunchScreenActionLogo();
		if (actionBarLogo != 0) {
			getActionBar().setLogo(actionBarLogo);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mControllerFragment.shouldDisplayMenu()) {
			debugMenu.onPrepareOptionsMenu(menu);
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
		case R.id.menu_account: {
			Intent intent = new Intent(this, AccountSettingsActivity.class);
			startActivityForResult(intent, Constants.REQUEST_SETTINGS);
			return true;
		}
		}

		if (debugMenu.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
		@NonNull int[] grantResults) {

		boolean wasRationaleRequired = false;

		switch (requestCode) {
		case Constants.PERMISSION_REQUEST_LOCATION_WITH_RATIONALE:
			wasRationaleRequired = true; //FALL THRU
		case Constants.PERMISSION_REQUEST_LOCATION:
			Events.PermissionEvent.PermissionResult result
				= Events.PermissionEvent.PermissionResult.PermissionResult.from(grantResults[0]);
			Events.PermissionEvent event = new Events.PermissionEvent(result, requestCode, permissions[0], wasRationaleRequired);

			if (!busRegistered) {
				// Will be handled when bus is registered - we get this result before onResume() is called
				// 		- the system UI for permissions happens in a separate activity
				delayedPermissionEvent = event;
			}
			else {
				Events.post(event);
			}
			return;
		default:
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	/*
	 * FragmentAvailabilityUtils.IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (FTAG_CONTROLLER_FRAGMENT.equals(tag)) {
			frag = mControllerFragment;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (FTAG_CONTROLLER_FRAGMENT.equals(tag)) {
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

	public static void showLOBNotSupportedAlertMessage(Context context, CharSequence errorMessage,
		int confirmButtonResourceId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false)
			.setMessage(errorMessage)
			.setPositiveButton(confirmButtonResourceId, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}
}
