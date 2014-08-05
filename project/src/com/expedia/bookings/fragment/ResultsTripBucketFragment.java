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
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.CenteredCaptionedIcon;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketFragment designed for Tablet 2014
 */
public class ResultsTripBucketFragment extends Fragment
	implements FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String FTAG_BUCKET = "FTAG_BUCKET";

	private TripBucketFragment mBucketFrag;

	private ViewGroup mRootC;
	private FrameLayoutTouchController mTripBucketC;
	private CenteredCaptionedIcon mEmptyBucketView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip_bucket, container, false);
		mRootC = container;
		mTripBucketC = Ui.findView(view, R.id.trip_bucket_container);
		mEmptyBucketView = Ui.findView(view, R.id.empty_bucket_view);

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

	private void bind(TripBucket bucket) {
		boolean showBucket = bucket != null && (bucket.size() > 0 || mBucketFrag.hasItemsInUndoState());
		LineOfBusiness lobToRefresh = bucket.getLOBToRefresh();
		if (showBucket) {
			mEmptyBucketView.setVisibility(View.GONE);
			mTripBucketC.setVisibility(View.VISIBLE);
			mRootC.setVisibility(View.VISIBLE);
		}
		else {
			if (getResources().getBoolean(R.bool.landscape)) {
				mEmptyBucketView.setVisibility(View.VISIBLE);
				mTripBucketC.setVisibility(View.INVISIBLE);
			}
			else {
				mRootC.setVisibility(View.GONE);
			}
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
		int height = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_height);
		if (mTripBucketC != null) {
			Rect rect = ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mTripBucketC);
			rect.left += paddingX;
			rect.right -= paddingX;
			rect.bottom = rect.top + height;
			return rect;
		}
		return new Rect();
	}

	public boolean hasItemsInUndoState() {
		return mBucketFrag.hasItemsInUndoState();
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
