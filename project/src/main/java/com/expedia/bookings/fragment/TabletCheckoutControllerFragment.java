package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.activity.TabletResultsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.CheckoutFormState;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.BookingUnavailableFragment.BookingUnavailableFragmentListener;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightBookingFragment.FlightBookingState;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.HotelBookingFragment.HotelBookingState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.expedia.bookings.widget.SlideToWidgetJB;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.otto.Subscribe;

/**
 * TabletCheckoutControllerFragment: designed for tablet checkout 2014
 * This controls all the fragments relating to tablet checkout
 */
public class TabletCheckoutControllerFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutState>, IFragmentAvailabilityProvider, CVVEntryFragmentListener,
	CheckoutInformationListener, SlideToWidgetJB.ISlideToListener,
	TabletCheckoutFormsFragment.ISlideToPurchaseSizeProvider, IAcceptingListenersListener, BookingUnavailableFragmentListener {

	private static final String STATE_CHECKOUT_STATE = "STATE_CHECKOUT_STATE";

	private static final String FRAG_TAG_CHECKOUT_INFO = "FRAG_TAG_CHECKOUT_INFO";
	private static final String FRAG_TAG_SLIDE_TO_PURCHASE = "FRAG_TAG_SLIDE_TO_PURCHASE";
	private static final String FRAG_TAG_CVV = "FRAG_TAG_CVV";
	private static final String FRAG_TAG_CONF_FLIGHT = "FRAG_TAG_CONF_FLIGHT";
	private static final String FRAG_TAG_CONF_HOTEL = "FRAG_TAG_CONF_HOTEL";
	private static final String FRAG_TAG_BLUR_BG = "FRAG_TAG_BLUR_BG";
	private static final String FRAG_TAG_BOOKING_UNAVAILABLE = "FRAG_TAG_BOOKING_UNAVAILABLE";

	// Containers
	private TouchableFrameLayout mRootC;
	private ViewGroup mSlideContainer;
	private ViewGroup mFormContainer;
	private ViewGroup mCvvContainer;
	private ViewGroup mBookingContainer;
	private ViewGroup mBookingUnavailableContainer;
	private ViewGroup mConfirmationContainer;

	// Fragments
	private TabletCheckoutFormsFragment mCheckoutFragment;
	private TabletCheckoutSlideFragment mSlideFragment;
	private CVVEntryFragment mCvvFrag;
	private FlightBookingFragment mFlightBookingFrag;
	private HotelBookingFragment mHotelBookingFrag;
	private TabletFlightConfirmationFragment mFlightConfFrag;
	private TabletHotelConfirmationFragment mHotelConfFrag;
	private ResultsBackgroundImageFragment mBlurredBgFrag;
	private BookingUnavailableFragment mBookingUnavailableFragment;

	private static final String TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG = "TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG";
	private static final String TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG = "TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG";
	private static final String TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG = "TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG";

	private static final String INSTANCE_TRIP_BUCKET_OPEN = "INSTANCE_TRIP_BUCKET_OPEN";

	private static final String INSTANCE_CURRENT_LOB = "INSTANCE_CURRENT_LOB";

	private boolean mCheckoutInformationIsValid = false;

	//vars
	private StateManager<CheckoutState> mStateManager = new StateManager<>(CheckoutState.OVERVIEW, this);

	private ThrobberDialog mHotelProductDownloadThrobber;
	private ThrobberDialog mCreateTripDownloadThrobber;
	private ThrobberDialog mFlightCreateTripDownloadThrobber;

	private LineOfBusiness mCurrentLob;

	private boolean mTripBucketOpen = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		if (savedInstanceState == null) {
			setLob(((TabletCheckoutActivity) getActivity()).getLob());
		}

		// We should ALWAYS have an instance of the HotelBookingFragment and FlightBookingFragment.
		// Hence we should not use FragmentAvailabilityUtils.setFragmentAvailability

		if (Db.getTripBucket().getHotel() != null) {
			mHotelBookingFrag = Ui.findOrAddSupportFragment(getActivity(), View.NO_ID, HotelBookingFragment.class, HotelBookingFragment.TAG);
		}

		if (Db.getTripBucket().getFlight() != null) {
			mFlightBookingFrag = Ui.findOrAddSupportFragment(getActivity(), View.NO_ID, FlightBookingFragment.class, FlightBookingFragment.TAG);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_checkout_controller, null, false);

		mFormContainer = Ui.findView(mRootC, R.id.checkout_forms_container);
		mSlideContainer = Ui.findView(mRootC, R.id.slide_container);
		mCvvContainer = Ui.findView(mRootC, R.id.cvv_container);
		mBookingContainer = Ui.findView(mRootC, R.id.booking_container);
		mBookingUnavailableContainer = Ui.findView(mRootC, R.id.booking_unavailable_container);
		mConfirmationContainer = Ui.findView(mRootC, R.id.confirmation_container);

		if (savedInstanceState != null) {
			mStateManager.setDefaultState(CheckoutState.valueOf(savedInstanceState.getString(
				STATE_CHECKOUT_STATE,
				CheckoutState.OVERVIEW.name())));
			mCurrentLob = LineOfBusiness.valueOf(savedInstanceState.getString(INSTANCE_CURRENT_LOB));
			mTripBucketOpen = savedInstanceState.getBoolean(INSTANCE_TRIP_BUCKET_OPEN);
		}
		else {
			// Initial load of this fragment, so attempt to load user data
			BookingInfoUtils.populateTravelerDataFromUser(getActivity(), getLob());
			BookingInfoUtils.populatePaymentDataFromUser(getActivity(), getLob());
		}

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutState>(), false);

		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUT_STATE, mStateManager.getState().name());
		outState.putBoolean(INSTANCE_TRIP_BUCKET_OPEN, mTripBucketOpen);
		if (mCurrentLob != null) {
			outState.putString(INSTANCE_CURRENT_LOB, mCurrentLob.name());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		CheckoutState state = mStateManager.getState();
		if (getLob() == LineOfBusiness.HOTELS) {
			state = getStartState(Db.getTripBucket().getHotel());
		}
		if (getLob() == LineOfBusiness.FLIGHTS) {
			state = getStartState(Db.getTripBucket().getFlight());
		}
		setCheckoutState(state, false);
	}

	private CheckoutState getStartState(TripBucketItem item) {
		CheckoutState state = mStateManager.getState();

		if (item != null) {
			if (item.getState() == TripBucketItemState.BOOKING_UNAVAILABLE || item.getState() == TripBucketItemState.EXPIRED) {
				state = CheckoutState.BOOKING_UNAVAILABLE;
			}
			if (item.getState() == TripBucketItemState.PURCHASED) {
				state = CheckoutState.CONFIRMATION;
			}
		}

		return state;
	}

	@Override
	public void onResume() {
		super.onResume();

		Events.register(this);

		mBackManager.registerWithParent(this);

		IAcceptingListenersListener readyForListeners = Ui.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);

		mBackManager.unregisterWithParent(this);
		if (getActivity().isFinishing()) {
			if (Db.getTripBucket().getHotel() != null) {
				Db.getTripBucket().getHotel().setIsCouponApplied(false);
				Db.getTripBucket().getHotel().setHasPriceChanged(false);
				Db.getTripBucket().getHotel().setSelected(false);
			}
			if (Db.getTripBucket().getFlight() != null) {
				Db.getTripBucket().getFlight().setHasPriceChanged(false);
				Db.getTripBucket().getFlight().setSelected(false);
			}
		}
		Db.saveTripBucket(getActivity());
	}

	public CheckoutState getCheckoutState() {
		if (mStateManager != null) {
			return mStateManager.getState();
		}
		else {
			return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mCheckoutFragment != null) {
			mCheckoutFragment.onActivityResult(requestCode, resultCode, data);
		}
		if (getCurrentBookingFragment() != null) {
			getCurrentBookingFragment().onActivityResult(requestCode, resultCode, data);
		}
	}

	/*
	 * LobableFragment
	 */

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mCurrentLob != null && mCurrentLob != lob) {
			Db.clearGoogleWallet();
			WalletUtils.unbindAllWalletDataFromBillingInfo(Db.getBillingInfo());
		}
		mCurrentLob = lob;

		// Remove invalid credit cards when going from hotels -> flights
		if (mCurrentLob == LineOfBusiness.FLIGHTS) {
			Db.loadBillingInfo(getActivity());
			BillingInfo billingInfo = Db.getBillingInfo();

			boolean isValidCard = Db.getTripBucket().getFlight().isCardTypeSupported(billingInfo.getCardType());
			if (!isValidCard) {
				// We should probably be calling billingInfo.delete(getActivity()) instead, but due
				// to race conditions, getActivity() can return null here. This is a less comprehensive
				// but less crashy fix.
				billingInfo.setStoredCard(null);
				billingInfo.setNumber(null);
				billingInfo.setSecurityCode(null);
			}
		}

		if (mCheckoutFragment != null) {
			mCheckoutFragment.setLob(lob);
		}
		if (mSlideFragment != null) {
			mSlideFragment.setLob(lob);
		}
		if (mBookingUnavailableFragment != null) {
			mBookingUnavailableFragment.setLob(lob);
		}

		if (lob == LineOfBusiness.HOTELS) {
			AdTracker.trackHotelCheckoutStarted();
		}
		if (lob == LineOfBusiness.FLIGHTS) {
			AdTracker.trackFlightCheckoutStarted();
		}
	}

	/*
	 * GETTERS/SETTERS
	 */

	public void setCheckoutState(CheckoutState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	private boolean bookingWithGoogleWallet() {
		return Db.getBillingInfo().isUsingGoogleWallet();
	}

	public void rebindCheckoutFragment() {
		mCheckoutFragment.bindAll();
	}

	private BookingFragment getCurrentBookingFragment() {
		if (getLob() == LineOfBusiness.FLIGHTS) {
			return mFlightBookingFrag;
		}
		else {
			return mHotelBookingFrag;
		}
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
				// Let's consume the back button event when the checkout state is BOOKING
				if (mStateManager.getState() == CheckoutState.BOOKING) {
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
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.BOOKING) {
				setShowBookingPercentage(0f);
			}
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.BOOKING) {
				setShowBookingPercentage(0f);
			}
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowReadyForCheckoutPercentage(0f);
				mSlideContainer.setVisibility(View.VISIBLE);
				mFormContainer.setVisibility(View.VISIBLE);
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
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.BOOKING) {
				setShowBookingPercentage(percentage);
			}
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.BOOKING) {
				setShowReadyForCheckoutPercentage(1f - percentage);
				setShowBookingPercentage(percentage);
			}
			if (stateOne == CheckoutState.OVERVIEW && stateTwo == CheckoutState.BOOKING_UNAVAILABLE) {
				setShowReadyForCheckoutPercentage(1f - percentage);
				setShowBookingUnavailablePercentage(percentage);
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
			else if (stateOne == CheckoutState.READY_FOR_CHECKOUT && stateTwo == CheckoutState.BOOKING) {
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
				doCreateTrip();
			}
			else if (state == CheckoutState.FORM_OPEN) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(0f);
			}
			else if (state == CheckoutState.READY_FOR_CHECKOUT) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(1f);
				OmnitureTracking.trackTabletSlideToPurchasePageLoad(getActivity(), getLob());
			}
			else if (state == CheckoutState.CVV) {
				setShowCvvPercentage(1f);
				setShowReadyForCheckoutPercentage(0f);
				OmnitureTracking.trackTabletCVVPageLoad(getActivity(), getLob());
			}
			else if (state == CheckoutState.BOOKING) {
				setShowBookingPercentage(1f);
				if (bookingWithGoogleWallet()) {
					setShowReadyForCheckoutPercentage(0f);
				}
				startBooking();
			}
			else if (state == CheckoutState.CONFIRMATION) {
				OmnitureTracking.trackTabletConfirmationPageLoad(getActivity(), getLob());
			}

			if (state == CheckoutState.BOOKING) {
				mRootC.setBlockNewEventsEnabled(true);
				if (getActivity() != null && getActivity().getActionBar() != null) {
					getActivity().getActionBar().setHomeButtonEnabled(false);
				}
			}
			else {
				mRootC.setBlockNewEventsEnabled(false);
				if (getActivity() != null && getActivity().getActionBar() != null) {
					getActivity().getActionBar().setHomeButtonEnabled(true);
				}
			}
		}
	};

	//private List<TripBucketItemFragment> mTripBucketItemFragments;
	//private ViewGroup[] mTripBucketItemViews;

	//private class TripBucketOrchestrator extends StateListenerHelper<TripBucketItemState> {
	//	private TripBucketItemFragment mFragment;
	//	private boolean mForward = true;
	//	private int mShift = 0;
	//	private int mExpandedPosition = 0;
	//	private int mItemToExpandPosition = 0;

	//	public TripBucketOrchestrator(TripBucketItemFragment frag) {
	//		mFragment = frag;
	//	}

	//	@Override
	//	public void onStateTransitionStart(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
	//		// Ignore
	//	}

	//	@Override
	//	public void onStateTransitionUpdate(TripBucketItemState stateOne, TripBucketItemState stateTwo,
	//										float percentage) {
	//		if (stateTwo == TripBucketItemState.EXPANDED || stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {
	//			for (int i = 0; i < mTripBucketItemFragments.size(); i++) {
	//				TripBucketItemFragment f = mTripBucketItemFragments.get(i);
	//				if (f.getState() == TripBucketItemState.EXPANDED
	//					|| f.getState() == TripBucketItemState.SHOWING_PRICE_CHANGE) {
	//					mExpandedPosition = i;
	//				}
	//				if (f == mFragment) {
	//					mItemToExpandPosition = i;
	//				}
	//			}

	//			mForward = mExpandedPosition < mItemToExpandPosition;
	//			if (mForward) {
	//				mShift = mTripBucketItemFragments.get(mExpandedPosition).getExpandedHeight();
	//				mShift += mTripBucketItemFragments.get(mExpandedPosition).getPriceChangeHeight();
	//			}
	//			else {
	//				mShift = mTripBucketItemFragments.get(mItemToExpandPosition).getExpandedHeight();
	//				mShift += mTripBucketItemFragments.get(mItemToExpandPosition).getPriceChangeHeight();
	//			}

	//			float amount;
	//			if (mForward) {
	//				amount = -mShift * percentage;
	//			}
	//			else {
	//				amount = mShift * (1.0f - percentage);
	//			}

	//			int start = Math.min(mExpandedPosition, mItemToExpandPosition);
	//			int end = Math.max(mExpandedPosition, mItemToExpandPosition);
	//			for (int i = start + 1; i <= end; i++) {
	//				if (mTripBucketItemViews[i] != null) {
	//					if (mForward) {
	//						mTripBucketItemViews[i].setTranslationY(amount);
	//					}
	//					else {
	//						mTripBucketItemViews[i].setTranslationY(-amount);
	//					}
	//				}
	//			}
	//		}
	//	}

	//	@Override
	//	public void onStateTransitionEnd(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
	//		if (stateTwo == TripBucketItemState.EXPANDED) {
	//			for (ViewGroup view : mTripBucketItemViews) {
	//				view.setTranslationY(0);
	//			}
	//		}
	//	}

	//	@Override
	//	public void onStateFinalized(TripBucketItemState state) {
	//		// Ignore
	//	}
	//}

	private void setShowCvvPercentage(float percentage) {
		mFormContainer.setTranslationX(percentage * mFormContainer.getWidth());
		Resources res = getResources();
		float startx = -mRootC.getWidth();
		if (!res.getBoolean(R.bool.portrait)) {
			// Adjust for trip bucket taking up left 1/3 of screen
			startx += mRootC.getWidth() / 3;

			// Adjust for empty horizontal padding between edge of screen and cc digits
			float padding = mRootC.getWidth() - (res.getDimensionPixelSize(R.dimen.cvv_credit_card_section_width) + res.getDimensionPixelSize(R.dimen.cvv_credit_card_spacer) + res.getDimensionPixelSize(R.dimen.cvv_digits_section_width));
			startx += padding / 2;
		}
		float endx = 0f;
		mCvvContainer.setTranslationX(startx + (endx - startx) * percentage);
	}

	private void setShowReadyForCheckoutPercentage(float percentage) {
		mSlideContainer.setAlpha(percentage);
	}

	private void setShowBookingUnavailablePercentage(float percentage) {
		mBookingUnavailableContainer.setAlpha(percentage);
	}

	private void setShowBookingPercentage(float percentage) {
		if (bookingWithGoogleWallet()) {
			mFormContainer.setTranslationX(percentage * mFormContainer.getWidth());
		}
		else {
			mCvvContainer.setTranslationX(percentage * -mCvvContainer.getWidth());
		}
		mBookingContainer.setTranslationX((1f - percentage) * mBookingContainer.getWidth());
	}

	private void setVisibilityState(CheckoutState state) {
		//TODO: This is a little out of control, we probably want to switch to a whitelisting method
		if (state == CheckoutState.OVERVIEW) {
			mFormContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.FORM_OPEN) {
			mFormContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.READY_FOR_CHECKOUT) {
			mFormContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.CVV) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mCvvContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mBookingContainer.setVisibility(View.GONE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.BOOKING) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mBookingContainer.setVisibility(View.VISIBLE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.INVISIBLE);
		}
		else if (state == CheckoutState.BOOKING_UNAVAILABLE) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mBookingContainer.setVisibility(View.GONE);
			mBookingUnavailableContainer.setVisibility(View.VISIBLE);
			mConfirmationContainer.setVisibility(View.INVISIBLE);
		}
		else if (state == CheckoutState.CONFIRMATION) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mBookingContainer.setVisibility(View.INVISIBLE);
			mBookingUnavailableContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.VISIBLE);
		}
	}

	private void doCreateTrip() {
		LineOfBusiness lob = getLob();
		if (lob == LineOfBusiness.FLIGHTS) {
			if (!mFlightBookingFrag.isDownloadingCreateTrip()
				&& TextUtils.isEmpty(Db.getTripBucket().getFlight().getFlightTrip().getItineraryNumber())
				&& Db.getTripBucket().getFlight().canBePurchased()) {
				mFlightCreateTripDownloadThrobber = ThrobberDialog
					.newInstance(getString(R.string.loading_flight_details));
				mFlightCreateTripDownloadThrobber.show(getFragmentManager(), TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG);
				mFlightBookingFrag.startDownload(FlightBookingState.CREATE_TRIP);
			}
		}
		else if (lob == LineOfBusiness.HOTELS) {
			if (!mHotelBookingFrag.isDownloadingHotelProduct()
				&& Db.getTripBucket().getHotel().getHotelProductResponse() == null
				&& Db.getTripBucket().getHotel().canBePurchased()) {
				mHotelProductDownloadThrobber = ThrobberDialog
					.newInstance(getString(R.string.calculating_taxes_and_fees));
				mHotelProductDownloadThrobber.show(getFragmentManager(), TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG);
				mHotelBookingFrag.startDownload(HotelBookingState.HOTEL_PRODUCT);
			}
		}
	}

	private void startBooking() {
		if (getLob() == LineOfBusiness.FLIGHTS) {
			doFlightBooking();
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			doHotelBooking();
		}
	}

	private void doFlightBooking() {
		mFlightBookingFrag.doBooking();
	}

	private void doHotelBooking() {
		mHotelBookingFrag.doBooking();
	}

	private void setFragmentState(CheckoutState state) {
		if (!isAdded()) {
			return;
		}

		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean checkoutFormsAvailable = state != CheckoutState.BOOKING_UNAVAILABLE;
		boolean slideToPurchaseAvailable = true;
		boolean isBookingUnvailable = state == CheckoutState.BOOKING_UNAVAILABLE;
		boolean cvvAvailable =
			state != CheckoutState.OVERVIEW;//If we are in cvv mode or are ready to enter it, we add cvv

		boolean mFlightConfAvailable = state == CheckoutState.CONFIRMATION && getLob() == LineOfBusiness.FLIGHTS;
		boolean mHotelConfAvailable = state == CheckoutState.CONFIRMATION && getLob() == LineOfBusiness.HOTELS;

		//if (mBucketFlightFrag != null && mBucketFlightFragStateListener != null) {
		//	mBucketFlightFrag.unRegisterStateListener(mBucketFlightFragStateListener);
		//}
		//if (mBucketHotelFrag != null && mBucketHotelFragStateListener != null) {
		//	mBucketHotelFrag.unRegisterStateListener(mBucketHotelFragStateListener);
		//}

		mCheckoutFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			checkoutFormsAvailable, FRAG_TAG_CHECKOUT_INFO, manager, transaction, this,
			R.id.checkout_forms_container, false);

		mSlideFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			slideToPurchaseAvailable, FRAG_TAG_SLIDE_TO_PURCHASE, manager, transaction, this,
			R.id.slide_container, true);

		mCvvFrag = FragmentAvailabilityUtils.setFragmentAvailability(cvvAvailable, FRAG_TAG_CVV,
			manager, transaction, this, R.id.cvv_container, false);

		mFlightConfFrag = FragmentAvailabilityUtils.setFragmentAvailability(mFlightConfAvailable,
			FRAG_TAG_CONF_FLIGHT, manager, transaction, this, R.id.confirmation_container, false);

		mHotelConfFrag = FragmentAvailabilityUtils.setFragmentAvailability(mHotelConfAvailable,
			FRAG_TAG_CONF_HOTEL, manager, transaction, this, R.id.confirmation_container, false);

		mBlurredBgFrag = FragmentAvailabilityUtils.setFragmentAvailability(true, FRAG_TAG_BLUR_BG,
			manager, transaction, this, R.id.blurred_dest_image_overlay, false);

		mBookingUnavailableFragment = FragmentAvailabilityUtils.setFragmentAvailability(isBookingUnvailable, FRAG_TAG_BOOKING_UNAVAILABLE,
			manager, transaction, this, R.id.booking_unavailable_container, true);

		transaction.commit();

		//if (mBucketFlightFrag != null) {
		//	if (mBucketFlightFragStateListener == null) {
		//		mBucketFlightFragStateListener = new TripBucketOrchestrator(mBucketFlightFrag);
		//	}
		//	mBucketFlightFrag.registerStateListener(mBucketFlightFragStateListener, false);
		//}

		//if (mBucketHotelFrag != null) {
		//	if (mBucketHotelFragStateListener == null) {
		//		mBucketHotelFragStateListener = new TripBucketOrchestrator(mBucketHotelFrag);
		//	}
		//	mBucketHotelFrag.registerStateListener(mBucketHotelFragStateListener, false);
		//}
	}

	/*
	 * CheckoutState ISTATEPROVIDER
	 */

	private StateListenerCollection<CheckoutState> mStateListeners = new StateListenerCollection<CheckoutState>();

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
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			return mCheckoutFragment;
		}
		else if (FRAG_TAG_SLIDE_TO_PURCHASE.equals(tag)) {
			return mSlideFragment;
		}
		else if (FRAG_TAG_CVV.equals(tag)) {
			return mCvvFrag;
		}
		else if (FRAG_TAG_CONF_FLIGHT.equals(tag)) {
			return mFlightConfFrag;
		}
		else if (FRAG_TAG_CONF_HOTEL.equals(tag)) {
			return mHotelConfFrag;
		}
		else if (FRAG_TAG_BLUR_BG.equals(tag)) {
			return mBlurredBgFrag;
		}
		else if (FRAG_TAG_BOOKING_UNAVAILABLE.equals(tag)) {
			return mBookingUnavailableFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			return TabletCheckoutFormsFragment.newInstance();
		}
		else if (FRAG_TAG_SLIDE_TO_PURCHASE.equals(tag)) {
			return TabletCheckoutSlideFragment.newInstance();
		}
		else if (FRAG_TAG_CVV.equals(tag)) {
			return CVVEntryFragment.newInstance();
		}
		else if (FRAG_TAG_CONF_FLIGHT.equals(tag)) {
			return new TabletFlightConfirmationFragment();
		}
		else if (FRAG_TAG_CONF_HOTEL.equals(tag)) {
			return new TabletHotelConfirmationFragment();
		}
		else if (FRAG_TAG_BLUR_BG.equals(tag)) {
			return ResultsBackgroundImageFragment.newInstance(getLob(), true);
		}
		else if (FRAG_TAG_BOOKING_UNAVAILABLE.equals(tag)) {
			return BookingUnavailableFragment.newInstance();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			TabletCheckoutFormsFragment f = (TabletCheckoutFormsFragment) frag;
			f.setLob(getLob());
		}
		else if (FRAG_TAG_BOOKING_UNAVAILABLE.equals(tag)) {
			BookingUnavailableFragment f = (BookingUnavailableFragment) frag;
			f.setLob(getLob());
		}
		else if (FRAG_TAG_SLIDE_TO_PURCHASE.equals(tag)) {
			TabletCheckoutSlideFragment f = (TabletCheckoutSlideFragment) frag;
			f.setLob(getLob());
			f.setPriceFromTripBucket();
		}
	}

	/*
	 * SlideToWidgetJB.ISlideToListener
	 *
	 * This manages the "slide to book hotel" animation. onSlideStart/Progress/AllTheWay/Abort
	 * callbacks come from the SlideToWidgetJB View. It's more of a two-step animation, which
	 * needs some clever coding here:
	 * 1. As the user is dragging his finger, we want to transition the fragments along with
	 * the user's finger. We have to calculate the percentage based on the number of pixels the
	 * user's finger has travelled.
	 * 2. After the user slides all the way, we want to transition the rest of the way. We'll just
	 * set up a ValueAnimator to do that.
	 */

	float mSlideProgress = 0f;

	@Override
	public void onSlideStart() {
		CheckoutState stateTwo = bookingWithGoogleWallet() ? CheckoutState.BOOKING : CheckoutState.CVV;
		startStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo);
	}

	@Override
	public void onSlideProgress(float pixels, float total) {
		CheckoutState stateTwo = bookingWithGoogleWallet() ? CheckoutState.BOOKING : CheckoutState.CVV;
		mSlideProgress = pixels / mSlideFragment.getView().getWidth();
		updateStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo, mSlideProgress);
	}

	@Override
	public void onSlideAllTheWay() {
		final CheckoutState stateTwo = bookingWithGoogleWallet() ? CheckoutState.BOOKING : CheckoutState.CVV;
		if (!BookingInfoUtils
			.migrateRequiredCheckoutDataToDbBillingInfo(getActivity(), getLob(), Db.getTravelers().get(0), true)) {
			//Somehow we don't have the information we need. This should be very rare, but it could happen.

			if (mSlideFragment != null) {
				mSlideFragment.resetSlider();
			}
			if (TextUtils.isEmpty(Db.getBillingInfo().getEmail())) {
				Ui.showToast(getActivity(), R.string.please_enter_a_valid_email_address);
			}
			else {
				//TODO: This shouldn't happen, but if it does we show a worthless toast.
				Ui.showToast(getActivity(), R.string.unknown);
			}

			//We clean up our transition and go back to ready for checkout.
			endStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo);
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, false);
		}
		else {
			//Our data is legit, lets complete the transition
			ValueAnimator anim = ValueAnimator.ofFloat(mSlideProgress, 1f);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					updateStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo,
						(Float) valueAnimator.getAnimatedValue());
					setShowReadyForCheckoutPercentage(1f - valueAnimator.getAnimatedFraction());
				}
			});
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animator) {
					endStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo);
					setCheckoutState(stateTwo, false);
				}
			});
			anim.start();
		}

	}

	@Override
	public void onSlideAbort() {
		CheckoutState stateTwo = bookingWithGoogleWallet() ? CheckoutState.BOOKING : CheckoutState.CVV;
		endStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo);
		// 3371: Don't try to finish this state transition if we're offscreen
		if (isAdded() && isResumed()) {
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, false);
		}
	}

	/*
	 * CVVEntyrFragmentListener
	 * @see com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener#onBook(java.lang.String)
	 */

	@Override
	public void onBook(String cvv) {
		Db.getBillingInfo().setSecurityCode(cvv);
		setCheckoutState(CheckoutState.BOOKING, true);
	}

	/*
	 * BookingUnavailableFragment listener
	 */

	@Override
	public void onTripBucketItemRemoved(LineOfBusiness lob) {
		BookingUnavailableFragmentListener listener = Ui.findFragmentListener(this, BookingUnavailableFragmentListener.class, false);
		if (listener != null) {
			listener.onTripBucketItemRemoved(lob);
		}
	}

	@Override
	public void onSelectNewTripItem(LineOfBusiness lob) {
		if (lob == LineOfBusiness.FLIGHTS) {
			NavUtils.restartFlightSearch(getActivity());
		}
		else {
			NavUtils.restartHotelSearch(getActivity());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener#checkoutInformationIsValid()
	 */

	@Override
	public void checkoutInformationIsValid() {
		mCheckoutInformationIsValid = true;
		if (mStateManager.getState() == CheckoutState.OVERVIEW) {
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
		}
		// CheckoutInfo has changed, so let's reset CVV text to start fresh.
		if (mCvvFrag != null) {
			mCvvFrag.resetCVVText();
		}
	}

	@Override
	public void checkoutInformationIsNotValid() {
		mCheckoutInformationIsValid = false;
		if (mStateManager.getState() == CheckoutState.READY_FOR_CHECKOUT) {
			setCheckoutState(CheckoutState.OVERVIEW, true);
		}
	}

	public boolean getCheckoutInformationIsValid() {
		return mCheckoutInformationIsValid;
	}

	@Override
	public void onBillingInfoChange() {
		if (mCvvFrag != null && mCvvFrag.isAdded()) {
			mCvvFrag.bind();
		}
	}

	@Override
	public void onLogout() {
		if (!User.isLoggedIn(getActivity())) {
			doCreateTrip();
		}
	}

	///////////////////////////////////
	/// Bookings events

	@Subscribe
	public void onStartBooking(Events.BookingDownloadStarted event) {
		// TODO do something?
	}

	@Subscribe
	public void onBookingResponse(Events.BookingDownloadResponse event) {
		Response results = event.response;
		if (results == null) {
			getCurrentBookingFragment().handleBookingErrorResponse(results, getLob());
		}
		else {
			if (!AndroidUtils.isRelease(getActivity())) {
				if (SettingUtils
					.get(getActivity(), R.string.preference_force_google_wallet_error, false)) {
					ServerError googleWalletError = new ServerError();
					googleWalletError.setCode("GOOGLE_WALLET_ERROR");
					results.addErrorToFront(googleWalletError);
				}
				if (SettingUtils.get(getActivity(), R.string.preference_force_passenger_category_error, false)) {
					ServerError passengerCategoryError = new ServerError();
					passengerCategoryError.setCode("INVALID_INPUT");
					passengerCategoryError.addExtra("field", "mainFlightPassenger.birthDate");
					results.addErrorToFront(passengerCategoryError);
				}
			}

			if (results instanceof FlightCheckoutResponse) {
				FlightCheckoutResponse response = (FlightCheckoutResponse) results;

				Db.getTripBucket().getFlight().setCheckoutResponse(response);
				// TODO save the TripBucket to disk for better persistence?
				AdTracker.trackFlightBooked();

				if (response == null || response.hasErrors()) {
					mFlightBookingFrag.handleBookingErrorResponse(response);
				}
				else {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.PURCHASED);
					Db.saveTripBucket(getActivity());
					setCheckoutState(CheckoutState.CONFIRMATION, true);
				}
			}
			// HotelBookingResponse
			else if (results instanceof HotelBookingResponse) {
				HotelBookingResponse response = (HotelBookingResponse) results;
				Property property = Db.getTripBucket().getHotel().getProperty();

				Db.getTripBucket().getHotel().setBookingResponse(response);
				AdTracker.trackHotelBooked();

				if (results == null || response.hasErrors()) {
					response.setProperty(property);
					mHotelBookingFrag.handleBookingErrorResponse(response);
				}
				else {
					response.setProperty(property);
					Db.getTripBucket().getHotel().setState(TripBucketItemState.PURCHASED);
					Db.saveTripBucket(getActivity());
					WalletUtils.unbindAllWalletDataFromBillingInfo(Db.getBillingInfo());
					setCheckoutState(CheckoutState.CONFIRMATION, true);
				}
			}
		}
	}

	private void startCreateTripDownload() {
		if (mCreateTripDownloadThrobber == null) {
			mCreateTripDownloadThrobber = ThrobberDialog
				.newInstance(getString(R.string.spinner_text_hotel_create_trip));
			mCreateTripDownloadThrobber.show(getFragmentManager(), TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG);
		}
		else {
			mCreateTripDownloadThrobber.show(getFragmentManager(), TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG);
		}
		mHotelBookingFrag.startDownload(HotelBookingState.CREATE_TRIP);
	}

	private void dismissLoadingDialogs() {
		mHotelProductDownloadThrobber = Ui.findSupportFragment((FragmentActivity) getActivity(),
			TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG);
		if (mHotelProductDownloadThrobber != null && mHotelProductDownloadThrobber.isAdded()) {
			mHotelProductDownloadThrobber.dismiss();
		}
		mCreateTripDownloadThrobber = Ui.findSupportFragment((FragmentActivity) getActivity(),
			TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG);
		if (mCreateTripDownloadThrobber != null && mCreateTripDownloadThrobber.isAdded()) {
			mCreateTripDownloadThrobber.dismiss();
		}
		mFlightCreateTripDownloadThrobber = Ui.findSupportFragment((FragmentActivity) getActivity(),
			TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG);
		if (mFlightCreateTripDownloadThrobber != null && mFlightCreateTripDownloadThrobber.isAdded()) {
			mFlightCreateTripDownloadThrobber.dismiss();
		}
	}

	///////////////////////////////////
	/// Booking error events

	@Subscribe
	public void onRetryUnhandledException(Events.UnhandledErrorDialogRetry event) {
		LineOfBusiness lob = getLob();
		if (lob == LineOfBusiness.FLIGHTS) {
			doFlightBooking();
		}
		else if (lob == LineOfBusiness.HOTELS) {
			doHotelBooking();
		}
		else {
			// Maybe we should crash if the LOB is set to something
			// weird. For now, let's just take them to the overview.
			setCheckoutState(CheckoutState.OVERVIEW, true);
		}
	}

	// We're not sure exactly what we want to do with call support on tablet.
	// Right now, we just display the phone number, but it's ugly.
	@Subscribe
	public void onCallCustomerSupport(Events.UnhandledErrorDialogCallCustomerSupport event) {
		SocialUtils.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
	}

	@Subscribe
	public void onCancelUnhandledException(Events.UnhandledErrorDialogCancel event) {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (bookingWithGoogleWallet()) {
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
		}
		else {
			setCheckoutState(CheckoutState.CVV, true);
		}
	}

	@Subscribe
	public void onHotelProductDownloadSuccess(Events.HotelProductDownloadSuccess event) {
		dismissLoadingDialogs();
		startCreateTripDownload();
	}

	@Subscribe
	public void onCreateTripDownloadSuccess(Events.CreateTripDownloadSuccess event) {
		dismissLoadingDialogs();
		BookingInfoUtils.populatePaymentDataFromUser(getActivity(), getLob());
		mCheckoutFragment.onCheckoutDataUpdated();
	}

	@Subscribe
	public void onCreateTripDownloadError(Events.CreateTripDownloadError event) {
		dismissLoadingDialogs();
	}

	@Subscribe
	public void onCreateTripDownloadRetry(Events.CreateTripDownloadRetry event) {
		if (getLob() == LineOfBusiness.HOTELS) {
			startCreateTripDownload();
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			doCreateTrip();
		}
	}

	@Subscribe
	public void onCreateTripDownloadRetryCancel(Events.CreateTripDownloadRetryCancel event) {
		if (getActivity() != null) {
			getActivity().finish();
		}
	}

	@Subscribe
	public void onBookingErrorDialogClick(Events.SimpleCallBackDialogOnClick event) {
		setCheckoutState(CheckoutState.OVERVIEW, true);
		if (bookingWithGoogleWallet()) {
			return;
		}
		int callBackId = event.callBackId;
		switch (callBackId) {
		case SimpleCallbackDialogFragment.CODE_EXPIRED_CC:
		case SimpleCallbackDialogFragment.CODE_INVALID_CC:
		case SimpleCallbackDialogFragment.CODE_INVALID_POSTALCODE:
		case SimpleCallbackDialogFragment.CODE_INVALID_PAYMENT:
		case SimpleCallbackDialogFragment.CODE_NAME_ONCARD_MISMATCH:
			setCheckoutState(CheckoutState.FORM_OPEN, true);
			mCheckoutFragment.setState(CheckoutFormState.EDIT_PAYMENT, true);
			break;
		case SimpleCallbackDialogFragment.CODE_INVALID_PHONENUMBER:
			setCheckoutState(CheckoutState.FORM_OPEN, true);
			mCheckoutFragment.openTravelerEntry(0);
			break;
		case SimpleCallbackDialogFragment.CODE_INVALID_MINOR:
		case SimpleCallbackDialogFragment.CODE_MINOR:
		default:
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
			break;
		}
	}

	@Subscribe
	public void onBookingErrorDialogCancel(Events.SimpleCallBackDialogOnCancel event) {
		// If we're booking via wallet, back out; otherwise sit on CVV screen
		if (bookingWithGoogleWallet()) {
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
		}
		else {
			setCheckoutState(CheckoutState.CVV, true);
		}
	}

	@Subscribe
	public void onBookingErrorTripBooked(Events.BookingResponseErrorTripBooked event) {
		// Send the user to their confirmation screen, since they ended up booking their trip already.
		setCheckoutState(CheckoutState.CONFIRMATION, true);
	}

	@Subscribe
	public void onBookingResponseErrorCVV(Events.BookingResponseErrorCVV event) {
		mCvvFrag.setCvvErrorMode(true);
		setCheckoutState(CheckoutState.CVV, true);
	}

	@Subscribe
	public void onBookNext(Events.BookingConfirmationBookNext event) {
		if (event.nextItem != null) {
			setLob(event.nextItem);
			setCheckoutState(CheckoutState.OVERVIEW, false);
		}
	}


	//////////////////////////////////////////////////////////////////////////
	// BirthDateInvalidDialog

	@Subscribe
	public void onBirthDateInvalidEditSearch(Events.BirthDateInvalidEditSearch event) {
		Intent goToSearch = new Intent(getActivity(), TabletResultsActivity.class);
		goToSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(goToSearch);
	}

	@Subscribe
	public void onBirthDateInvalidEditTraveler(Events.BirthDateInvalidEditTraveler event) {
		setCheckoutState(CheckoutState.OVERVIEW, true);
	}

	///////////////////////////////////
	// ISlideToPurchaseSizeProvider
	// The forms container needs to know about the slide to purchase container to size its content correctly.

	@Override
	public View getSlideToPurchaseContainer() {
		return mSlideContainer;
	}

	///////////////////////////////////
	// IAcceptingListenersListener

	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (frag == mCheckoutFragment) {
			if (acceptingListener) {
				mCheckoutFragment.registerStateListener(mCheckoutFormStateListenerSlide, true);
				mCheckoutFragment.registerStateListener(mCheckoutFormStateListenerEditPayment, true);
				mCheckoutFragment.registerStateListener(mCheckoutFormStateListenerEditTraveler, true);
				mCheckoutFragment.registerStateListener(mCheckoutFormStateListenerAny, true);
			}
			else {
				mCheckoutFragment.unRegisterStateListener(mCheckoutFormStateListenerSlide);
				mCheckoutFragment.unRegisterStateListener(mCheckoutFormStateListenerEditPayment);
				mCheckoutFragment.unRegisterStateListener(mCheckoutFormStateListenerEditTraveler);
				mCheckoutFragment.unRegisterStateListener(mCheckoutFormStateListenerAny);
			}

			// These wires run deeeeep
			IAcceptingListenersListener readyForListeners = Ui.findFragmentListener(this, IAcceptingListenersListener.class, false);
			if (readyForListeners != null) {
				readyForListeners.acceptingListenersUpdated(mCheckoutFragment, acceptingListener);
			}
		}
	}

	///////////////////////////////////
	// CheckoutFormState listeners
	// Hide/show the slide to checkout when required

	private StateListenerHelper<CheckoutFormState> mCheckoutFormStateListenerSlide = new StateListenerHelper<CheckoutFormState>() {
		private boolean mStartReacted = false;
		private boolean mValidAtStart = false;

		@Override
		public void onStateTransitionStart(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			mValidAtStart = mCheckoutFragment != null && mCheckoutFragment.hasValidCheckoutInfo();
			if (stateIsReadyForCheckout()) {
				setShowReadyForCheckoutPercentage(!mValidAtStart || stateOne.isOpen() ? 0f : 1f);
				mStartReacted = true;
			}
			else {
				mStartReacted = false;
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutFormState stateOne, CheckoutFormState stateTwo, float percentage) {
			if (mValidAtStart && mStartReacted && stateIsReadyForCheckout()) {
				setShowReadyForCheckoutPercentage(stateOne.isOpen() ? percentage : 1f - percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutFormState stateOne, CheckoutFormState stateTwo) {

		}

		@Override
		public void onStateFinalized(CheckoutFormState state) {
			if (stateIsReadyForCheckout()) {
				if (!state.isOpen()) {
					if (mCheckoutFragment != null && !mCheckoutFragment.hasValidCheckoutInfo()) {
						//So our form is closed and our checkout data is no longer valid, lets be sure
						//to set the proper state.
						setCheckoutState(CheckoutState.OVERVIEW, false);
					}
					else {
						setShowReadyForCheckoutPercentage(1f);
					}
				}
				else {
					setShowReadyForCheckoutPercentage(0f);
				}
			}
			else if (mStartReacted) {
				//If we reacted at the start, but we aren't reacting here, lets be safe and reset the state.
				setCheckoutState(mStateManager.getState(), false);
			}
			mStartReacted = false;
		}

		private boolean stateIsReadyForCheckout() {
			return mStateManager.getState() == CheckoutState.READY_FOR_CHECKOUT;
		}
	};

	// Hide/show the trip bucket and actionbar when appropriate
	private ISingleStateListener mCheckoutFormStateListenerAlpha = new ISingleStateListener() {
		View mTripBucketButtonContainer;

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mTripBucketButtonContainer = Ui.findView(getView(), R.id.trip_bucket_show_hide_container);
			if (mTripBucketButtonContainer != null) {
				mTripBucketButtonContainer.setAlpha(isReversed ? 0f : 1f);
			}

			if (!isReversed) {
				getActivity().getActionBar().hide();
			}
			else {
				getActivity().getActionBar().show();
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			// Show/hide trip bucket button in portrait
			if (mTripBucketButtonContainer != null) {
				mTripBucketButtonContainer.setAlpha(1f - percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			mTripBucketButtonContainer = Ui.findView(getView(), R.id.trip_bucket_show_hide_container);
			if (mTripBucketButtonContainer != null) {
				mTripBucketButtonContainer.setAlpha(isReversed ? 1f : 0f);
			}

			if (!isReversed) {
				getActivity().getActionBar().hide();
			}
			else {
				getActivity().getActionBar().show();
			}
		}
	};

	private SingleStateListener mCheckoutFormStateListenerEditPayment = new SingleStateListener(
		CheckoutFormState.OVERVIEW, CheckoutFormState.EDIT_PAYMENT, true, mCheckoutFormStateListenerAlpha);

	private SingleStateListener mCheckoutFormStateListenerEditTraveler = new SingleStateListener(
		CheckoutFormState.OVERVIEW, CheckoutFormState.EDIT_TRAVELER, true, mCheckoutFormStateListenerAlpha);

	private StateListenerHelper<CheckoutFormState> mCheckoutFormStateListenerAny = new StateListenerHelper<CheckoutFormState>() {
		@Override
		public void onStateTransitionStart(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			if (!stateOne.isOpen() && stateTwo.isOpen()) {
				startStateTransition(mStateManager.getState(), CheckoutState.FORM_OPEN);
			}
			if (stateOne.isOpen() && !stateTwo.isOpen()) {
				startStateTransition(CheckoutState.FORM_OPEN, getOverviewState());
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutFormState stateOne, CheckoutFormState stateTwo, float percentage) {
			if (!stateOne.isOpen() && stateTwo.isOpen()) {
				updateStateTransition(mStateManager.getState(), CheckoutState.FORM_OPEN, percentage);
			}
			if (stateOne.isOpen() && !stateTwo.isOpen()) {
				updateStateTransition(CheckoutState.FORM_OPEN, getOverviewState(), percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			if (!stateOne.isOpen() && stateTwo.isOpen()) {
				endStateTransition(mStateManager.getState(), CheckoutState.FORM_OPEN);
			}
			if (stateOne.isOpen() && !stateTwo.isOpen()) {
				endStateTransition(CheckoutState.FORM_OPEN, getOverviewState());
			}
		}

		@Override
		public void onStateFinalized(CheckoutFormState state) {
			if (state.isOpen()) {
				setCheckoutState(CheckoutState.FORM_OPEN, false);
			}
			else {
				CheckoutState currentState = mStateManager.getState();
				if (currentState != getOverviewState() && !currentState.validCheckoutInfoHasBeenEntered()) {
					setCheckoutState(getOverviewState(), false);
				}
			}
		}

		private CheckoutState getOverviewState() {
			return mCheckoutInformationIsValid ? CheckoutState.READY_FOR_CHECKOUT : CheckoutState.OVERVIEW;
		}
	};


	@Subscribe
	public void onBookingUnavailable(Events.BookingUnavailable event) {
		dismissLoadingDialogs();

		if (getLob() == LineOfBusiness.FLIGHTS) {
			Db.getTripBucket().getFlight().setState(TripBucketItemState.BOOKING_UNAVAILABLE);
		}
		else {
			Db.getTripBucket().getHotel().setState(TripBucketItemState.BOOKING_UNAVAILABLE);
		}
		Db.saveTripBucket(getActivity());
		OmnitureTracking.trackItemSoldOutOnCheckoutLink(getActivity(), getLob());
		setCheckoutState(CheckoutState.BOOKING_UNAVAILABLE, true);
	}

	@Subscribe
	public void onTripItemExpired(Events.TripItemExpired event) {
		dismissLoadingDialogs();
		if (event.lineOfBusiness == LineOfBusiness.FLIGHTS) {
			Db.getTripBucket().getFlight().setState(TripBucketItemState.EXPIRED);
		}
		else {
			Db.getTripBucket().getHotel().setState(TripBucketItemState.EXPIRED);
		}

		setCheckoutState(CheckoutState.BOOKING_UNAVAILABLE, true);
	}
}
