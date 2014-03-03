package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
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
import com.expedia.bookings.data.TripBucket;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tripbucket, null, false);
		mScrollC = Ui.findView(view, R.id.scroll_container);
		mContentC = Ui.findView(view, R.id.content_container);
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
		bind(Db.getTripBucket());
	}

	public void bind(TripBucket bucket) {
		//TODO: In the future, this thing should iterate over the trip bucket items and support N items etc.

		FragmentManager manager = getChildFragmentManager();
		manager.executePendingTransactions();

		boolean showFlight = bucket.getFlight() != null;
		boolean showHotel = bucket.getHotel() != null;

		FragmentTransaction transaction = manager.beginTransaction();

		mTripBucketFlightFrag = FragmentAvailabilityUtils.setFragmentAvailability(showFlight, FTAG_BUCKET_FLIGHT, manager, transaction, this, R.id.content_container, true);
		mTripBucketHotelFrag = FragmentAvailabilityUtils.setFragmentAvailability(showHotel, FTAG_BUCKET_HOTEL, manager, transaction, this, R.id.content_container, true);

		transaction.commit();

		if (showFlight) {
			mTripBucketFlightFrag.bind();
			mTripBucketFlightFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
		}
		if (showHotel) {
			mTripBucketHotelFrag.bind();
			mTripBucketHotelFrag.setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
		}
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
