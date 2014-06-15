package com.expedia.bookings.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.TripBucket;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketFragment designed for Tablet 2014
 */
public class ResultsTripBucketFragment extends Fragment
	implements FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String FTAG_BUCKET = "FTAG_BUCKET";

	private TripBucketFragment mBucketFrag;

	private FrameLayoutTouchController mTripBucketC;
	private ViewGroup mEmptyTripC;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip_bucket, container, false);

		mTripBucketC = Ui.findView(view, R.id.trip_bucket_container);
		mEmptyTripC = Ui.findView(view, R.id.empty_bucket_container);

		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		mBucketFrag = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_BUCKET,
			manager, transaction, this, R.id.trip_bucket_container, true);
		transaction.commit();

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (Db.getTripBucket().isEmpty()) {
			Db.loadTripBucket(getActivity());
		}
		if (Db.getTripBucket().getFlight() != null) {
			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			Db.getFlightSearch().clearSelectedLegs();
			Db.getFlightSearch().setSearchParams(flight.getFlightSearchParams());
			Db.getFlightSearch().setSelectedFlightTrip(flight.getFlightTrip());
		}
		if (Db.getTripBucket().getHotel() != null) {
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			Db.getHotelSearch().setSelectedRate(hotel.getRate(), hotel.getHotelAvailability());
			Db.getHotelSearch().setSelectedHotelAvailability(hotel.getHotelAvailability());
			Db.getHotelSearch().setSelectedProperty(hotel.getProperty());
		}
	}

	public void bindToDb() {
		bind(Db.getTripBucket());
	}

	private void bind(TripBucket bucket) {
		boolean showBucket = bucket != null && bucket.size() > 0;
		LineOfBusiness lobToRefresh = bucket.getLOBToRefresh();
		if (showBucket) {
			mEmptyTripC.setVisibility(View.GONE);
			mTripBucketC.setVisibility(View.VISIBLE);
		}
		else {
			mEmptyTripC.setVisibility(View.VISIBLE);
			mTripBucketC.setVisibility(View.INVISIBLE);
		}

		if (mBucketFrag != null && mBucketFrag.isAdded() && showBucket) {
			mBucketFrag.bind(bucket, lobToRefresh);
		}
	}

	public void setBucketPreparedForAdd(LineOfBusiness lob) {
		if (mBucketFrag != null && mBucketFrag.isResumed()) {
			mBucketFrag.setBucketPreparedForAdd(lob);
		}
	}

	public Rect getAddToTripBucketDestinationRect(LineOfBusiness lob) {
		Rect rect = null;
		if (mBucketFrag != null && mBucketFrag.isResumed()) {
			if (lob == LineOfBusiness.HOTELS) {
				rect = mBucketFrag.getHotelRect();
			}
			else if (lob == LineOfBusiness.FLIGHTS) {
				rect = mBucketFrag.getFlightRect();
			}
		}
		if (rect == null || rect.isEmpty() || rect.height() <= 0 || rect.width() <= 0) {
			rect = getFakeTopRect();
		}
		return rect;
	}

	private Rect getFakeTopRect() {
		int paddingX = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_padding_x);
		int bucketMarginTop = getResources().getDimensionPixelSize(R.dimen.bucket_top_margin);
		int height = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_height);
		if (mTripBucketC != null) {
			Rect rect = ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mTripBucketC);
			rect.top += bucketMarginTop;
			rect.left += paddingX;
			rect.right -= paddingX;
			rect.bottom = rect.top + height;
			return rect;
		}
		return new Rect();
	}


	/**
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_BUCKET) {
			return mBucketFrag;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_BUCKET) {
			return new TripBucketFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {

	}
}
