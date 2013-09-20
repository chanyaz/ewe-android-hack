package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.util.Ui;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * ResultsTripOverviewFragment: The trip overview fragment designed for tablet results 2013
 */
public class ResultsTripOverviewFragment extends Fragment {

	private View mHotelView;
	private View mFlightView;

	public static ResultsTripOverviewFragment newInstance() {
		ResultsTripOverviewFragment frag = new ResultsTripOverviewFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip_bucket, null);

		mHotelView = Ui.findView(view, R.id.trip_bucket_hotel);
		mFlightView = Ui.findView(view, R.id.trip_bucket_flight);

		return view;
	}

	public View getHotelViewForAddTrip() {
		TextView tv = new TextView(getActivity());
		tv.setBackgroundColor(Color.RED);
		tv.setText("Hotel Image B");
		tv.setGravity(Gravity.CENTER);
		return tv;
	}

	public Rect getHotelContainerRect() {
		return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mHotelView);
	}

	public View getFlightViewForAddTrip() {
		FlightLegSummarySectionTablet view = (FlightLegSummarySectionTablet) getActivity().getLayoutInflater().inflate(R.layout.section_flight_leg_summary_tablet, null);
		return view;
	}

	public Rect getFlightContainerRect() {
		return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mFlightView);

	}

}
