package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.presenter.CarsPresenter;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsActivity extends ActionBarActivity {

	@InjectView(R.id.cars_presenter)
	CarsPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultComponents();
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
