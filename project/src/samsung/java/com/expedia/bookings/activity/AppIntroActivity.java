package com.expedia.bookings.activity;

import android.app.Activity;
import android.os.Bundle;

import com.expedia.bookings.R;

public class AppIntroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_app_intro_layout);
	}

}
