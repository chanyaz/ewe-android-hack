package com.expedia.bookings.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.TripFragment;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FlightDetailsActivity extends SherlockFragmentActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";
	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	public static final int SEATS_REMAINING_CUTOFF = 5;

	private String mTripKey;
	private int mLegPosition;
	private TripFragment mDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_details);

		mTripKey = getIntent().getStringExtra(EXTRA_TRIP_KEY);
		mLegPosition = getIntent().getIntExtra(EXTRA_LEG_POSITION, 0);

		FlightTrip trip = Db.getFlightSearch().getFlightTrip(mTripKey);
		if (trip.getSeatsRemaining() < SEATS_REMAINING_CUTOFF) {
			Ui.setText(this, R.id.flight_details_num_seats_tv, "" + trip.getSeatsRemaining());
			Ui.setText(this, R.id.flight_details_num_seats_label_tv,
					getResources().getQuantityString(R.plurals.seats_left_no_formatting, trip.getSeatsRemaining()));
			findViewById(R.id.flight_detail_info_bar_seats_left_ll).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.flight_detail_info_bar_seats_left_ll).setVisibility(View.INVISIBLE);
		}

		Ui.setText(this, R.id.flight_details_seat_price_tv,
				"" + trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));

		//TODO:We shouldn't build a new fragment every time, we should be able to reuse the existing one (during rotate)
		mDetails = Ui.findSupportFragment(this, R.id.flight_details_card_holder_ll);
		if (mDetails == null) {
			mDetails = TripFragment.newInstance(mTripKey, mLegPosition);
			getSupportFragmentManager().beginTransaction().add(R.id.flight_details_card_holder_ll, mDetails).commit();
		}

		// Enable the home button on the action bar
		if (AndroidUtils.getSdkVersion() >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	public void selectLeg() {
		//TODO:This is largely stolen from the FlightSearchResultsActivity and should at somepoint just call a method there instead of rewritting.

		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getFlightTrip(mTripKey);
		FlightLeg leg = trip.getLeg(mLegPosition);
		search.setSelectedLeg(mLegPosition, leg);

		if (mLegPosition + 1 < search.getSearchParams().getQueryLegCount()) {
			// If the user hasn't selected all legs yet, push them to select the next leg
			Intent intent = new Intent(this, FlightSearchResultsActivity.class);
			intent.putExtra(EXTRA_LEG_POSITION, mLegPosition + 1);
			startActivity(intent);
		}
		else {
			// TODO: If the user has selected all legs, go to checkout screen
			Toast.makeText(this, "TODO: All legs selected, implement checkout screen", Toast.LENGTH_SHORT).show();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_details, menu);

		//Add the select leg button
		LinearLayout selectLegBtnLL = (LinearLayout) menu.findItem(R.id.select_leg).getActionView();
		Button selectLegBtn = (Button) selectLegBtnLL.findViewById(R.id.actionbar_flights_select_leg_btn);
		selectLegBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectLeg();
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_leg:
			selectLeg();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
