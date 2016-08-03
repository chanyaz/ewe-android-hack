package com.expedia.ui;

import org.joda.time.DateTimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.car.CarPresenter;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarActivity extends AbstractAppCompatActivity {

	@InjectView(R.id.car_presenter)
	CarPresenter carsPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultCarComponents();
		setContentView(R.layout.activity_car);
		Ui.showTransparentStatusBar(this);
		ButterKnife.inject(this);
		if (getIntent() != null && getIntent().getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
			handleNavigationViaDeepLink();
		}
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
	protected void onDestroy() {
		Ui.getApplication(this).setCarComponent(null);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);

		if (isFinishing()) {
			clearCCNumber();
			clearStoredCard();
		}
	}

	@Subscribe
	public void onFinishActivity(Events.FinishActivity event) {
		finish();
	}

	private void handleNavigationViaDeepLink() {
		Intent intent = getIntent();
		final String productKey = intent.getStringExtra(Codes.CARS_PRODUCT_KEY);
		final CarSearchParams carSearchParams
			= CarDataUtils.getCarSearchParamsFromJSON(intent.getStringExtra("carSearchParams"));
		if (carSearchParams != null && carSearchParams.startDateTime != null && carSearchParams.endDateTime != null) {
			carSearchParams.startDateTime = carSearchParams.startDateTime.withZone(DateTimeZone.getDefault());
			carSearchParams.endDateTime = carSearchParams.endDateTime.withZone(DateTimeZone.getDefault());
		}

		carsPresenter.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				carsPresenter.getViewTreeObserver().removeOnPreDrawListener(this);
				if (carSearchParams != null) {
					if (Strings.isEmpty(productKey)) {
						Events.post(new Events.CarsNewSearchParams(carSearchParams));
						return true;
					}
					else {
						Events.post(new Events.CarsNewSearchParams(carSearchParams, productKey));
						return true;
					}
				}
				return true;
			}
		});
	}
}
