package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketYourTripToFragment extends Fragment {

	public static ResultsTripBucketYourTripToFragment newInstance() {
		ResultsTripBucketYourTripToFragment frag = new ResultsTripBucketYourTripToFragment();
		return frag;
	}

	private boolean mRunBind = false;

	private ViewGroup mRootC;
	private TextView mPrimaryDestTv;
	private TextView mSecondaryDestTv;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_your_trip_to, null);
		mPrimaryDestTv = Ui.findView(mRootC, R.id.primary_destination_text);
		mSecondaryDestTv = Ui.findView(mRootC, R.id.secondary_destination_text);
		if (mRunBind) {
			bindToDb();
		}
		return mRootC;
	}

	public void bindToDb() {
		if (mPrimaryDestTv != null) {
			String city = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity();
			String country = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCountryCode();

			mPrimaryDestTv.setText(city);
			mSecondaryDestTv.setText(country);
			mRunBind = false;
		}
		else {
			mRunBind = true;
		}
	}
}
