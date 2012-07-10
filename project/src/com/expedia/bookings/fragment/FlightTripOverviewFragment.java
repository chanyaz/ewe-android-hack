package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightCheckoutActivity;
import com.expedia.bookings.activity.FlightDetailsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.SectionFlightLeg;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

public class FlightTripOverviewFragment extends Fragment {

	public static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";

	private static final int FLIGHT_LEG_BOTTOM_MARGIN = 20;

	FlightTrip mTrip;

	ArrayList<SectionFlightLeg> mFlights;
	ViewGroup mFlightContainer;

	public static FlightTripOverviewFragment newInstance(String tripKey) {
		FlightTripOverviewFragment fragment = new FlightTripOverviewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TRIP_KEY, tripKey);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_flight_trip_overview, container, false);

		mFlights = new ArrayList<SectionFlightLeg>();
		mFlightContainer = Ui.findView(v, R.id.flight_legs_container);

		String tripKey = getArguments().getString(ARG_TRIP_KEY);
		if (tripKey != null) {
			mTrip = Db.getFlightSearch().getFlightTrip(tripKey);

			//Set up the Activity's views.
			FlightLeg arrLeg = mTrip.getLeg(0);
			String cityName = arrLeg.getSegment(arrLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;

			Flight firstSeg = arrLeg.getSegment(0);
			Calendar cal = firstSeg.mOrigin.getMostRelevantDateTime();
			String monthStr = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_LONG);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);

			String date = String
					.format(getResources().getString(R.string.long_form_date_TEMPLATE), monthStr, day, year);
			Ui.setText(v, R.id.departure_date_long_form, date);
			Ui.setText(v, R.id.your_trip_to,
					String.format(getResources().getString(R.string.your_trip_to_TEMPLATE), cityName));
			Ui.setText(v, R.id.traveler_count,
					String.format(getResources().getString(R.string.number_of_travelers_TEMPLATE), "1"));
			Ui.setText(v, R.id.trip_cost, mTrip.getTotalFare().getFormattedMoney());

			//Inflate and store the sections
			SectionFlightLeg tempFlight;
			for (int i = 0; i < mTrip.getLegCount(); i++) {
				tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
				if (i < mTrip.getLegCount() - 1) {
					tempFlight.setIsOutbound(true);
					LinearLayout.LayoutParams tempFlightLayoutParams = (LayoutParams) tempFlight.getLayoutParams();
					if (tempFlightLayoutParams == null) {
						tempFlightLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					}
					tempFlightLayoutParams.bottomMargin = FLIGHT_LEG_BOTTOM_MARGIN;
					tempFlight.setLayoutParams(tempFlightLayoutParams);
				}
				else {
					tempFlight.setIsOutbound(false);
				}
				mFlights.add(tempFlight);
				mFlightContainer.addView(tempFlight);
			}

		}

		Button checkoutBtn = Ui.findView(v, R.id.checkout_btn);
		checkoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FlightCheckoutActivity.class);
				intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, mTrip.getProductKey());
				startActivity(intent);
			}
		});

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		bindAll();
	}

	public void bindAll() {

		//TODO:Better checking
		if (mTrip == null) {
			return;
		}

		if (mFlights.size() != mTrip.getLegCount()) {
			Ui.showToast(getActivity(), "Invalid flight legs");
			return;
		}

		for (int i = 0; i < mTrip.getLegCount(); i++) {
			mFlights.get(i).bind(mTrip.getLeg(i));
		}
	}
}
