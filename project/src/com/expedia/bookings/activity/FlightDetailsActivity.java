package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.TripFragment;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FlightDetailsActivity extends FragmentActivity {

	public static final String EXTRA_STARTING_POSITION = "EXTRA_STARTING_POSITION";
	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_flight_details);
		
		int pos = getIntent().getIntExtra(EXTRA_STARTING_POSITION, 0);
		int legPos = getIntent().getIntExtra(EXTRA_LEG_POSITION, 0);
		
		Log.i("onCreate - EXTRA_STARTING_POSITION:" + pos);
		Log.i("onCreate - EXTRA_LEG_POSITION:" + legPos);
		
		FlightTrip trip = Db.getFlightSearch().getTrips(legPos).get(pos);
	
		Ui.setText(this, R.id.flight_details_num_seats_tv, "" + trip.getSeatsRemaining());//TODO: Seats remaining cutoff like results activity!?!?!
		Ui.setText(this, R.id.flight_details_seat_price_tv, "" + trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
	
        // During initial setup, plug in the details fragment.
        TripFragment details = TripFragment.newInstance(pos,legPos);
        getSupportFragmentManager().beginTransaction().add(R.id.flight_details_card_holder_ll, details).commit();
	}

	@Override
	protected void onResume(){
		super.onResume();
	}
}
