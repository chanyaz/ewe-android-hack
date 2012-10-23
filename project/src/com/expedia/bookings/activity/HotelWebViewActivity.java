package com.expedia.bookings.activity;

import com.expedia.bookings.R;

import android.os.Bundle;

public class HotelWebViewActivity extends FlightWebViewActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setLogo(R.drawable.ic_logo_hotels);
	}
}