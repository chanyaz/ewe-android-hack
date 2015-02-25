package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.presenter.CarsPresenter;
import com.expedia.bookings.utils.Ui;
import com.facebook.Session;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsActivity extends ActionBarActivity {

	@InjectView(R.id.cars_presenter)
	CarsPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultCarComponents();
		setContentView(R.layout.activity_cars);
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
		Session.getActiveSession().onActivityResult(CarsActivity.this, requestCode, resultCode, data);
	}
}
