package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.CarsState;
import com.expedia.bookings.presenter.CarsPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarsActivity extends Activity {

	private static final String KEY_LAYOUT_RES = "KEY_LAYOUT_RES";

	@InjectView(R.id.cars_presenter)
	CarsPresenter carsPresenter;

	public static Intent createIntent(Context context, @LayoutRes int layoutResId) {
		Intent intent = new Intent(context, CarsActivity.class);
		intent.putExtra(KEY_LAYOUT_RES, layoutResId);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getIntent().getIntExtra(KEY_LAYOUT_RES, R.layout.activity_cars));

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
