package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.SectionFlightLeg;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class FlightTripOverviewActivity extends SherlockActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	private static final int FLIGHT_LEG_BOTTOM_MARGIN = 20;

	FlightTrip mTrip;

	ArrayList<SectionFlightLeg> mFlights;
	ViewGroup mFlightContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_trip_overview);

		mFlights = new ArrayList<SectionFlightLeg>();
		mFlightContainer = Ui.findView(this, R.id.flight_legs_container);

		String tripKey = getIntent().getStringExtra(EXTRA_TRIP_KEY);
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
			Ui.setText(this, R.id.departure_date_long_form, date);
			Ui.setText(this, R.id.your_trip_to,
					String.format(getResources().getString(R.string.your_trip_to_TEMPLATE), cityName));
			Ui.setText(this, R.id.traveler_count,
					String.format(getResources().getString(R.string.number_of_travelers_TEMPLATE), "1"));
			Ui.setText(this, R.id.trip_cost, mTrip.getTotalFare().getFormattedMoney());

			//Inflate and store the sections
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		Button checkoutBtn = Ui.findView(this, R.id.checkout_btn);
		checkoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTripOverviewActivity.this, FlightCheckoutActivity.class);
				intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, mTrip.getProductKey());
				startActivity(intent);
			}
		});
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
			Ui.showToast(this, "Invalid flight legs");
			return;
		}

		for (int i = 0; i < mTrip.getLegCount(); i++) {
			mFlights.get(i).bind(mTrip.getLeg(i));
		}
	}

}