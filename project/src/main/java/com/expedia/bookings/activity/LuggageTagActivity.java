package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.LuggageScanFoundSmartTagFragment;
import com.expedia.bookings.utils.Ui;

public class LuggageTagActivity extends AppCompatActivity {
	private static final String TAG_SCAN_FOUND_SMART_TAG = "TAG_SCAN_FOUND_SMART_TAG";
	private LuggageScanFoundSmartTagFragment mScanFoundSmartTag;

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, LuggageTagActivity.class);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.fragment_container_with_toolbar);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(getResources().getString(R.string.luggage_tag));
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//
//		mScanFoundSmartTag = Ui.findSupportFragment(this, TAG_SCAN_FOUND_SMART_TAG);
//
//  		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mScanFoundSmartTag).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
