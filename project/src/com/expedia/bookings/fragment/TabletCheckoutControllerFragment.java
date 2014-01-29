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
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
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
 * TabletCheckoutControllerFragment: designed for tablet checkout 2014
 * This controls all the fragments relating to tablet checkout
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletCheckoutControllerFragment extends Fragment implements IBackManageable,
	IStateProvider<CheckoutState>, IFragmentAvailabilityProvider, CVVEntryFragmentListener {

	private static final String STATE_CHECKOUT_STATE = "STATE_CHECKOUT_STATE";

	private static final String FRAG_TAG_BUCKET_FLIGHT = "FRAG_TAG_BUCKET_FLIGHT";
	private static final String FRAG_TAG_BUCKET_HOTEL = "FRAG_TAG_BUCKET_HOTEL";
	private static final String FRAG_TAG_CHECKOUT_INFO = "FRAG_TAG_CHECKOUT_INFO";
	private static final String FRAG_TAG_CVV = "FRAG_TAG_CVV";

	//Containers
	private ScrollView mBucketScrollContainer;
	private ViewGroup mBucketHotelContainer;
	private ViewGroup mBucketFlightContainer;
	private ViewGroup mSlideContainer;
	private ViewGroup mFormContainer;
	private ViewGroup mCvvContainer;

	//Views
	private TextView mBucketDateRange;

	//frags
	private ResultsTripBucketFlightFragment mBucketFlightFrag;
	private ResultsTripBucketHotelFragment mBucketHotelFrag;
	private TabletCheckoutFormsFragment mCheckoutFragment;
	private CVVEntryFragment mCvvFrag;

	//vars
	private LineOfBusiness mLob = LineOfBusiness.FLIGHTS;
	private StateManager<CheckoutState> mStateManager = new StateManager<CheckoutState>(
		CheckoutState.OVERVIEW, this);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_checkout_controller, null, false);

		mBucketScrollContainer = Ui.findView(view, R.id.trip_bucket_scroll);
		mBucketHotelContainer = Ui.findView(view, R.id.bucket_hotel_frag_container);
		mBucketFlightContainer = Ui.findView(view, R.id.bucket_flight_frag_container);
		mFormContainer = Ui.findView(view, R.id.checkout_forms_container);
		mSlideContainer = Ui.findView(view, R.id.finish_checkout_container);
		mCvvContainer = Ui.findView(view, R.id.cvv_container);

		mBucketDateRange = Ui.findView(view, R.id.trip_date_range);
		mBucketDateRange.setText("FEB 8 - CAT 12");//TODO: real date range

		if (savedInstanceState != null) {
			mStateManager.setDefaultState(CheckoutState.valueOf(savedInstanceState.getString(
				STATE_CHECKOUT_STATE,
				CheckoutState.OVERVIEW.name())));
		}

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutState>(), false);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUT_STATE, mStateManager.getState().name());
	}

	@Override
	public void onResume() {
		super.onResume();
		mBackManager.registerWithParent(this);
		setCheckoutState(mStateManager.getState(), false);
		checkForAddedTrips();
	}

	@Override
	public void onPause() {
		super.onPause();
		mBackManager.unregisterWithParent(this);
	}

	private void checkForAddedTrips() {
		boolean hasHotel = Db.getHotelSearch().getAddedProperty() != null;
		mBucketHotelContainer.setVisibility(hasHotel ? View.VISIBLE : View.GONE);

		boolean hasFlight = Db.getFlightSearch().getAddedFlightTrip() != null;
		mBucketFlightContainer.setVisibility(hasFlight ? View.VISIBLE : View.GONE);
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
			if (mStateManager.isAnimating()) {
				//If we are in the middle of state transition, just reverse it
				setCheckoutState(mStateManager.getState(), true);
				return true;
			}
			else {
				if (mStateManager.getState() == CheckoutState.CVV) {
					setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
					return true;
				}
			}
			return false;
		}

	};

	/*
	 * CheckoutState LISTENER
	 */

	private StateListenerHelper<CheckoutState> mStateHelper = new StateListenerHelper<CheckoutState>() {

		@Override
		public void onStateTransitionStart(CheckoutState stateOne, CheckoutState stateTwo) {
			if (stateOne == CheckoutState.OVERVIEW && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowReadyForCheckoutPercentage(0f);
				mSlideContainer.setVisibility(View.VISIBLE);
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.OVERVIEW) {
				mSlideContainer.setVisibility(View.VISIBLE);
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.CVV) {
				setShowCvvPercentage(0f);
				mCvvContainer.setVisibility(View.VISIBLE);
			}
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowReadyForCheckoutPercentage(0f);
				mSlideContainer.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
			if (stateOne == CheckoutState.OVERVIEW && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowReadyForCheckoutPercentage(percentage);
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.OVERVIEW) {
				setShowReadyForCheckoutPercentage(1f - percentage);
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.CVV) {
				setShowCvvPercentage(percentage);
			}
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowCvvPercentage(1f - percentage);
				setShowReadyForCheckoutPercentage(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutState stateOne, CheckoutState stateTwo) {
			if (stateOne == CheckoutState.OVERVIEW && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				//TODO
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.OVERVIEW) {
				//TODO
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.CVV) {
				//TODO
			}
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				//TODO
			}
		}

		@Override
		public void onStateFinalized(CheckoutState state) {
			setFragmentState(state);
			setVisibilityState(state);

			if (state == CheckoutState.OVERVIEW) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(0f);
			}
			else if (state == CheckoutState.READY_FOR_CHECKOUT) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(1f);
			}
			else if (state == CheckoutState.CVV) {
				setShowCvvPercentage(1f);
				setShowReadyForCheckoutPercentage(1f);
			}
		}
	};

	private void setShowCvvPercentage(float percentage) {
		mBucketScrollContainer.setTranslationX(percentage * -mBucketScrollContainer.getWidth());
		mFormContainer.setTranslationX(percentage * mFormContainer.getWidth());
		mCvvContainer.setTranslationX((1f - percentage) * -mCvvContainer.getWidth());
	}

	private void setShowReadyForCheckoutPercentage(float percentage) {
		mSlideContainer.setTranslationY((1f - percentage) * mSlideContainer.getHeight());
	}

	private void setVisibilityState(CheckoutState state) {
		if (state == CheckoutState.OVERVIEW) {
			mFormContainer.setVisibility(View.VISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mCvvContainer.setVisibility(View.INVISIBLE);
		}
		else if (state == CheckoutState.READY_FOR_CHECKOUT) {
			mFormContainer.setVisibility(View.VISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
		}
		else if (state == CheckoutState.CVV) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mBucketScrollContainer.setVisibility(View.INVISIBLE);
			mCvvContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.INVISIBLE);
		}
	}

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
		boolean cvvAvailable = state != CheckoutState.OVERVIEW;//If we are in cvv mode or are ready to enter it, we add cvv

		mBucketFlightFrag = (ResultsTripBucketFlightFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightBucketItemAvailable, FRAG_TAG_BUCKET_FLIGHT,
			manager, transaction, this, R.id.bucket_flight_frag_container, false);

		mBucketHotelFrag = (ResultsTripBucketHotelFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelBucketItemAvailable, FRAG_TAG_BUCKET_HOTEL,
			manager, transaction, this, R.id.bucket_hotel_frag_container, false);

		mCheckoutFragment = (TabletCheckoutFormsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			checkoutFormsAvailable, FRAG_TAG_CHECKOUT_INFO, manager, transaction, this,
			R.id.checkout_forms_container, false);

		mCvvFrag = (CVVEntryFragment) FragmentAvailabilityUtils.setFragmentAvailability(cvvAvailable, FRAG_TAG_CVV,
			manager, transaction, this, R.id.cvv_container, false);

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
		else if (FRAG_TAG_CVV.equals(tag)) {
			return mCvvFrag;
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
		else if (FRAG_TAG_CVV.equals(tag)) {
			//return CVVEntryFragment.newInstance(getActivity(), Db.getBillingInfo());

			//TODO: THIS IS SUPER FAKE
			return CVVEntryFragment.newInstance("Cat Miggins", "Super Black Platinum Gold Card Premium",
				CreditCardType.UNKNOWN);
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

	/*
	 * CVVEntyrFragmentListener
	 * @see com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener#onBook(java.lang.String)
	 */

	@Override
	public void onBook(String cvv) {
		// TODO: We should probably book or something.

	}

}