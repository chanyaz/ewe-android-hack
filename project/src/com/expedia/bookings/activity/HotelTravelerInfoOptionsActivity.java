package com.expedia.bookings.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;

public class HotelTravelerInfoOptionsActivity extends SherlockFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hotel_traveler_info_options);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}