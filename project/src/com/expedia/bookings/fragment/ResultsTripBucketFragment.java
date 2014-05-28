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
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
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

	public void bindToDb() {
		bind(Db.getTripBucket());
	}

	public void bind(TripBucket bucket) {
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

	public void setBucketPreparedForAdd() {
		if (mBucketFrag != null && mBucketFrag.isResumed()) {
			mBucketFrag.setBucketPreparedForAdd();
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
		return rect;
	}


	/**
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
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
