package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.enums.CheckoutTripBucketState;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.BookingUnavailableFragment.BookingUnavailableFragmentListener;
import com.expedia.bookings.fragment.TabletCheckoutControllerFragment;
import com.expedia.bookings.fragment.TabletCheckoutTripBucketControllerFragment;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ITripBucketBookClickListener;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.DebugMenuFactory;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.Log;

/**
 * TabletCheckoutActivity: The checkout activity designed for tablet 2014
 */
public class TabletCheckoutActivity extends TrackingFragmentActivity implements IBackManageable,
	ITripBucketBookClickListener, IAcceptingListenersListener, BookingUnavailableFragmentListener {

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, TabletCheckoutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(ARG_LOB, lob.name());
		return intent;
	}

	//Args
	private static final String ARG_LOB = "ARG_LOB";

	private static final String CHECKOUT_FRAG_TAG = "CHECKOUT_FRAG_TAG";
	private static final String TRIP_BUCKET_FRAG_TAG = "TRIP_BUCKET_FRAG_TAG";

	private static final String INSTANCE_CURRENT_LOB = "INSTANCE_CURRENT_LOB";

	//Fragments
	TabletCheckoutControllerFragment mFragCheckoutController;
	TabletCheckoutTripBucketControllerFragment mFragTripBucketController;

	//Other
	private LineOfBusiness mLob;

	private DebugMenu debugMenu;

	boolean mIsBailing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		debugMenu = DebugMenuFactory.newInstance(this, null);

		if (Sp.isEmpty()) {
			finish();
			mIsBailing = true;
		}

		super.onCreate(savedInstanceState);

		if (mIsBailing) {
			return;
		}

		setContentView(R.layout.activity_tablet_checkout);

		// Args
		if (savedInstanceState == null) {
			updateLobFromIntent(getIntent());
		}
		else {
			updateLobFromString(savedInstanceState.getString(INSTANCE_CURRENT_LOB));
		}

		// Fragments
		mFragCheckoutController = Ui.findOrAddSupportFragment(this, R.id.checkout_controller_root, TabletCheckoutControllerFragment.class, CHECKOUT_FRAG_TAG);
		mFragTripBucketController = Ui.findOrAddSupportFragment(this, R.id.trip_bucket_controller_root, TabletCheckoutTripBucketControllerFragment.class, TRIP_BUCKET_FRAG_TAG);

		// Actionbar
		ActionBar ab = getActionBar();
		ab.setCustomView(R.layout.actionbar_tablet_title);
		TextView title = com.mobiata.android.util.Ui.findView(ab.getCustomView(), R.id.text1);
		title.setText(R.string.Checkout);

		int actionBarLogo = ProductFlavorFeatureConfiguration.getInstance().getLaunchScreenActionLogo();
		if (actionBarLogo != 0) {
			getActionBar().setLogo(actionBarLogo);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isFinishing()) {
			clearCCNumber();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		LineOfBusiness lob = mFragCheckoutController.getLob();
		if (lob != null) {
			outState.putString(INSTANCE_CURRENT_LOB, lob.name());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		updateLobFromIntent(intent);
	}

	private void updateLobFromIntent(Intent intent) {
		if (intent.hasExtra(ARG_LOB)) {
			try {
				String lobString = intent.getStringExtra(ARG_LOB);
				intent.removeExtra(ARG_LOB);
				updateLobFromString(lobString);
			}
			catch (Exception ex) {
				Log.e("Exception parsing lob from intent.", ex);
			}
		}
	}

	private void updateLobFromString(String lobString) {
		LineOfBusiness lob = LineOfBusiness.valueOf(lobString);
		updateLob(lob);
	}

	public void updateLob(LineOfBusiness lob) {
		mLob = lob;
		if (mFragCheckoutController != null) {
			mFragCheckoutController.setLob(lob);
		}
		if (mFragTripBucketController != null) {
			mFragTripBucketController.setLob(lob);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFragCheckoutController.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * MENU STUFF
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retVal = super.onCreateOptionsMenu(menu);

		debugMenu.onCreateOptionsMenu(menu);

		//We allow debug users to jump between states
		if (BuildConfig.DEBUG) {
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
		debugMenu.onPrepareOptionsMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			clearCCNumber();
			return true;
		}
		}

		if (debugMenu.onOptionsItemSelected(item)) {
			return true;
		}

		//We allow debug users to jump between states
		if (BuildConfig.DEBUG && mFragCheckoutController != null) {

			//All of our groups/ids are .ordinal() + 1 so we subtract here to make things easier
			int groupId = item.getGroupId() - 1;
			int id = item.getItemId() - 1;

			if (groupId == CheckoutState.OVERVIEW.ordinal() && id == CheckoutState.OVERVIEW.ordinal()) {
				Log.d("JumpTo: OVERVIEW");
				setCheckoutState(CheckoutState.OVERVIEW, true);
				return true;
			}
			else if (groupId == CheckoutState.READY_FOR_CHECKOUT.ordinal()
				&& id == CheckoutState.READY_FOR_CHECKOUT.ordinal()) {
				setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
				return true;
			}
			else if (groupId == CheckoutState.CVV.ordinal() && id == CheckoutState.CVV.ordinal()) {
				setCheckoutState(CheckoutState.CVV, true);
				return true;
			}
			else if (groupId == CheckoutState.BOOKING.ordinal() && id == CheckoutState.BOOKING.ordinal()) {
				setCheckoutState(CheckoutState.BOOKING, true);
				return true;
			}
			else if (groupId == CheckoutState.CONFIRMATION.ordinal() && id == CheckoutState.CONFIRMATION.ordinal()) {
				setCheckoutState(CheckoutState.CONFIRMATION, true);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void setCheckoutState(CheckoutState state, boolean animate) {
		if (mFragCheckoutController != null) {
			mFragCheckoutController.setCheckoutState(state, animate);
		}
	}

	public CheckoutState getCheckoutState() {
		if (mFragCheckoutController != null) {
			return mFragCheckoutController.getCheckoutState();
		}
		return null;
	}

	public void updateBucketItems(boolean animate) {
		if (mFragTripBucketController != null) {
			mFragTripBucketController.updateBucketItems(animate);
		}
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	/*
	IAcceptingListenersListener
	 */

	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (frag == mFragCheckoutController) {
			if (acceptingListener) {
				mFragCheckoutController.registerStateListener(mCheckoutStateHelper, false);
			}
			else {
				mFragCheckoutController.unRegisterStateListener(mCheckoutStateHelper);
			}
		}
	}

	private StateListenerHelper<CheckoutState> mCheckoutStateHelper = new StateListenerHelper<CheckoutState>() {
		@Override
		public void onStateTransitionStart(CheckoutState stateOne, CheckoutState stateTwo) {
			if (stateOne != stateTwo) {
				mFragTripBucketController.startStateTransition(CheckoutTripBucketState.transmogrify(stateOne), CheckoutTripBucketState.transmogrify(stateTwo));
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
			if (stateOne != stateTwo) {
				mFragTripBucketController.updateStateTransition(CheckoutTripBucketState.transmogrify(stateOne), CheckoutTripBucketState.transmogrify(stateTwo), percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutState stateOne, CheckoutState stateTwo) {
			if (stateOne != stateTwo) {
				mFragTripBucketController.endStateTransition(CheckoutTripBucketState.transmogrify(stateOne), CheckoutTripBucketState.transmogrify(stateTwo));
			}
		}

		@Override
		public void onStateFinalized(CheckoutState state) {
			mFragTripBucketController.setState(CheckoutTripBucketState.transmogrify(state), false);
		}
	};

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public void onBackPressed() {
		if (!mBackManager.doOnBackPressed()) {
			clearCCNumber();
			super.onBackPressed();
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

	/*
	 * ITripBucketBookClickListener
	 */
	public void onTripBucketBookClicked(LineOfBusiness lob) {
		updateLob(lob);
		CheckoutState state;
		if (lob == LineOfBusiness.FLIGHTS && !Db.getTripBucket().getFlight().canBePurchased()) {
			state = CheckoutState.BOOKING_UNAVAILABLE;
		}
		else if (lob == LineOfBusiness.HOTELS && !Db.getTripBucket().getHotel().canBePurchased()) {
			state = CheckoutState.BOOKING_UNAVAILABLE;
		}
		else {
			state = mFragCheckoutController.getCheckoutInformationIsValid() ? CheckoutState.READY_FOR_CHECKOUT : CheckoutState.OVERVIEW;
		}

		// Tracking
		boolean isAirAttachScenario = lob == LineOfBusiness.HOTELS &&
			Db.getTripBucket().getHotel().hasAirAttachRate();
		OmnitureTracking.trackBookNextClick(lob, isAirAttachScenario);

		updateBucketItems(true);
		setCheckoutState(state, true);
	}

	/*
	 * BookingUnavailableFragment listener
	 */

	@Override
	public void onTripBucketItemRemoved(LineOfBusiness lob) {
		if (mFragTripBucketController != null) {
			mFragTripBucketController.onTripBucketItemRemoved(lob);
		}
	}

	@Override
	public void onSelectNewTripItem(LineOfBusiness lob) {
		// ignore
	}

	private void clearCCNumber() {
		try {
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null);
			Db.getBillingInfo().setNumber(null);
		}
		catch (Exception ex) {
			Log.e("Error clearing billingInfo card number", ex);
		}
	}
}
