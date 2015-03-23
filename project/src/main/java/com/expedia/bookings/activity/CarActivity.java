package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.CarPresenter;
import com.expedia.bookings.utils.Ui;
import com.facebook.Session;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarActivity extends ActionBarActivity {

	@InjectView(R.id.car_presenter)
	CarPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultCarComponents();
		setContentView(R.layout.activity_car);
		Ui.showTransparentStatusBar(this);
		ButterKnife.inject(this);
	}

	@Override
	public void onBackPressed() {
		if (!carsPresenter.back()) {
			super.onBackPressed();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(CarActivity.this, requestCode, resultCode, data);
	}

	@Subscribe
	public void onFinishActivity(Events.FinishActivity event) {
		finish();
	}
}
