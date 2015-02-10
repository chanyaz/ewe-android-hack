package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.presenter.CarsPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsActivity extends Activity {

	@InjectView(R.id.cars_presenter)
	CarsPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		CarDb.injectEndpoint(this);

		setContentView(R.layout.activity_cars);
		ButterKnife.inject(this);
	}

	@Override
	public void onBackPressed() {
		if (!carsPresenter.back()) {
			super.onBackPressed();
		}
	}
}
