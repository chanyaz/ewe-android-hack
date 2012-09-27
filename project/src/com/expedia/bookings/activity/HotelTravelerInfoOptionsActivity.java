package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.HotelTravelerInfoOptionsFragment;
import com.mobiata.android.Log;

public class HotelTravelerInfoOptionsActivity extends SherlockFragmentActivity {
	private HotelTravelerInfoOptionsFragment mHotelTravelerInfoOptionsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hotel_traveler_info_options);

		mHotelTravelerInfoOptionsFragment = (HotelTravelerInfoOptionsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.hotel_traveler_info_options_fragment);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_hotel_traveler_info_options, menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		ViewGroup titleView = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getSelectedProperty();
		if (property == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return false;
		}

		((TextView) titleView.findViewById(R.id.title)).setText(property.getName());
		((RatingBar) titleView.findViewById(R.id.rating)).setRating((float) property.getHotelRating());

		actionBar.setCustomView(titleView);

		final MenuItem doneMenuItem = menu.findItem(R.id.menu_done);
		final Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_done, null);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(doneMenuItem);
			}
		});

		doneMenuItem.setActionView(tv);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		case R.id.menu_done: {
			syncBillingInfo();
			finish();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	private void syncBillingInfo() {
		final List<Traveler> travelers = new ArrayList<Traveler>();
		travelers.add(mHotelTravelerInfoOptionsFragment.createTravler());

		Db.setTravelers(travelers);
	}
}