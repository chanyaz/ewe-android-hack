package com.expedia.bookings.activity;

import android.app.Activity;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.CarsState;
import com.expedia.bookings.presenter.CarsPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsActivity extends Activity {

	@InjectView(R.id.cars_presenter)
	CarsPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cars);

		ButterKnife.inject(this);
		carsPresenter.show(CarsState.SEARCH);
	}

	@Override
	public void onBackPressed() {
		if (!carsPresenter.handleBackPress()) {
			super.onBackPressed();
		}
	}
}
