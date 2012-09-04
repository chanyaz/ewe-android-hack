package com.expedia.bookings.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;

public class FlightSearchOverlayActivity extends SherlockFragmentActivity {

	public static final String EXTRA_SEARCH_PARAMS = "EXTRA_SEARCH_PARAMS";

	private FlightSearchParamsFragment mSearchParamsFragment;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View root = findViewById(android.R.id.content);
		root.setBackgroundColor(Color.argb(180, 0, 0, 0));

		setTitle(R.string.edit_search);

		if (savedInstanceState == null) {
			mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
					false);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mSearchParamsFragment,
					FlightSearchParamsFragment.TAG).commit();
		}
		else {
			mSearchParamsFragment = Ui.findSupportFragment(this, FlightSearchParamsFragment.TAG);
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_search_overlay, menu);
		final MenuItem searchItem = menu.findItem(R.id.search);
		searchItem.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(searchItem);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			Intent intent = new Intent();
			JSONUtils.putJSONable(intent, EXTRA_SEARCH_PARAMS, mSearchParamsFragment.getSearchParams());
			setResult(RESULT_OK, intent);
			finish();
			return true;
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
