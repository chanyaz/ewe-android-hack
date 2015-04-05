package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXBaseActivity extends ActionBarActivity {

	@InjectView(R.id.lx_base_presenter)
	LXPresenter lxPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		setContentView(R.layout.lx_base_layout);

		ButterKnife.inject(this);
		Events.register(this);
		Ui.showTransparentStatusBar(this);
	}

	@Override
	public void onBackPressed() {
		if (!lxPresenter.back()) {
			super.onBackPressed();
		}
	}

}
