package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.TripFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FlightDetailsActivity extends FragmentActivity {

	public static final String EXTRA_STARTING_POSITION = "EXTRA_STARTING_POSITION";
	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	//private FlightAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_flight_details);
		
		if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            TripFragment details = TripFragment.newInstance(getIntent().getIntExtra(EXTRA_STARTING_POSITION, 0), getIntent().getBooleanExtra(EXTRA_LEG_POSITION, true));
            getSupportFragmentManager().beginTransaction().add(R.id.flight_details_card_holder_ll, details).commit();
        }
		
	}

}
