package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewTreeObserver;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.CarPresenter;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.facebook.Session;
import com.mobiata.android.Log;
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
		handleNavigationViaDeepLink();
	}

	@Override
	public void onBackPressed() {
		if (!carsPresenter.back()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);

		if (isFinishing()) {
			clearCCNumber();
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

	public void clearCCNumber() {
		try {
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null);
			Db.getBillingInfo().setNumber(null);
		}
		catch (Exception ex) {
			Log.e("Error clearing billingInfo card number", ex);
		}
	}

	private void handleNavigationViaDeepLink() {
		Intent intent = getIntent();
		final String productKey = intent.getStringExtra(Codes.CARS_PRODUCT_KEY);
		final boolean navigateToSearchResults =
			(intent != null) && (intent.getStringExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS) != null) && (intent
				.getBooleanExtra(Codes.FROM_DEEPLINK, false)) && (Strings.isEmpty(productKey));
		final boolean navigateToDetails =
			(intent != null) && (intent.getStringExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS) != null) && (intent
				.getBooleanExtra(Codes.FROM_DEEPLINK, false)) && (Strings
				.isNotEmpty(productKey));

		final CarSearchParams carSearchParams = CarDataUtils.getCarSearchParams(intent);

		carsPresenter.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				carsPresenter.getViewTreeObserver().removeOnPreDrawListener(this);
				if (navigateToSearchResults && carSearchParams != null) {
					Events.post(new Events.CarsNewSearchParams(carSearchParams));
					return true;
				}
				if (navigateToDetails && carSearchParams != null) {
					Events.post(new Events.CarsNewSearchParams(carSearchParams, productKey));
				}
				return true;
			}
		});
	}
}
