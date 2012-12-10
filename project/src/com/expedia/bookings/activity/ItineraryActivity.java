package com.expedia.bookings.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;

import android.os.Bundle;

public class ItineraryActivity extends SherlockFragmentActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_itinerary);
	}

}
