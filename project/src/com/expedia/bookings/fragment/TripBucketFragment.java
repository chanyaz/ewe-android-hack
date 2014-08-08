package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.TripBucket;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.widget.BucketItemUndoController;
import com.expedia.bookings.widget.SwipeOutLayout;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.Ui;


/**
 * TripBucketFragment: designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TripBucketFragment extends Fragment implements FragmentAvailabilityUtils.IFragmentAvailabilityProvider, com.mobiata.android.widget.UndoBarController.UndoListener {

	private static final String FTAG_BUCKET_FLIGHT = "FTAG_BUCKET_FLIGHT";
	private static final String FTAG_BUCKET_HOTEL = "FTAG_BUCKET_HOTEL";

	private static final float BUCKET_ITEM_SWIPE_THRESHOLD = 0.50f;

	private TripBucketFlightFragment mTripBucketFlightFrag;
	private TripBucketHotelFragment mTripBucketHotelFrag;

	// "In limbo": Item is in transitory state, stored in undo bar Bundle.

	private boolean mHotelInLimbo;
	private boolean mFlightInLimbo;

	private View mRootC;
	private ScrollView mScrollC;
	private LinearLayout mContentC;
	private SwipeOutLayout mHotelC;
	private FrameLayout mHotelCard;
	private LinearLayout mHotelUndo;
	private SwipeOutLayout mFlightC;
	private FrameLayout mFlightCard;
	private LinearLayout mFlightUndo;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = inflater.inflate(R.layout.fragment_tripbucket, null, false);
		mScrollC = Ui.findView(mRootC, R.id.scroll_container);
		mContentC = Ui.findView(mRootC, R.id.content_container);

		mHotelC = Ui.findView(mRootC, R.id.trip_bucket_hotel_trip_swipeout);
		mHotelCard = Ui.findView(mRootC, R.id.trip_bucket_hotel_trip);
		mHotelUndo = Ui.findView(mRootC, R.id.hotel_swipe_out_undo_bar);

		mFlightC = Ui.findView(mRootC, R.id.trip_bucket_flight_trip_swipeout);
		mFlightCard = Ui.findView(mRootC, R.id.trip_bucket_flight_trip);
		mFlightUndo = Ui.findView(mRootC, R.id.flight_swipe_out_undo_bar);

		mHotelC.addListener(new TripBucketSwipeListener(LineOfBusiness.HOTELS));
		mHotelC.setSwipeOutThresholdPercentage(BUCKET_ITEM_SWIPE_THRESHOLD);
		mFlightC.addListener(new TripBucketSwipeListener(LineOfBusiness.FLIGHTS));
		mFlightC.setSwipeOutThresholdPercentage(BUCKET_ITEM_SWIPE_THRESHOLD);

		int undoDividerPadding = (int) getResources().getDimension(R.dimen.undo_bar_divider_padding);
		mHotelUndo.setDividerPadding(undoDividerPadding);
		mFlightUndo.setDividerPadding(undoDividerPadding);

		// Because we share the base XML, I couldn't just set this visibility there.
		mHotelUndo.findViewById(com.mobiata.android.R.id.undobar_button).setVisibility(View.INVISIBLE);
		mFlightUndo.findViewById(com.mobiata.android.R.id.undobar_button).setVisibility(View.INVISIBLE);

		if (getResources().getBoolean(R.bool.portrait)) {
			mContentC.setOrientation(LinearLayout.HORIZONTAL);
		}

		return mRootC;
	}

	/**
	 * BIND
	 */

	public void bindToDb() {
		bind(Db.getTripBucket(), null);
	}

	public void bind(TripBucket bucket, LineOfBusiness lobToRefresh) {
		//TODO: In the future, this thing should iterate over the trip bucket items and support N items etc.
		mContentC.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

		boolean showFlightContainer = bucket.getFlight() != null || mFlightInLimbo;
		boolean showHotelContainer = bucket.getHotel() != null || mHotelInLimbo;

		setFragState(showFlightContainer, showHotelContainer);
		mFlightC.setVisibility(showFlightContainer ? View.VISIBLE : (showHotelContainer ? View.GONE : View.INVISIBLE));
		mFlightCard.setVisibility(bucket.getFlight() != null ? View.VISIBLE : (showHotelContainer ? View.GONE : View.INVISIBLE));
		mHotelC.setVisibility(showHotelContainer ? View.VISIBLE : View.GONE);
		mHotelCard.setVisibility(bucket.getHotel() != null ? View.VISIBLE : View.GONE);

		if (showFlightContainer && lobToRefresh != null && lobToRefresh == LineOfBusiness.FLIGHTS) {
			mTripBucketFlightFrag.bind();
			mTripBucketFlightFrag.setState(bucket.getFlight().getState());
		}
		if (showHotelContainer && lobToRefresh != null && lobToRefresh == LineOfBusiness.HOTELS) {
			mTripBucketHotelFrag.bind();
			mTripBucketHotelFrag.setState(bucket.getHotel().getState());
		}
		if (mTripBucketFlightFrag != null) {
			setItemSwipeEnabled(mTripBucketFlightFrag.getItem());
		}
		if (mTripBucketHotelFrag != null) {
			setItemSwipeEnabled(mTripBucketHotelFrag.getItem());
		}
	}

	private void setFragState(boolean attachFlight, boolean attachHotel) {
		FragmentManager manager = getChildFragmentManager();
		manager.executePendingTransactions();

		FragmentTransaction transaction = manager.beginTransaction();

		mTripBucketFlightFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(attachFlight, FTAG_BUCKET_FLIGHT, manager,
				transaction, this, R.id.trip_bucket_flight_trip, true);
		mTripBucketHotelFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(attachHotel, FTAG_BUCKET_HOTEL, manager,
				transaction, this, R.id.trip_bucket_hotel_trip, true);

		transaction.commit();
	}

	/**
	 * This method sets both containers to be invisible and adds our fragments (without binding) so they will be measured.
	 */
	public void setBucketPreparedForAdd(LineOfBusiness lob) {
		//We are flying in a copy above the item in the tripbucket, but we dont want an old copy sliding in from the right
		//while our new entry flys in from the left, so we make it invisible (and thus still measurable)
		if (lob == LineOfBusiness.FLIGHTS && mFlightC.getVisibility() == View.VISIBLE) {
			mFlightC.setVisibility(View.INVISIBLE);
		}
		if (lob == LineOfBusiness.HOTELS && mHotelC.getVisibility() == View.VISIBLE) {
			mHotelC.setVisibility(View.INVISIBLE);
		}

		//We attach our fragments now so that they will be measured.
		if (lob == LineOfBusiness.HOTELS) {
			setFragState(Db.getTripBucket().getFlight() != null, true);
		}
		else if (lob == LineOfBusiness.FLIGHTS) {
			setFragState(true, Db.getTripBucket().getHotel() != null);
		}

		//bind the LOB specific fragment
		if (lob == LineOfBusiness.FLIGHTS && mTripBucketFlightFrag != null) {
			mTripBucketFlightFrag.bind();
			mTripBucketFlightFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
		}
		else if (lob == LineOfBusiness.HOTELS && mTripBucketHotelFrag != null) {
			mTripBucketHotelFrag.bind();
			mTripBucketHotelFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
		}

		//Move scrollview into place
		if (lob == LineOfBusiness.FLIGHTS) {
			mScrollC.scrollTo(0, 0);
		}
	}

	private class TripBucketSwipeListener implements SwipeOutLayout.ISwipeOutListener {

		private LineOfBusiness mLob;

		public TripBucketSwipeListener(LineOfBusiness lob) {
			mLob = lob;
		}

		@Override
		public void onSwipeStateChange(int oldState, int newState) {
		}

		@Override
		public void onSwipeUpdate(float percentage) {
		}

		@Override
		public void onSwipeAllTheWay() {
			TripBucketItem item = (mLob == LineOfBusiness.FLIGHTS ? Db.getTripBucket().getFlight() : Db.getTripBucket().getHotel());
			if (item != null) {
				tripBucketItemRemoved(item);
				OmnitureTracking.trackTripBucketItemRemoval(getActivity(), mLob);
			}
		}
	};

	/*
	 * FRAG AVAILABILITY
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_BUCKET_FLIGHT) {
			return mTripBucketFlightFrag;
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			return mTripBucketHotelFrag;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_BUCKET_FLIGHT) {
			return TripBucketFlightFragment.newInstance();
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			return TripBucketHotelFragment.newInstance();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {

	}

	/*
	 * Undo Bars
	 */

	public void tripBucketItemRemoved(final TripBucketItem item) {
		View undoView = item.getLineOfBusiness() == LineOfBusiness.FLIGHTS ? mFlightUndo : mHotelUndo;
		BucketItemUndoController undoBar = new BucketItemUndoController(undoView, this);
		undoBar.setAdditionalAnimationCallBack(undoBarListener);

		// Cache removed item in Bundle.
		Bundle b = new Bundle();
		JSONUtils.putEnum(b, "lob", item.getLineOfBusiness());
		JSONUtils.putJSONable(b, "item", item);

		// Remove item from db, show undo bar
		Db.getTripBucket().clear(item.getLineOfBusiness());
		Db.saveTripBucket(getActivity());

		if (item.getLineOfBusiness() == LineOfBusiness.FLIGHTS) {
			mFlightInLimbo = true;
		}
		else {
			mHotelInLimbo = true;
		}
		undoBar.showUndoBar(false, getString(R.string.tablet_tripbucket_item_removed), b);
		bindToDb();
	}

	@Override
	public void onUndo(Parcelable token) {
		// Get item's LOB
		LineOfBusiness itemLob = JSONUtils.getEnum((Bundle) token, "lob", LineOfBusiness.class);
		OmnitureTracking.trackTripBucketItemUndoRemoval(getActivity(), itemLob);
		// Add item back
		if (itemLob == LineOfBusiness.FLIGHTS) {
			TripBucketItemFlight flight = JSONUtils.getJSONable((Bundle) token, "item", TripBucketItemFlight.class);
			Db.getTripBucket().add(flight);
			mFlightInLimbo = false;
		}
		else {
			TripBucketItemHotel hotel = JSONUtils.getJSONable((Bundle) token, "item", TripBucketItemHotel.class);
			Db.getTripBucket().add(hotel);
			mHotelInLimbo = false;
		}
		Db.saveTripBucket(getActivity());
		bindToDb();
	}

	// We need to bind the entire TripBucket back to DB once the animation ends.
	private BucketItemUndoController.AnimationListenerAdapter undoBarListener = new BucketItemUndoController.AnimationListenerAdapter() {
		@Override
		public void onAnimationEnd(Animation animation) {
			TripBucket tb = Db.getTripBucket();
			// If an item is now null, but was marked as in limbo,
			// toggle the limbo flag.
			if (tb.getFlight() == null && mFlightInLimbo) {
				mFlightInLimbo = false;
			}
			if (tb.getHotel() == null && mHotelInLimbo) {
				mHotelInLimbo = false;
			}
			bindParent();
		}
	};

	// Have to bind ResultsTripBucketFragment to toggle TB empty view.
	private void bindParent() {
		UndoAnimationEndListener listener = Ui.findFragmentListener(this, UndoAnimationEndListener.class);
		listener.onUndoAnimationListenerEnd();
	}

	public interface UndoAnimationEndListener {
		public void onUndoAnimationListenerEnd();
	}

	public boolean hasItemsInUndoState() {
		return mHotelInLimbo || mFlightInLimbo;
	}

	// Item is swipeable if its state is not "PURCHASED"
	private void setItemSwipeEnabled(TripBucketItem item) {
		if (item != null) {
			LineOfBusiness lob = item.getLineOfBusiness();
			SwipeOutLayout swipeable = lob == LineOfBusiness.FLIGHTS ? mFlightC : mHotelC;
			TripBucketItemState state = item.getState();
			swipeable.setSwipeEnabled(state != TripBucketItemState.PURCHASED);
		}
	}
}
