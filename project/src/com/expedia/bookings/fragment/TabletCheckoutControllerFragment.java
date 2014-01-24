package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.mobiata.android.util.Ui;

/**
 *  TabletCheckoutControllerFragment: designed for tablet checkout 2014
 *  This controls all the fragments relating to tablet checkout
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletCheckoutControllerFragment extends Fragment implements IBackManageable,
		IStateProvider<CheckoutState>, IFragmentAvailabilityProvider {

	private static final String FRAG_TAG_BUCKET_FLIGHT = "FRAG_TAG_BUCKET_FLIGHT";
	private static final String FRAG_TAG_BUCKET_HOTEL = "FRAG_TAG_BUCKET_HOTEL";
	private static final String FRAG_TAG_CHECKOUT_INFO = "FRAG_TAG_CHECKOUT_INFO";

	//Containers
	private ViewGroup mRootC;
	private ViewGroup mTripBucketContainer;
	private ViewGroup mBucketHotelContainer;
	private ViewGroup mBucketFlightContainer;
	private ViewGroup mCheckoutFormsContainer;

	//Views
	private TextView mBucketDateRange;

	//frags
	private ResultsTripBucketFlightFragment mBucketFlightFrag;
	private ResultsTripBucketHotelFragment mBucketHotelFrag;
	private TabletCheckoutFormsFragment mCheckoutFragment;

	//vars
	private LineOfBusiness mLob = LineOfBusiness.FLIGHTS;
	private StateManager<CheckoutState> mStateManager = new StateManager<CheckoutState>(
			CheckoutState.OVERVIEW, this);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_checkout_controller, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTripBucketContainer = Ui.findView(view, R.id.trip_bucket_container);
		mCheckoutFormsContainer = Ui.findView(view, R.id.checkout_forms_container);

		mBucketDateRange = Ui.findView(view, R.id.trip_date_range);
		mBucketDateRange.setText("FEB 8 - CAT 12");//TODO: real date range

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutState>(), false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBackManager.registerWithParent(this);
		setCheckoutState(mStateManager.getState(), false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mBackManager.unregisterWithParent(this);
	}

	/*
	 * GETTERS/SETTERS
	 */

	public void setLob(LineOfBusiness lob) {
		mLob = lob;
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	public void setCheckoutState(CheckoutState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			return false;
		}

	};

	/*
	 * CheckoutState LISTENER
	 */

	private StateListenerHelper<CheckoutState> mStateHelper = new StateListenerHelper<CheckoutState>() {

		@Override
		public void onStateTransitionStart(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateTransitionUpdate(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
		}

		@Override
		public void onStateTransitionEnd(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateFinalized(CheckoutState state) {
			setFragmentState(state);
		}
	};

	private void setFragmentState(CheckoutState state) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction

		FragmentTransaction transaction = manager.beginTransaction();

		boolean flightBucketItemAvailable = true;
		boolean hotelBucketItemAvailable = true;
		boolean checkoutFormsAvailable = true;

		mBucketFlightFrag = (ResultsTripBucketFlightFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightBucketItemAvailable, FRAG_TAG_BUCKET_FLIGHT,
				manager, transaction, this, R.id.bucket_flight_frag_container, false);

		mBucketHotelFrag = (ResultsTripBucketHotelFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelBucketItemAvailable, FRAG_TAG_BUCKET_HOTEL,
				manager, transaction, this, R.id.bucket_hotel_frag_container, false);

		mCheckoutFragment = (TabletCheckoutFormsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				checkoutFormsAvailable, FRAG_TAG_CHECKOUT_INFO, manager, transaction, this,
				R.id.checkout_forms_container, false);

		transaction.commit();
	}

	/*
	 * CheckoutState ISTATEPROVIDER
	 */

	private StateListenerCollection<CheckoutState> mStateListeners = new StateListenerCollection<CheckoutState>(
			mStateManager.getState());

	@Override
	public void startStateTransition(CheckoutState stateOne, CheckoutState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(CheckoutState stateOne, CheckoutState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(CheckoutState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<CheckoutState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<CheckoutState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			return mBucketFlightFrag;
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			return mBucketHotelFrag;
		}
		else if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			return mCheckoutFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			return ResultsTripBucketFlightFragment.newInstance();
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			return ResultsTripBucketHotelFragment.newInstance();
		}
		else if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			return TabletCheckoutFormsFragment.newInstance();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			((ResultsTripBucketFlightFragment) frag).setExpanded(mLob == LineOfBusiness.FLIGHTS);
			((ResultsTripBucketFlightFragment) frag).setShowButton(false);
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			((ResultsTripBucketHotelFragment) frag).setExpanded(mLob == LineOfBusiness.HOTELS);
			((ResultsTripBucketHotelFragment) frag).setShowButton(false);
		}
		else if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			((TabletCheckoutFormsFragment) frag).setLob(mLob);
		}
	}

}