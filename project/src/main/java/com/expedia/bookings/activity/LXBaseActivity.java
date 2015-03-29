package com.expedia.bookings.activity;

import org.joda.time.LocalDate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TimeFormatException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXBaseActivity extends ActionBarActivity {

	private static final String TAG = "LXBaseActivity";

	@InjectView(R.id.lx_base_presenter)
	LXPresenter lxPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		setContentView(R.layout.lx_base_layout);
		ButterKnife.inject(this);
		Ui.showTransparentStatusBar(this);
		fromDeepLink();
	}

	private void fromDeepLink() {
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null && intent.getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
			LXSearchParams params = new LXSearchParams();
			params.location = intent.getStringExtra("location");
			String startDateStr = intent.getStringExtra("startDateStr");
			params.startDate = getStartDate(startDateStr);
			params.endDate = params.startDate.plusDays(R.integer.lx_default_search_range);
			Events.LXNewSearchParamsAvailable event = new Events.LXNewSearchParamsAvailable(params);
			Events.post(event);
		}
	}

	private LocalDate getStartDate(String startDateStr) {
		LocalDate startDate = LocalDate.now();
		try {
			startDate = DateUtils.yyyyMMddToLocalDate(startDateStr);
			Log.d(TAG, "Set activity search date: " + startDate);
		}
		catch (TimeFormatException | IllegalArgumentException e) {
			Log.w(TAG, "Could not parse check in date: " + startDateStr, e);
		}
		return startDate;
	}

	@Override
	public void onBackPressed() {
		if (!lxPresenter.back()) {
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
	}

	@Subscribe
	public void onFinishActivity(Events.FinishActivity event) {
		finish();
	}

}
