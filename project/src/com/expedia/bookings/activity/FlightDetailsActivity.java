package com.expedia.bookings.activity;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.TripFragment;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.NavigationButton;
import com.expedia.bookings.widget.NavigationDropdownAdapter;

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

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

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

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		NavigationButton nb = NavigationButton.createNewInstanceAndAttach(this, R.drawable.icon, actionBar);
		nb.setDropdownAdapter(new NavigationDropdownAdapter(this));
		nb.setTitle(getTitle());
		
	}

	public void exitDisplay() {
		// All we do here is finish the current activity to go back to the screen before.
		finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_details, menu);

		menu.findItem(R.id.select_leg).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				exitDisplay();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exitDisplay();
			break;
		case R.id.select_leg:
			exitDisplay();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
