package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.LuggageAfterScanFragment;

public class LuggageAfterScanActivity extends AppCompatActivity {

	public static Intent createIntent(Context context) {
		return new Intent(context, LuggageAfterScanActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.fragment_container_with_toolbar);

		LuggageAfterScanFragment luggageAfterScanFragment = new LuggageAfterScanFragment();

		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, luggageAfterScanFragment).commit();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(getResources().getString(R.string.luggage_tag_scanned));
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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