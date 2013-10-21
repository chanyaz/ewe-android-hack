package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketFlightFragment extends Fragment {

	public static ResultsTripBucketFlightFragment newInstance() {
		ResultsTripBucketFlightFragment frag = new ResultsTripBucketFlightFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private FlightLegSummarySectionTablet mFlightSection;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_flight, null);
		mFlightSection = Ui.findView(mRootC, R.id.trip_bucket_flight);
		bindToDb();
		return mRootC;
	}

	public void bindToDb() {
		if (mFlightSection != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
			mFlightSection.bind(Db.getFlightSearch().getSelectedFlightTrip(), Db.getFlightSearch()
					.getSelectedFlightTrip().getLeg(0));
		}
	}
}
