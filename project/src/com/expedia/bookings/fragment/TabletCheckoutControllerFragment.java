package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.LocalDate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.CheckoutFormState;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.fragment.FlightBookingFragment.FlightBookingState;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.HotelBookingFragment.HotelBookingState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.otto.Events.CouponApplyDownloadSuccess;
import com.expedia.bookings.otto.Events.CouponRemoveDownloadSuccess;
import com.expedia.bookings.otto.Events.CreateTripDownloadError;
import com.expedia.bookings.otto.Events.CreateTripDownloadRetry;
import com.expedia.bookings.otto.Events.CreateTripDownloadRetryCancel;
import com.expedia.bookings.otto.Events.CreateTripDownloadSuccess;
import com.expedia.bookings.otto.Events.HotelProductDownloadSuccess;
import com.expedia.bookings.otto.Events.HotelProductRateUp;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.SlideToWidgetJB;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.squareup.otto.Subscribe;

/**
 * TabletCheckoutControllerFragment: designed for tablet checkout 2014
 * This controls all the fragments relating to tablet checkout
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletCheckoutControllerFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutState>, IFragmentAvailabilityProvider, CVVEntryFragmentListener,
	CheckoutInformationListener, SlideToWidgetJB.ISlideToListener,
	TabletCheckoutFormsFragment.ISlideToPurchaseSizeProvider, IAcceptingListenersListener {

	private static final String STATE_CHECKOUT_STATE = "STATE_CHECKOUT_STATE";

	private static final String FRAG_TAG_BUCKET_FLIGHT = "FRAG_TAG_BUCKET_FLIGHT";
	private static final String FRAG_TAG_BUCKET_HOTEL = "FRAG_TAG_BUCKET_HOTEL";
	private static final String FRAG_TAG_CHECKOUT_INFO = "FRAG_TAG_CHECKOUT_INFO";
	private static final String FRAG_TAG_SLIDE_TO_PURCHASE = "FRAG_TAG_SLIDE_TO_PURCHASE";
	private static final String FRAG_TAG_CVV = "FRAG_TAG_CVV";
	private static final String FRAG_TAG_CONF_FLIGHT = "FRAG_TAG_CONF_FLIGHT";
	private static final String FRAG_TAG_CONF_HOTEL = "FRAG_TAG_CONF_HOTEL";
	private static final String FRAG_TAG_BLUR_BG = "FRAG_TAG_BLUR_BG";

	//Containers
	private ViewGroup mRootC;
	private ScrollView mBucketScrollContainer;
	private ViewGroup mBucketHotelContainer;
	private ViewGroup mBucketFlightContainer;
	private ViewGroup mSlideAndFormContainer;
	private ViewGroup mSlideContainer;
	private ViewGroup mFormContainer;
	private ViewGroup mCvvContainer;
	private ViewGroup mBookingContainer;
	private ViewGroup mConfirmationContainer;
	private ViewGroup mBlurredDestImageOverlay;

	//Views
	private TextView mBucketDateRange;

	//frags
	private TripBucketFlightFragment mBucketFlightFrag;
	private TripBucketHotelFragment mBucketHotelFrag;
	private TabletCheckoutFormsFragment mCheckoutFragment;
	private TabletCheckoutSlideFragment mSlideFragment;
	private CVVEntryFragment mCvvFrag;
	private FlightBookingFragment mFlightBookingFrag;
	private HotelBookingFragment mHotelBookingFrag;
	private TabletFlightConfirmationFragment mFlightConfFrag;
	private TabletHotelConfirmationFragment mHotelConfFrag;
	private ResultsBackgroundImageFragment mBlurredBgFrag;

	private static final int DIALOG_CALLBACK_INVALID_CC = 1;
	private static final int DIALOG_CALLBACK_EXPIRED_CC = 2;
	private static final int DIALOG_CALLBACK_MINOR = 3;

	private static final String TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG = "TAG_HOTEL_PRODUCT_DOWNLOADING_DIALOG";
	private static final String TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG = "TAG_HOTEL_CREATE_TRIP_DOWNLOADING_DIALOG";
	private static final String TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG = "TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG";

	private static final String INSTANCE_DONE_LOADING_PRICE_CHANGE = "INSTANCE_DONE_LOADING_PRICE_CHANGE";
	private static final String INSTANCE_FLIGHT_TRIP_ERROR = "INSTANCE_FLIGHT_TRIP_ERROR";

	private boolean mIsDoneLoadingPriceChange = false;
	private boolean mIsFlightTripDone = false;
	private boolean mAnimateState = false;

	//vars
	private StateManager<CheckoutState> mStateManager = new StateManager<CheckoutState>(
		CheckoutState.OVERVIEW, this);

	private TripBucketOrchestrator mBucketFlightFragStateListener;
	private TripBucketOrchestrator mBucketHotelFragStateListener;

	private ThrobberDialog mHotelProductDownloadThrobber;
	private ThrobberDialog mCreateTripDownloadThrobber;
	private ThrobberDialog mFlightCreateTripDownloadThrobber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We should ALWAYS have an instance of the HotelBookingFragment and FlightBookingFragment.
		// Hence we necessarily don't have to use FragmentAvailabilityUtils.setFragmentAvailability
		mHotelBookingFrag = Ui.findSupportFragment((FragmentActivity) getActivity(), HotelBookingFragment.TAG);

		if (mHotelBookingFrag == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			mHotelBookingFrag = new HotelBookingFragment();
			ft.add(mHotelBookingFrag, HotelBookingFragment.TAG);
			ft.commit();
		}

		mFlightBookingFrag = Ui.findSupportFragment((FragmentActivity) getActivity(), FlightBookingFragment.TAG);

		if (mFlightBookingFrag == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			mFlightBookingFrag = new FlightBookingFragment();
			ft.add(mFlightBookingFrag, FlightBookingFragment.TAG);
			ft.commit();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_controller, null, false);

		Ui.findView(mRootC, R.id.blurred_dest_image_overlay);

		mBucketScrollContainer = Ui.findView(mRootC, R.id.trip_bucket_scroll);
		mBucketHotelContainer = Ui.findView(mRootC, R.id.bucket_hotel_frag_container);
		mBucketFlightContainer = Ui.findView(mRootC, R.id.bucket_flight_frag_container);

		mSlideAndFormContainer = Ui.findView(mRootC, R.id.checkout_forms_and_slide_container);
		mFormContainer = Ui.findView(mRootC, R.id.checkout_forms_container);
		mSlideContainer = Ui.findView(mRootC, R.id.slide_container);
		mCvvContainer = Ui.findView(mRootC, R.id.cvv_container);
		mBookingContainer = Ui.findView(mRootC, R.id.booking_container);
		mConfirmationContainer = Ui.findView(mRootC, R.id.confirmation_container);

		mBucketDateRange = Ui.findView(mRootC, R.id.trip_date_range);
		String dateRange;
		if (getLob() == LineOfBusiness.FLIGHTS) {
			FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
			Calendar depDate = trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
			Calendar retDate = trip.getLeg(trip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
			long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
			long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

			dateRange = DateUtils.formatDateRange(getActivity(), start, end,
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_WEEKDAY);
		}
		else {
			// Hotels
			LocalDate checkIn = Db.getHotelSearch().getSearchParams().getCheckInDate();
			LocalDate checkOut = Db.getHotelSearch().getSearchParams().getCheckOutDate();
			dateRange = JodaUtils.formatDateRange(getActivity(), checkIn, checkOut,
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_WEEKDAY);
		}
		mBucketDateRange.setText(dateRange);

		if (savedInstanceState != null) {
			mStateManager.setDefaultState(CheckoutState.valueOf(savedInstanceState.getString(
				STATE_CHECKOUT_STATE,
				CheckoutState.OVERVIEW.name())));
			mIsDoneLoadingPriceChange = savedInstanceState.getBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE);
			mIsFlightTripDone = savedInstanceState.getBoolean(INSTANCE_FLIGHT_TRIP_ERROR);
		}

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutState>(), false);

		mTripBucketItemViews = new ViewGroup[] {
			mBucketHotelContainer,
			mBucketFlightContainer,
		};

		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUT_STATE, mStateManager.getState().name());
		outState.putBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE, mIsDoneLoadingPriceChange);
		outState.putBoolean(INSTANCE_FLIGHT_TRIP_ERROR, mIsFlightTripDone);
	}

	@Override
	public void onStart() {
		super.onStart();
		setCheckoutState(mStateManager.getState(), false);
	}

	@Override
	public void onResume() {
		super.onResume();

		Events.register(this);

		mBackManager.registerWithParent(this);
		//TODO - There might be a better way of determining this?
		if (bookingWithGoogleWallet() && mStateManager.getState() != CheckoutState.BOOKING) {
			setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, true);
		}
		checkForAddedTrips();
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
			}
			if (Db.getTripBucket().getFlight() != null) {
				Db.getTripBucket().getFlight().setHasPriceChanged(false);
			}
		}
	}

	private void checkForAddedTrips() {
		boolean hasHotel = Db.getTripBucket().getHotel() != null;
		mBucketHotelContainer.setVisibility(hasHotel ? View.VISIBLE : View.GONE);

		boolean hasFlight = Db.getTripBucket().getFlight() != null;
		mBucketFlightContainer.setVisibility(hasFlight ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCheckoutFragment.onActivityResult(requestCode, resultCode, data);
		if (getCurrentBookingFragment() != null) {
			getCurrentBookingFragment().onActivityResult(requestCode, resultCode, data);
		}
	}

	/*
	 * LobableFragment
	 */

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mCheckoutFragment != null) {
			mCheckoutFragment.setLob(lob);
		}
		if (mSlideFragment != null) {
			mSlideFragment.setLob(lob);
		}
	}

	/*
	 * GETTERS/SETTERS
	 */

	public void setCheckoutState(CheckoutState state, boolean animate) {
		mAnimateState = animate;
		mStateManager.setState(state, animate);
	}

	private boolean bookingWithGoogleWallet() {
		if (getCurrentBookingFragment() != null) {
			return getCurrentBookingFragment().willBookViaGoogleWallet();
		}
		return false;
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
			else if (stateOne == CheckoutState.CVV && stateTwo == CheckoutState.READY_FOR_CHECKOUT) {
				setShowReadyForCheckoutPercentage(0f);
				mSlideContainer.setVisibility(View.VISIBLE);
				mFormContainer.setVisibility(View.VISIBLE);
				mSlideAndFormContainer.setVisibility(View.VISIBLE);
				if (Db.getTripBucket().getFlight() != null
					&& Db.getTripBucket().getFlight().getState() == TripBucketItemState.SHOWING_PRICE_CHANGE) {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.EXPANDED);
				}
				if (Db.getTripBucket().getHotel() != null
					&& Db.getTripBucket().getHotel().getState() == TripBucketItemState.SHOWING_PRICE_CHANGE) {
					Db.getTripBucket().getHotel().setState(TripBucketItemState.EXPANDED);
				}
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
				setShowBookingPercentage(percentage);
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
			updateBucketForState(state, mAnimateState);

			if (state == CheckoutState.OVERVIEW) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(0f);
				doCreateTrip();
			}
			else if (state == CheckoutState.READY_FOR_CHECKOUT) {
				setShowCvvPercentage(0f);
				setShowReadyForCheckoutPercentage(1f);
			}
			else if (state == CheckoutState.CVV) {
				setShowCvvPercentage(1f);
				setShowReadyForCheckoutPercentage(0f);
			}
			else if (state == CheckoutState.BOOKING) {
				setShowBookingPercentage(1f);
				if (bookingWithGoogleWallet()) {
					setShowReadyForCheckoutPercentage(0f);
				}
				startBooking();
			}
			else if (state == CheckoutState.CONFIRMATION) {
				setShowConfirmationPercentage(1f);
			}
		}
	};

	private List<TripBucketItemFragment> mTripBucketItemFragments;
	private ViewGroup[] mTripBucketItemViews;

	private class TripBucketOrchestrator extends StateListenerHelper<TripBucketItemState> {
		private TripBucketItemFragment mFragment;
		private boolean mForward = true;
		private int mShift = 0;
		private int mExpandedPosition = 0;
		private int mItemToExpandPosition = 0;

		public TripBucketOrchestrator(TripBucketItemFragment frag) {
			mFragment = frag;
		}

		@Override
		public void onStateTransitionStart(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			// Ignore
		}

		@Override
		public void onStateTransitionUpdate(TripBucketItemState stateOne, TripBucketItemState stateTwo,
			float percentage) {
			if (stateTwo == TripBucketItemState.EXPANDED || stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {
				for (int i = 0; i < mTripBucketItemFragments.size(); i++) {
					TripBucketItemFragment f = mTripBucketItemFragments.get(i);
					if (f.getState() == TripBucketItemState.EXPANDED
						|| f.getState() == TripBucketItemState.SHOWING_PRICE_CHANGE) {
						mExpandedPosition = i;
					}
					if (f == mFragment) {
						mItemToExpandPosition = i;
					}
				}

				mForward = mExpandedPosition < mItemToExpandPosition;
				if (mForward) {
					mShift = mTripBucketItemFragments.get(mExpandedPosition).getExpandedHeight();
					mShift += mTripBucketItemFragments.get(mExpandedPosition).getPriceChangeHeight();
				}
				else {
					mShift = mTripBucketItemFragments.get(mItemToExpandPosition).getExpandedHeight();
					mShift += mTripBucketItemFragments.get(mItemToExpandPosition).getPriceChangeHeight();
				}

				float amount;
				if (mForward) {
					amount = -mShift * percentage;
				}
				else {
					amount = mShift * (1.0f - percentage);
				}

				int start = Math.min(mExpandedPosition, mItemToExpandPosition);
				int end = Math.max(mExpandedPosition, mItemToExpandPosition);
				for (int i = start + 1; i <= end; i++) {
					if (mTripBucketItemViews[i] != null) {
						if (mForward) {
							mTripBucketItemViews[i].setTranslationY(amount);
						}
						else {
							mTripBucketItemViews[i].setTranslationY(-amount);
						}
					}
				}
			}
		}

		@Override
		public void onStateTransitionEnd(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			if (stateTwo == TripBucketItemState.EXPANDED) {
				for (ViewGroup view : mTripBucketItemViews) {
					view.setTranslationY(0);
				}
			}
		}

		@Override
		public void onStateFinalized(TripBucketItemState state) {
			// Ignore
		}
	}

	;

	private void setShowCvvPercentage(float percentage) {
		mFormContainer.setTranslationX(percentage * mFormContainer.getWidth());
		mCvvContainer.setTranslationX((1f - percentage) * -mCvvContainer.getWidth());
	}

	private void setShowReadyForCheckoutPercentage(float percentage) {
		mSlideContainer.setAlpha(percentage);
	}

	private void setShowConfirmationPercentage(float percentage) {
		mBucketScrollContainer.setTranslationX((1f - percentage) * -mBucketScrollContainer.getWidth());
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
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.GONE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideAndFormContainer.setVisibility(View.VISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.READY_FOR_CHECKOUT) {
			mFormContainer.setVisibility(View.VISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideAndFormContainer.setVisibility(View.VISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.CVV) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.VISIBLE);
			mSlideContainer.setVisibility(View.INVISIBLE);
			mSlideAndFormContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.GONE);
			mConfirmationContainer.setVisibility(View.GONE);
		}
		else if (state == CheckoutState.BOOKING) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.INVISIBLE);
			mSlideAndFormContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.VISIBLE);
			mConfirmationContainer.setVisibility(View.INVISIBLE);
		}
		else if (state == CheckoutState.CONFIRMATION) {
			mFormContainer.setVisibility(View.INVISIBLE);
			mBucketScrollContainer.setVisibility(View.VISIBLE);
			mCvvContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.INVISIBLE);
			mSlideAndFormContainer.setVisibility(View.INVISIBLE);
			mBookingContainer.setVisibility(View.INVISIBLE);
			mConfirmationContainer.setVisibility(View.VISIBLE);
		}
	}

	private void updateBucketForState(CheckoutState state, boolean animate) {

		//SETUP Db.getTripBucket() state
		if (state == CheckoutState.CONFIRMATION) {
			if (Db.getTripBucket().getFlight() != null && getLob() == LineOfBusiness.FLIGHTS) {
				Db.getTripBucket().getFlight().setState(TripBucketItemState.CONFIRMATION);
				if (Db.getTripBucket().getHotel() != null
					&& Db.getTripBucket().getHotel().getState() != TripBucketItemState.PURCHASED) {
					Db.getTripBucket().getHotel().setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
				}
			}
			else if (Db.getTripBucket().getHotel() != null) {
				Db.getTripBucket().getHotel().setState(TripBucketItemState.CONFIRMATION);
				if (Db.getTripBucket().getFlight() != null
					&& Db.getTripBucket().getFlight().getState() != TripBucketItemState.PURCHASED) {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
				}
			}
		}
		// When in booking state let's disable "Book" button for existing trips in the bucket.
		else if (state == CheckoutState.BOOKING) {
			if (getLob() == LineOfBusiness.FLIGHTS) {
				if (Db.getTripBucket().getHotel() != null
					&& Db.getTripBucket().getHotel().getState() == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
					Db.getTripBucket().getHotel().setState(TripBucketItemState.DISABLED);
				}
			}
			else {
				if (Db.getTripBucket().getFlight() != null
					&& Db.getTripBucket().getFlight().getState() == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.DISABLED);
				}
			}
		}
		else {
			if (getLob() == LineOfBusiness.FLIGHTS) {
				if (Db.getTripBucket().getHotel() != null
					&& Db.getTripBucket().getHotel().getState() == TripBucketItemState.CONFIRMATION) {
					Db.getTripBucket().getHotel().setState(TripBucketItemState.PURCHASED);
				}
				if (Db.getTripBucket().getFlight() != null) {
					if (Db.getTripBucket().getFlight().hasPriceChanged()
						// Let's not show the price change notification when state changes
						&& (state != CheckoutState.READY_FOR_CHECKOUT && state != CheckoutState.CVV)) {
						Db.getTripBucket().getFlight().setState(TripBucketItemState.SHOWING_PRICE_CHANGE);
					}
					else {
						Db.getTripBucket().getFlight().setState(TripBucketItemState.EXPANDED);
					}
				}
				if (Db.getTripBucket().getHotel() != null
					&& Db.getTripBucket().getHotel().getState() != TripBucketItemState.PURCHASED) {
					Db.getTripBucket().getHotel().setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
				}
			}
			else {
				if (Db.getTripBucket().getFlight() != null
					&& Db.getTripBucket().getFlight().getState() == TripBucketItemState.CONFIRMATION) {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.PURCHASED);
				}
				if (Db.getTripBucket().getHotel() != null) {
					if (Db.getTripBucket().getHotel().hasPriceChanged()
						// Let's not show the price change notification when state changes
						&& (state != CheckoutState.READY_FOR_CHECKOUT && state != CheckoutState.CVV)) {
						Db.getTripBucket().getHotel().setState(TripBucketItemState.SHOWING_PRICE_CHANGE);
					}
					else {
						Db.getTripBucket().getHotel().setState(TripBucketItemState.EXPANDED);
					}
				}
				if (Db.getTripBucket().getFlight() != null
					&& Db.getTripBucket().getFlight().getState() != TripBucketItemState.PURCHASED) {
					Db.getTripBucket().getFlight().setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
				}
			}
		}

		//Apply state to frags
		if (Db.getTripBucket().getFlight() != null) {
			mBucketFlightFrag.setState(Db.getTripBucket().getFlight().getState(), animate);
		}
		if (Db.getTripBucket().getHotel() != null) {
			mBucketHotelFrag.setState(Db.getTripBucket().getHotel().getState(), animate);
		}

	}

	private void doCreateTrip() {
		LineOfBusiness lob = getLob();
		if (lob == LineOfBusiness.FLIGHTS) {
			//mBucketFlightFrag.doCreateTrip();
			getFragmentManager().executePendingTransactions();

			if (!mFlightBookingFrag.isDownloadingCreateTrip()
				&& TextUtils.isEmpty(Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber())
				&& !mIsFlightTripDone) {
				mFlightCreateTripDownloadThrobber = ThrobberDialog
					.newInstance(getString(R.string.loading_flight_details));
				mFlightCreateTripDownloadThrobber.show(getFragmentManager(), TAG_FLIGHT_CREATE_TRIP_DOWNLOADING_DIALOG);
				mFlightBookingFrag.startDownload(FlightBookingState.CREATE_TRIP);
			}
		}
		else if (lob == LineOfBusiness.HOTELS) {
			getFragmentManager().executePendingTransactions();

			if (!mHotelBookingFrag.isDownloadingHotelProduct() && !mIsDoneLoadingPriceChange) {
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
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction

		FragmentTransaction transaction = manager.beginTransaction();

		boolean flightBucketItemAvailable = Db.getTripBucket().getFlight() != null;
		boolean hotelBucketItemAvailable = Db.getTripBucket().getHotel() != null;
		boolean checkoutFormsAvailable = true;
		boolean slideToPurchaseAvailable = true;
		boolean cvvAvailable =
			state != CheckoutState.OVERVIEW;//If we are in cvv mode or are ready to enter it, we add cvv

		boolean mFlightConfAvailable = state == CheckoutState.CONFIRMATION && getLob() == LineOfBusiness.FLIGHTS;
		boolean mHotelConfAvailable = state == CheckoutState.CONFIRMATION && getLob() == LineOfBusiness.HOTELS;

		if (mBucketFlightFrag != null && mBucketFlightFragStateListener != null) {
			mBucketFlightFrag.unRegisterStateListener(mBucketFlightFragStateListener);
		}
		if (mBucketHotelFrag != null && mBucketHotelFragStateListener != null) {
			mBucketHotelFrag.unRegisterStateListener(mBucketHotelFragStateListener);
		}

		mBucketFlightFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightBucketItemAvailable, FRAG_TAG_BUCKET_FLIGHT,
			manager, transaction, this, R.id.bucket_flight_frag_container, false);

		mBucketHotelFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			hotelBucketItemAvailable, FRAG_TAG_BUCKET_HOTEL,
			manager, transaction, this, R.id.bucket_hotel_frag_container, false);

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

		transaction.commit();

		if (mBucketFlightFrag != null) {
			if (mBucketFlightFragStateListener == null) {
				mBucketFlightFragStateListener = new TripBucketOrchestrator(mBucketFlightFrag);
			}
			mBucketFlightFrag.registerStateListener(mBucketFlightFragStateListener, false);
		}

		if (mBucketHotelFrag != null) {
			if (mBucketHotelFragStateListener == null) {
				mBucketHotelFragStateListener = new TripBucketOrchestrator(mBucketHotelFrag);
			}
			mBucketHotelFrag.registerStateListener(mBucketHotelFragStateListener, false);
		}

		mTripBucketItemFragments = new ArrayList<TripBucketItemFragment>();
		if (mBucketHotelFrag != null) {
			mTripBucketItemFragments.add(mBucketHotelFrag);
		}
		if (mBucketFlightFrag != null) {
			mTripBucketItemFragments.add(mBucketFlightFrag);
		}
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
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			return TripBucketFlightFragment.newInstance();
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			return TripBucketHotelFragment.newInstance();
		}
		else if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
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
			String dest = Sp.getParams().getDestination().getAirportCode();
			return ResultsBackgroundImageFragment.newInstance(dest, true);
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			TripBucketFlightFragment f = (TripBucketFlightFragment) frag;
			f.setState(getLob() == LineOfBusiness.FLIGHTS ? TripBucketItemState.EXPANDED : TripBucketItemState.DEFAULT);
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			TripBucketHotelFragment f = (TripBucketHotelFragment) frag;
			f.setState(getLob() == LineOfBusiness.HOTELS ? TripBucketItemState.EXPANDED : TripBucketItemState.DEFAULT);
		}
		else if (FRAG_TAG_CHECKOUT_INFO.equals(tag)) {
			TabletCheckoutFormsFragment f = (TabletCheckoutFormsFragment) frag;
			f.setLob(getLob());
		}
		else if (FRAG_TAG_SLIDE_TO_PURCHASE.equals(tag)) {
			TabletCheckoutSlideFragment f = (TabletCheckoutSlideFragment) frag;
			LineOfBusiness lob = getLob();
			f.setLob(lob);
			if (lob == LineOfBusiness.FLIGHTS) {
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				f.setTotalPriceString(FlightUtils.getSlideToPurchaseString(getActivity(), trip));
			}
			else if (lob == LineOfBusiness.HOTELS) {
				HotelSearch search = Db.getHotelSearch();
				Property property = search.getSelectedProperty();
				Rate rate = search.getSelectedRate();
				if (search.isCouponApplied()) {
					rate = search.getCouponRate();
				}
				f.setTotalPriceString(HotelUtils.getSlideToPurchaseString(getActivity(), property, rate));
			}
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

	@Override
	public void onSlideAbort() {
		CheckoutState stateTwo = bookingWithGoogleWallet() ? CheckoutState.BOOKING : CheckoutState.CVV;
		endStateTransition(CheckoutState.READY_FOR_CHECKOUT, stateTwo);
		setCheckoutState(CheckoutState.READY_FOR_CHECKOUT, false);
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
	 * (non-Javadoc)
	 * @see com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener#checkoutInformationIsValid()
	 */

	@Override
	public void checkoutInformationIsValid() {
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
		if (mStateManager.getState() == CheckoutState.READY_FOR_CHECKOUT) {
			setCheckoutState(CheckoutState.OVERVIEW, true);
		}
	}

	@Override
	public void onBillingInfoChange() {
		if (mCvvFrag != null && mCvvFrag.isAdded()) {
			mCvvFrag.bind();
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

		if (!AndroidUtils.isRelease(getActivity()) && SettingUtils
			.get(getActivity(), R.string.preference_force_google_wallet_error, false)) {
			ServerError googleWalletError = new ServerError();
			googleWalletError.setCode("GOOGLE_WALLET_ERROR");
			results.addErrorToFront(googleWalletError);
		}

		if (results instanceof FlightCheckoutResponse) {
			FlightCheckoutResponse response = (FlightCheckoutResponse) results;

			Db.setFlightCheckout(response);

			if (response == null || response.hasErrors()) {
				mFlightBookingFrag.handleBookingErrorResponse(response);
			}
			else {
				// TODO tracking ??
				setCheckoutState(CheckoutState.CONFIRMATION, true);
			}
		}
		// HotelBookingResponse
		else if (results instanceof BookingResponse) {
			BookingResponse response = (BookingResponse) results;
			Property property = Db.getHotelSearch().getSelectedProperty();

			Db.setBookingResponse(response);

			if (results == null || response.hasErrors()) {
				response.setProperty(property);
				mHotelBookingFrag.handleBookingErrorResponse(response);
			}
			else {
				response.setProperty(property);
				setCheckoutState(CheckoutState.CONFIRMATION, true);
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
	public void onHotelProductDownloadSuccess(HotelProductDownloadSuccess event) {
		mIsDoneLoadingPriceChange = true;
		dismissLoadingDialogs();
		startCreateTripDownload();
	}

	@Subscribe
	public void onHotelProductRateUp(HotelProductRateUp event) {
		mBucketHotelFrag.refreshRate(event.newRate);
		// Let's refresh state to reflect price change notification
		setCheckoutState(CheckoutState.OVERVIEW, true);
	}

	@Subscribe
	public void onCreateTripDownloadSuccess(CreateTripDownloadSuccess event) {
		dismissLoadingDialogs();
		if (getLob() == LineOfBusiness.FLIGHTS) {
			mIsFlightTripDone = true;
			rebindCheckoutFragment();
		}
	}

	@Subscribe
	public void onCreateTripDownloadError(CreateTripDownloadError event) {
		dismissLoadingDialogs();
		if (getLob() == LineOfBusiness.FLIGHTS) {
			mIsFlightTripDone = true;
		}
	}

	@Subscribe
	public void onCreateTripDownloadRetry(CreateTripDownloadRetry event) {
		if (getLob() == LineOfBusiness.HOTELS) {
			startCreateTripDownload();
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mIsFlightTripDone = false;
			doCreateTrip();
		}
	}

	@Subscribe
	public void onCreateTripDownloadRetryCancel(CreateTripDownloadRetryCancel event) {
		if (getActivity() != null) {
			getActivity().finish();
		}
	}

	@Subscribe
	public void onCouponApplySuccess(CouponApplyDownloadSuccess event) {
		Db.getTripBucket().getHotel().setIsCouponApplied(true);
		Db.getTripBucket().getHotel().setCouponRate(event.newRate);
		mBucketHotelFrag.refreshRate();
	}

	@Subscribe
	public void onCouponRemoveSuccess(CouponRemoveDownloadSuccess event) {
		Db.getTripBucket().getHotel().setIsCouponApplied(false);
		Db.getTripBucket().getHotel().setCouponRate(null);
		mBucketHotelFrag.refreshRate();
	}

	@Subscribe
	public void onBookingErrorDialogClick(Events.SimpleCallBackDialogOnClick event) {
		if (bookingWithGoogleWallet()) {
			setCheckoutState(CheckoutState.OVERVIEW, true);
			return;
		}
		int callBackId = event.callBackId;
		switch (callBackId) {
		case BookingFragment.DIALOG_CALLBACK_EXPIRED_CC:
		case BookingFragment.DIALOG_CALLBACK_INVALID_CC:
		case BookingFragment.DIALOG_CALLBACK_INVALID_POSTALCODE:
		case BookingFragment.DIALOG_CALLBACK_INVALID_PAYMENT:
			mCheckoutFragment.setState(CheckoutFormState.EDIT_PAYMENT, false);
			setCheckoutState(CheckoutState.OVERVIEW, true);
			break;
		case BookingFragment.DIALOG_CALLBACK_INVALID_PHONENUMBER:
			mCheckoutFragment.setState(CheckoutFormState.EDIT_TRAVELER, false);
			setCheckoutState(CheckoutState.OVERVIEW, true);
			break;
		case BookingFragment.DIALOG_CALLBACK_INVALID_MINOR:
		case BookingFragment.DIALOG_CALLBACK_MINOR:
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
			Db.clearGoogleWallet();
			WalletUtils.unbindAllWalletDataFromBillingInfo(Db.getBillingInfo());
			setLob(event.nextItem);
			setCheckoutState(CheckoutState.OVERVIEW, false);
		}
	}

	@Subscribe
	public void onFlightTripPriceChange(Events.FlightPriceChange event) {
		String changeString = event.changeString;
		if (changeString != null) {
			mBucketFlightFrag.refreshTripOnPriceChanged(event.changeString);
			// Let's refresh state to reflect price change notification
			setCheckoutState(CheckoutState.OVERVIEW, true);
		}
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
				mCheckoutFragment.registerStateListener(mCheckoutFormStateListener, true);
			}
			else {
				mCheckoutFragment.unRegisterStateListener(mCheckoutFormStateListener);
			}
		}
	}

	///////////////////////////////////
	// Listen to CheckoutFormState, hide the slide to checkout when required

	private StateListenerHelper<CheckoutFormState> mCheckoutFormStateListener = new StateListenerHelper<CheckoutFormState>() {
		private boolean mStartReacted = false;

		@Override
		public void onStateTransitionStart(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			if (reactToFormOpening()) {
				setShowReadyForCheckoutPercentage(stateIsOpen(stateOne) ? 0f : 1f);
				mStartReacted = true;
			}
			else {
				mStartReacted = false;
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutFormState stateOne, CheckoutFormState stateTwo, float percentage) {
			if (reactToFormOpening()) {
				setShowReadyForCheckoutPercentage(stateIsOpen(stateOne) ? percentage : 1f - percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutFormState stateOne, CheckoutFormState stateTwo) {

		}

		@Override
		public void onStateFinalized(CheckoutFormState state) {
			if (reactToFormOpening()) {
				setShowReadyForCheckoutPercentage(stateIsOpen(state) ? 0f : 1f);
			}
			else if (mStartReacted) {
				//If we reacted at the start, but we aren't reacting here, lets be safe and reset the state.
				setCheckoutState(mStateManager.getState(), false);
			}
			mStartReacted = false;
		}

		private boolean reactToFormOpening() {
			return mStateManager.getState() == CheckoutState.READY_FOR_CHECKOUT;
		}

		private boolean stateIsOpen(CheckoutFormState state) {
			return state != CheckoutFormState.OVERVIEW;
		}
	};

}
