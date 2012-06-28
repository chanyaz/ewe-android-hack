package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.SectionDisplayFlight;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightTripOverviewActivity extends SherlockActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	FlightTrip mFt;

	SectionDisplayFlight mDepFlight;
	SectionDisplayFlight mRetFlight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_trip_overview);

		String tripKey = getIntent().getStringExtra(EXTRA_TRIP_KEY);
		if (tripKey != null) {
			mFt = Db.getFlightSearch().getFlightTrip(tripKey);

			//Set textviews
			FlightLeg arrLeg = mFt.getLeg(0);
			String cityName = arrLeg.getSegment(arrLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;

			Flight firstSeg = arrLeg.getSegment(0);
			Calendar cal = firstSeg.mOrigin.getMostRelevantDateTime();
			String monthStr = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);

			String date = String
					.format(getResources().getString(R.string.long_form_date_TEMPLATE), monthStr, day, year);
			Ui.setText(this, R.id.departure_date_long_form, date);
			Ui.setText(this, R.id.your_trip_to,
					String.format(getResources().getString(R.string.your_trip_to_TEMPLATE), cityName));
			Ui.setText(this, R.id.traveler_count,
					String.format(getResources().getString(R.string.number_of_travelers_TEMPLATE), "1"));
			Ui.setText(this, R.id.trip_cost, mFt.getTotalFare().getFormattedMoney());

		}

		Button checkoutBtn = Ui.findView(this, R.id.checkout_btn);
		checkoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTripOverviewActivity.this, FlightBookingActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mDepFlight = Ui.findView(this, R.id.departing_flight);
		mRetFlight = Ui.findView(this, R.id.return_flight);

		mDepFlight.setIsOutbound(true);
		mRetFlight.setIsOutbound(false);

		bindAll();
	}

	public void bindAll() {

		//TODO:Better checking
		if (mFt == null) {
			return;
		}

		if (mFt.getLegCount() == 2) {
			mDepFlight.bind(mFt.getLeg(0));
			mRetFlight.bind(mFt.getLeg(1));
		}
		else {
			Ui.showToast(this, "Expecting 2 flight legs, got: " + mFt.getLegCount());
		}
	}

}