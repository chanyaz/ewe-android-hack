package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.PayLaterInfoFragment;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;

public class PayLaterInfoFragmentActivity extends FragmentActivity {

	private static final String FRAGMENT_PAY_LATER_INFO_TAG = "FRAGMENT_PAY_LATER_INFO_TAG";

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, PayLaterInfoFragmentActivity.class);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (HotelUtils.checkPhoneFinishConditionsAndFinish(this)) {
			return;
		}
		Ui.findOrAddSupportFragment(this, PayLaterInfoFragment.class, FRAGMENT_PAY_LATER_INFO_TAG);
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
			Intent intent = HotelDetailsFragmentActivity.createIntent(this);
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
