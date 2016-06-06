package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.enums.CheckoutTripBucketState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.BookingUnavailableFragment.BookingUnavailableFragmentListener;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class TabletCheckoutTripBucketControllerFragment extends LobableFragment implements IStateProvider<CheckoutTripBucketState>,
	IFragmentAvailabilityProvider, BookingUnavailableFragmentListener {

	private static final String FRAG_TAG_BUCKET_FLIGHT = "FRAG_TAG_BUCKET_FLIGHT";
	private static final String FRAG_TAG_BUCKET_HOTEL = "FRAG_TAG_BUCKET_HOTEL";

	private static final String SAVED_STATE = "SAVED_STATE";

	private TripBucketFlightFragment mBucketFlightFrag;
	private TripBucketHotelFragment mBucketHotelFrag;

	private StateManager<CheckoutTripBucketState> mStateManager = new StateManager<>(CheckoutTripBucketState.SHOWING, this);

	private boolean mIsLandscape = true;
	private boolean mAnimating = false;

	// Containers
	private ViewGroup mRootC;
	private ViewGroup mBucketFlightContainerContainer;
	private ViewGroup mPortraitShowHideContainer;
	private TouchableFrameLayout mTouchBlocker;

	// Views
	private ViewGroup mBucketContainer;
	private View mBucketDimmer;
	private View mBucketShowHideButton;
	private View mCollapsedIndicator;
	private View mFlightSpacer;
	private View mHotelSpacer;
	private View mDummySpacer;

	public TabletCheckoutTripBucketControllerFragment newInstance() {
		return new TabletCheckoutTripBucketControllerFragment();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mIsLandscape = getResources().getBoolean(R.bool.landscape);

		CheckoutTripBucketState state;
		if (savedInstanceState != null) {
			state = parseState(savedInstanceState, SAVED_STATE, CheckoutTripBucketState.SHOWING);
		}
		else {
			state = CheckoutTripBucketState.SHOWING;
			setLob(((TabletCheckoutActivity) getActivity()).getLob());
		}

		if (mIsLandscape && state == CheckoutTripBucketState.OPEN) {
			// OPEN doesn't mean anything in landscape
			state = CheckoutTripBucketState.SHOWING;
		}

		mStateManager.setDefaultState(state);

		registerStateListener(new StateListenerLogger<CheckoutTripBucketState>(), false);

		if (mIsLandscape) {
			registerStateListener(mLandscapeListener, true);
		}
		else {
			registerStateListener(mPortraitOpenedListener, true);
			registerStateListener(mPortraitHiddenListener, true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_checkout_trip_bucket_controller, null, false);

		mBucketFlightContainerContainer = Ui.findView(mRootC, R.id.bucket_flight_frag_container_container);

		mBucketContainer = Ui.findView(mRootC, R.id.trip_bucket_container);
		mPortraitShowHideContainer = Ui.findView(mRootC, R.id.trip_bucket_show_hide_container);
		mBucketDimmer = Ui.findView(mRootC, R.id.trip_bucket_dimmer);
		mTouchBlocker = Ui.findView(mRootC, R.id.trip_bucket_dimmer);
		mCollapsedIndicator = Ui.findView(mRootC, R.id.collapsed_indicator);
		mBucketShowHideButton = Ui.findView(mRootC, R.id.trip_bucket_show_hide_button);
		if (mBucketShowHideButton != null) {
			mBucketShowHideButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mBucketContainer == null || mBucketDimmer == null) {
						return;
					}

					CheckoutTripBucketState other = CheckoutTripBucketState.SHOWING;
					if (getState() == CheckoutTripBucketState.SHOWING) {
						other = CheckoutTripBucketState.OPEN;
					}
					OmnitureTracking.trackTripBucketPortraitToggle(getLob(), other);
					setState(other, true);
				}
			});
		}

		mFlightSpacer = Ui.findView(mRootC, R.id.flight_spacer);
		mHotelSpacer = Ui.findView(mRootC, R.id.hotel_spacer);
		mDummySpacer = Ui.findView(mRootC, R.id.dummy_spacer);

		updateNumberOfItems();
		updateDateRange();

		return mRootC;
	}

	@Override
	public void onStart() {
		super.onStart();
		setState(mStateManager.getState(), false);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		Events.unregister(this);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_STATE, mStateManager.getState().name());
	}

	private void updateNumberOfItems() {
		TextView numText = Ui.findView(mRootC, R.id.number_of_items_in_trip_textview);
		TextView itemsText = Ui.findView(mRootC, R.id.items_in_trip_textview);
		if (numText != null && itemsText != null) {
			int numItemsInTripBucket = 0;

			if (Db.getTripBucket().getFlight() != null) {
				numItemsInTripBucket++;
			}
			if (Db.getTripBucket().getHotel() != null) {
				numItemsInTripBucket++;
			}

			numText.setText("" + numItemsInTripBucket);
			itemsText.setText(getResources().getQuantityString(R.plurals.items_in_trip, numItemsInTripBucket));
		}
	}

	private void updateDateRange() {
		TextView bucketDateRange = Ui.findView(mRootC, R.id.trip_date_range);
		if (getLob() == LineOfBusiness.FLIGHTS) {
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			String date = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
			bucketDateRange.setText(date);
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			String dateRange = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
			bucketDateRange.setText(dateRange);
		}

		// Set up an observer to hide the date range if it overlaps "your trip" in this language
		// (I'm looking at you, vi_VN)
		mRootC.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				TextView bucketDateRange = Ui.findView(mRootC, R.id.trip_date_range);
				TextView yourTrip = Ui.findView(mRootC, R.id.your_trip_tv);
				if (bucketDateRange.getWidth() > 0 && yourTrip.getWidth() > 0) {
					bucketDateRange.setVisibility(yourTrip.getRight() > bucketDateRange.getLeft() ? View.INVISIBLE : View.VISIBLE);
					mRootC.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			}
		});
	}

	public void setState(CheckoutTripBucketState state, boolean animate) {
		mAnimating = animate;
		mStateManager.setState(state, animate);
	}

	public void updateBucketItems(boolean animate) {
		setBucketState(animate);
	}

	private CheckoutTripBucketState getState() {
		return mStateManager.getState();
	}

	/*
	 * CheckoutTripBucketState ISTATEPROVIDER
	 */

	private StateListenerCollection<CheckoutTripBucketState> mStateListeners = new StateListenerCollection<CheckoutTripBucketState>();

	@Override
	public void startStateTransition(CheckoutTripBucketState stateOne, CheckoutTripBucketState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(CheckoutTripBucketState stateOne, CheckoutTripBucketState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(CheckoutTripBucketState stateOne, CheckoutTripBucketState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(CheckoutTripBucketState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<CheckoutTripBucketState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<CheckoutTripBucketState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			return mBucketFlightFrag;
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			return mBucketHotelFrag;
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
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FRAG_TAG_BUCKET_FLIGHT.equals(tag)) {
			TripBucketFlightFragment f = (TripBucketFlightFragment) frag;
			f.setState(TripBucketItemState.DEFAULT);
		}
		else if (FRAG_TAG_BUCKET_HOTEL.equals(tag)) {
			TripBucketHotelFragment f = (TripBucketHotelFragment) frag;
			f.setState(TripBucketItemState.DEFAULT);
		}
	}

	private void setFragmentState(CheckoutTripBucketState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean flightBucketItemAvailable = Db.getTripBucket().getFlight() != null;
		boolean hotelBucketItemAvailable = Db.getTripBucket().getHotel() != null;

		if (!mIsLandscape) {
			if (flightBucketItemAvailable) {
				mBucketFlightContainerContainer.setVisibility(View.VISIBLE);
				mFlightSpacer.setVisibility(View.VISIBLE);

				mHotelSpacer.setVisibility(View.GONE);
				mDummySpacer.setVisibility(View.GONE);
			}
			else {
				mBucketFlightContainerContainer.setVisibility(View.GONE);
				mFlightSpacer.setVisibility(View.GONE);

				mHotelSpacer.setVisibility(View.VISIBLE);
				mDummySpacer.setVisibility(View.VISIBLE);
			}
		}

		mBucketFlightFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightBucketItemAvailable, FRAG_TAG_BUCKET_FLIGHT,
			manager, transaction, this, R.id.bucket_flight_frag_container, false);

		mBucketHotelFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			hotelBucketItemAvailable, FRAG_TAG_BUCKET_HOTEL,
			manager, transaction, this, R.id.bucket_hotel_frag_container, false);

		transaction.commitAllowingStateLoss();
	}

	private void setBucketState(boolean animate) {
		if (Db.getTripBucket().getFlight() != null && mBucketFlightFrag != null) {
			boolean sameLob = getLob() == LineOfBusiness.FLIGHTS;
			TripBucketItemState flightState = getItemUiStateFromDataState(Db.getTripBucket().getFlight(), sameLob);
			mBucketFlightFrag.setState(flightState, animate);
		}
		if (Db.getTripBucket().getHotel() != null && mBucketHotelFrag != null) {
			boolean sameLob = getLob() == LineOfBusiness.HOTELS;
			TripBucketItemState hotelState = getItemUiStateFromDataState(Db.getTripBucket().getHotel(), sameLob);
			mBucketHotelFrag.setState(hotelState, animate);
		}
	}

	private TripBucketItemState getItemUiStateFromDataState(TripBucketItem item, final boolean lobMatches) {
		TripBucketItemState state = item.getState();
		CheckoutState checkoutState = getCheckoutState();
		Log.v("Transmogrification", "in state=" + state.name());

		if (state == TripBucketItemState.DEFAULT && !item.isSelected()) {
			state = TripBucketItemState.SHOWING_CHECKOUT_BUTTON;
		}

		if (mIsLandscape) {
			if (state == TripBucketItemState.DEFAULT && item.isSelected()) {
				state = TripBucketItemState.EXPANDED;
			}
			if (checkoutState == CheckoutState.CONFIRMATION) {

				if (lobMatches && state == TripBucketItemState.PURCHASED) {
					state = TripBucketItemState.CONFIRMATION;
				}

				else if (!lobMatches && item.getLineOfBusiness() == LineOfBusiness.HOTELS &&
					item.hasPriceChanged() &&
					item.canBePurchased()) {
					state = TripBucketItemState.SHOWING_AIR_ATTACH_PRICE_CHANGE;
				}

			}

			if (lobMatches && item.hasPriceChanged() && CheckoutState.shouldShowPriceChange(checkoutState)) {
				state = TripBucketItemState.SHOWING_PRICE_CHANGE;
			}
		}

		if (!lobMatches && checkoutState == CheckoutState.BOOKING) {
			state = TripBucketItemState.DISABLED;
		}

		Log.v("Transmogrification", "out state=" + state.name());
		return state;
	}

	private CheckoutState getCheckoutState() {
		return ((TabletCheckoutActivity) getActivity()).getCheckoutState();
	}

	private final ISingleStateListener mLandscapeBucketHidden = new ISingleStateListener() {

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mBucketContainer.setAlpha(1.0f);
			mBucketContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float p) {
			mBucketContainer.setAlpha(1.0f - p);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			final boolean isHidden = !isReversed;

			int visibility = isHidden ? View.INVISIBLE : View.VISIBLE;
			mBucketContainer.setVisibility(visibility);
			mBucketContainer.setAlpha(1.0f);

			setFragmentState(isHidden ? CheckoutTripBucketState.HIDDEN : CheckoutTripBucketState.SHOWING);
			setBucketState(mAnimating);
		}
	};

	private final ISingleStateListener mPortraitBucketOpen = new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			final boolean isOpen = !isReversed;
			mBucketDimmer.setVisibility(View.VISIBLE);
			mBucketContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float p) {
			mBucketDimmer.setAlpha(p);
			mBucketContainer.setTranslationY((1.0f - p) * -mBucketContainer.getHeight());

			mCollapsedIndicator.setRotation(180.0f * p);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			final boolean isOpen = !isReversed;

			int visibility = isOpen ? View.VISIBLE : View.INVISIBLE;
			mBucketDimmer.setVisibility(visibility);
			mBucketContainer.setVisibility(visibility);

			setFragmentState(isOpen ? CheckoutTripBucketState.OPEN : CheckoutTripBucketState.SHOWING);
			setBucketState(false /*animate*/);

			mTouchBlocker.setConsumeTouch(isOpen);
		}
	};

	private final ISingleStateListener mPortraitBucketHidden = new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mPortraitShowHideContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float p) {
			mPortraitShowHideContainer.setAlpha(1.0f - p);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			final boolean isHidden = !isReversed;
			mPortraitShowHideContainer.setVisibility(isHidden ? View.GONE : View.VISIBLE);

			mBucketDimmer.setVisibility(View.INVISIBLE);
			mBucketContainer.setVisibility(View.INVISIBLE);
			mTouchBlocker.setConsumeTouch(false);
		}
	};

	private final SingleStateListener mLandscapeListener = new SingleStateListener(CheckoutTripBucketState.SHOWING, CheckoutTripBucketState.HIDDEN, true, mLandscapeBucketHidden);
	private final SingleStateListener mPortraitOpenedListener = new SingleStateListener(CheckoutTripBucketState.SHOWING, CheckoutTripBucketState.OPEN, true, mPortraitBucketOpen);
	private final SingleStateListener mPortraitHiddenListener = new SingleStateListener(CheckoutTripBucketState.SHOWING, CheckoutTripBucketState.HIDDEN, true, mPortraitBucketHidden);

	@Override
	public void onLobSet(LineOfBusiness lob) {
		// Ignore
	}

	private CheckoutTripBucketState parseState(Bundle bundle, String key, CheckoutTripBucketState def) {
		return CheckoutTripBucketState.valueOf(bundle.getString(key, def.name()));
	}

	@Subscribe
	public void onLCCPaymentFeesAdded(Events.LCCPaymentFeesAdded event) {
		mBucketFlightFrag.refreshExpandedTripPrice();
	}

	@Subscribe
	public void onHotelProductRateUp(Events.HotelProductRateUp event) {
		mBucketHotelFrag.refreshRate();
		setBucketState(true);
	}

	@Subscribe
	public void onCreateTripSuccess(Events.CreateTripDownloadSuccess event) {
		// In the case of hotels with resort fees, the data received from /create
		// is different enough to warrant a complete re-bind. This has to do with
		// rate types, etc.
		if (event.createTripResponse instanceof CreateTripResponse) {
			mBucketHotelFrag.refreshRate();
		}
	}

	@Subscribe
	public void onFlightTripPriceChange(Events.FlightPriceChange event) {
		mBucketFlightFrag.refreshTripOnPriceChanged();
		setBucketState(true);
	}

	@Subscribe
	public void onCouponApplySuccess(Events.CouponApplyDownloadSuccess event) {
		mBucketHotelFrag.refreshRate();
	}

	@Subscribe
	public void onCouponRemoveSuccess(Events.CouponRemoveDownloadSuccess event) {
		mBucketHotelFrag.refreshRate();
	}

	/*
	 * BookingUnavailableFragment listener
	 */

	@Override
	public void onTripBucketItemRemoved(LineOfBusiness lob) {
		if (lob == LineOfBusiness.FLIGHTS) {
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			if (hotel != null && !hotel.hasBeenPurchased()) {
				updateNumberOfItems();
				mBucketHotelFrag.triggerTripBucketBookAction(LineOfBusiness.HOTELS);
				updateDateRange();
				setFragmentState(mStateManager.getState());
			}
			else {
				getActivity().finish();
			}
		}
		else {
			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			if (flight != null && !flight.hasBeenPurchased()) {
				updateNumberOfItems();
				mBucketFlightFrag.triggerTripBucketBookAction(LineOfBusiness.FLIGHTS);
				updateDateRange();
				setFragmentState(mStateManager.getState());
			}
			else {
				getActivity().finish();
			}
		}
	}

	@Override
	public void onSelectNewTripItem(LineOfBusiness lob) {
		// Ignore
	}
}
