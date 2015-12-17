package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.HotelPayLaterInfoFragment;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;

public class HotelPayLaterInfoActivity extends FragmentActivity {

	private static final String FRAGMENT_PAY_LATER_INFO_TAG = "FRAGMENT_PAY_LATER_INFO_TAG";

	private ActivityKillReceiver mKillReceiver;

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, HotelPayLaterInfoActivity.class);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (HotelUtils.checkPhoneFinishConditionsAndFinish(this)) {
			return;
		}
		Ui.findOrAddSupportFragment(this, HotelPayLaterInfoFragment.class, FRAGMENT_PAY_LATER_INFO_TAG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setCustomView(Ui.inflate(this, R.layout.actionbar_etp_info, null));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// app icon in action bar clicked; go back
			onBackPressed();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}
}
