package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.widget.FlightTripView;
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
	private Button mBookFlight;
	private FlightTripView mFlightTripView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_flight, null);
		mFlightSection = Ui.findView(mRootC, R.id.trip_bucket_flight);
		mFlightTripView = Ui.findView(mRootC, R.id.flight_trip_view);

		mBookFlight = Ui.findView(mRootC, R.id.tripbucket_book_button_flight);
		FontCache.setTypeface(mBookFlight, Font.ROBOTO_MEDIUM);

		bindToDb();
		return mRootC;
	}

	public void bindToDb() {
		if (mFlightSection != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
			mFlightSection.bind(Db.getFlightSearch().getSelectedFlightTrip(), Db.getFlightSearch()
					.getSelectedFlightTrip().getLeg(0));
			FlightSearch search = Db.getFlightSearch();
			FlightTripQuery query = search.queryTrips(0);
			mFlightTripView.setUp(search.getSelectedLegs()[0].getFlightLeg(), query.getMinTime(),
					query.getMaxTime());
		}
	}
}
