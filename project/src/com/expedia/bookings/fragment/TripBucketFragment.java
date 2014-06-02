package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.TripBucket;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.util.Ui;

/**
 * TripBucketFragment: designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TripBucketFragment extends Fragment implements FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String FTAG_BUCKET_FLIGHT = "FTAG_BUCKET_FLIGHT";
	private static final String FTAG_BUCKET_HOTEL = "FTAG_BUCKET_HOTEL";

	private TripBucketFlightFragment mTripBucketFlightFrag;
	private TripBucketHotelFragment mTripBucketHotelFrag;

	private GridManager mGrid = new GridManager();

	private ScrollView mScrollC;
	private LinearLayout mContentC;
	private ViewGroup mHotelC;
	private ViewGroup mFlightC;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tripbucket, null, false);
		mScrollC = Ui.findView(view, R.id.scroll_container);
		mContentC = Ui.findView(view, R.id.content_container);
		mHotelC = Ui.findView(view, R.id.trip_bucket_hotel_trip);
		mFlightC = Ui.findView(view, R.id.trip_bucket_flight_trip);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
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

		boolean showFlight = bucket.getFlight() != null;
		boolean showHotel = bucket.getHotel() != null;

		setFragState(showFlight, showHotel);

		mFlightC.setVisibility(showFlight ? View.VISIBLE : (showHotel ? View.GONE : View.INVISIBLE));
		mHotelC.setVisibility(showHotel ? View.VISIBLE : View.GONE);

		if (showFlight && lobToRefresh != null && lobToRefresh == LineOfBusiness.FLIGHTS) {
			mTripBucketFlightFrag.bind();
			mTripBucketFlightFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
		}
		if (showHotel && lobToRefresh != null && lobToRefresh == LineOfBusiness.HOTELS) {
			mTripBucketHotelFrag.bind();
			mTripBucketHotelFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
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

	public Rect getFlightRect() {
		return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mFlightC);
	}

	public Rect getHotelRect() {
		return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mHotelC);
	}


	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);
			//TODO: USE THIS OR NUKE IT
		}

	};

	/*
	 * FRAG AVAILABILITY
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
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
}
