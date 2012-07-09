package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOptionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_traveler_info_options);

		Button enterManually = Ui.findView(this, R.id.enter_info_manually_button);
		enterManually.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoOneActivity.class);
				intent.fillIn(getIntent(), 0);
				startActivity(intent);
			}
		});
	}
}
