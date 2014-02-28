package com.expedia.bookings.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.expedia.bookings.R;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.fragment.TabletCheckoutControllerFragment;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

/**
 * TabletCheckoutActivity: The checkout activity designed for tablet 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletCheckoutActivity extends SherlockFragmentActivity implements IBackButtonLockListener,
	IBackManageable {

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, TabletCheckoutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(ARG_LOB, lob.name());
		return intent;
	}

	//Args
	private static final String ARG_LOB = "ARG_LOB";

	private static final String CHECKOUT_FRAG_TAG = "CHECKOUT_FRAG_TAG";

	//Containers..
	private ViewGroup mRootC;

	//Fragments
	TabletCheckoutControllerFragment mFragCheckoutController;

	//Other
	private boolean mBackButtonLocked = false;
	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_checkout);

		// Loading checkout data and blocking. this is disk i/o but we need the data loaded at this
		// point due to how the code is structured.
		loadCachedData(true);

		// TODO remove getAddedProperty/getAddedFlight and use selected
		boolean hasSelectedProperty = Db.getHotelSearch().getSelectedProperty() != null;
		boolean hasSelectedFlightTrip = Db.getFlightSearch().getSelectedFlightTrip() != null;

		if (!hasSelectedProperty) {
			Db.loadHotelSearchFromDisk(this, true); // TODO REMOVE BYPASSTIMEOUT=TRUE before shipping
			if (Db.getHotelSearch().getSelectedProperty() != null) {
				hasSelectedProperty = true;
			}
			Log.i("TabletCheckoutActivity: loadedHotelSearch=" + hasSelectedProperty);
		}

		if (!hasSelectedFlightTrip) {
			Db.loadCachedFlightData(this);
			if (Db.getFlightSearch().getSelectedFlightTrip() != null) {
				hasSelectedFlightTrip = true;
				Db.loadFlightSearchParamsFromDisk(this);
			}
			Log.i("TabletCheckoutActivity: loadedFlightSearch=" + hasSelectedFlightTrip);
		}

		if (!hasSelectedFlightTrip && !hasSelectedProperty) {
			Log.i("Failed to load either Flight or Hotel search data. Were you trying to book something?");
			finish();
			return;
		}

		if (Db.getTripBucket().isEmpty()) {
			Db.loadTripBucket(this);
		}

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);

		//Fragments
		mFragCheckoutController = Ui.findOrAddSupportFragment(this, R.id.root_layout,
			TabletCheckoutControllerFragment.class, CHECKOUT_FRAG_TAG);

		//Args
		updateLobFromIntent(getIntent());

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mHockeyPuck.onResume();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateLobFromIntent(intent);
		mFragCheckoutController.setCheckoutState(CheckoutState.OVERVIEW, false);
	}

	private void updateLobFromIntent(Intent intent) {
		if (intent.hasExtra(ARG_LOB)) {
			try {
				LineOfBusiness lob = LineOfBusiness.valueOf(intent.getStringExtra(ARG_LOB));
				mFragCheckoutController.setLob(lob);
			}
			catch (Exception ex) {
				Log.e("Exception parsing lob from intent.", ex);
			}

			// ActionBar title
			ActionBar ab = getActionBar();
			ab.setDisplayShowTitleEnabled(true);
			ab.setTitle(getString(R.string.Checkout));
		}
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

		//We allow debug users to jump between states
		if (!AndroidUtils.isRelease(this)) {
			//We use ordinal() + 1 for all ids and groups because 0 == Menu.NONE
			SubMenu subMen = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Checkout State");
			subMen.add(CheckoutState.OVERVIEW.ordinal() + 1, CheckoutState.OVERVIEW.ordinal() + 1,
				CheckoutState.OVERVIEW.ordinal() + 1, CheckoutState.OVERVIEW.name());
			subMen.add(CheckoutState.READY_FOR_CHECKOUT.ordinal() + 1, CheckoutState.READY_FOR_CHECKOUT.ordinal() + 1,
				CheckoutState.READY_FOR_CHECKOUT.ordinal() + 1, CheckoutState.READY_FOR_CHECKOUT.name());
			subMen.add(CheckoutState.CVV.ordinal() + 1, CheckoutState.CVV.ordinal() + 1,
				CheckoutState.CVV.ordinal() + 1, CheckoutState.CVV.name());
			subMen.add(CheckoutState.BOOKING.ordinal() + 1, CheckoutState.BOOKING.ordinal() + 1,
				CheckoutState.BOOKING.ordinal() + 1, CheckoutState.BOOKING.name());
			subMen.add(CheckoutState.CONFIRMATION.ordinal() + 1, CheckoutState.CONFIRMATION.ordinal() + 1,
				CheckoutState.CONFIRMATION.ordinal() + 1, CheckoutState.CONFIRMATION.name());
			return true;
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

		//We allow debug users to jump between states
		if (!AndroidUtils.isRelease(this) && mFragCheckoutController != null) {

			//All of our groups/ids are .ordinal() + 1 so we subtract here to make things easier
			int groupId = item.getGroupId() - 1;
			int id = item.getItemId() - 1;

			if (groupId == CheckoutState.OVERVIEW.ordinal() && id == CheckoutState.OVERVIEW.ordinal()) {
				Log.d("JumpTo: OVERVIEW");
				mFragCheckoutController.setCheckoutState(CheckoutState.OVERVIEW, true);
				return true;
			}
			else if (groupId == CheckoutState.READY_FOR_CHECKOUT.ordinal()
				&& id == CheckoutState.READY_FOR_CHECKOUT.ordinal()) {
				mFragCheckoutController.setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
				return true;
			}
			else if (groupId == CheckoutState.CVV.ordinal() && id == CheckoutState.CVV.ordinal()) {
				mFragCheckoutController.setCheckoutState(CheckoutState.CVV, true);
				return true;
			}
			else if (groupId == CheckoutState.BOOKING.ordinal() && id == CheckoutState.BOOKING.ordinal()) {
				mFragCheckoutController.setCheckoutState(CheckoutState.BOOKING, true);
				return true;
			}
			else if (groupId == CheckoutState.CONFIRMATION.ordinal() && id == CheckoutState.CONFIRMATION.ordinal()) {
				mFragCheckoutController.setCheckoutState(CheckoutState.CONFIRMATION, true);
				return true;
			}
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
