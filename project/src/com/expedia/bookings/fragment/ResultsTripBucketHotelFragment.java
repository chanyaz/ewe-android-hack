package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.section.HotelSummarySection;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketHotelFragment extends Fragment {

	public static ResultsTripBucketHotelFragment newInstance() {
		ResultsTripBucketHotelFragment frag = new ResultsTripBucketHotelFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private HotelSummarySection mHotelSection;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_hotel, null);
		mHotelSection = Ui.findView(mRootC, R.id.trip_bucket_hotel);
		bindToDb();
		return mRootC;
	}

	public void bindToDb() {
		if (mHotelSection != null && Db.getHotelSearch() != null && Db.getHotelSearch().getAddedProperty() != null) {
			mHotelSection.bind(Db.getHotelSearch().getAddedProperty(), false, 16, false, DistanceUnit.MILES,
					false);
		}
	}
}
