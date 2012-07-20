package com.expedia.bookings.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightTripOverviewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.widget.FlightAdapter;
import com.mobiata.android.util.Ui;

// IMPLEMENTATION NOTE: This implementation heavily leans towards the user only picking
// two legs of a flight (outbound and inbound).  If you want to adapt it for 3+ legs, you
// will need to rewrite a good portion of it.
public class FlightListFragment extends ListFragment {

	public static final String TAG = FlightListFragment.class.getName();

	private static final String INSTANCE_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private FlightAdapter mAdapter;

	private ImageView mHeaderImage;
	private SectionFlightLeg mSectionFlightLeg;

	private Drawable mHeaderDrawable;

	private int mLegPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mLegPosition = savedInstanceState.getInt(INSTANCE_LEG_POSITION);
		}
		else {
			mLegPosition = 0;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// If the parent is finishing already, don't bother with displaying the data
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		// Configure the header
		ListView lv = Ui.findView(v, android.R.id.list);
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.snippet_flight_header, lv, false);
		mHeaderImage = Ui.findView(header, R.id.background);
		mSectionFlightLeg = Ui.findView(header, R.id.flight_leg);
		lv.addHeaderView(header);
		lv.setHeaderDividersEnabled(false);

		displayHeaderDrawable();
		displayHeaderLeg();

		// Add the adapter
		mAdapter = new FlightAdapter(getActivity(), savedInstanceState);
		setListAdapter(mAdapter);

		// Set initial data
		loadList();

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(INSTANCE_LEG_POSITION, mLegPosition);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Set the leg as selected
		FlightTrip trip = mAdapter.getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);
		FlightSearch flightSearch = Db.getFlightSearch();
		flightSearch.setSelectedLeg(mLegPosition, new FlightTripLeg(trip, leg));

		// If we need to select another leg, continue; otherwise go to next page
		if (flightSearch.getSelectedFlightTrip() == null) {
			mLegPosition++;

			displayHeaderLeg();

			loadList();
		}
		else {
			Intent intent = new Intent(getActivity(), FlightTripOverviewActivity.class);
			intent.putExtra(FlightTripOverviewActivity.EXTRA_TRIP_KEY, trip.getProductKey());
			startActivity(intent);
		}
	}

	/**
	 * We want to be able to handle back presses
	 * @return true if back press was consumed, false otherwise
	 */
	public boolean onBackPressed() {
		if (mLegPosition > 0) {
			Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
			mLegPosition--;
			loadList();

			displayHeaderLeg();

			return true;
		}

		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// Header control

	public void setHeaderDrawable(Drawable drawable) {
		mHeaderDrawable = drawable;
		displayHeaderDrawable();
	}

	private void displayHeaderDrawable() {
		if (mHeaderImage != null) {
			if (mHeaderDrawable == null) {
				mHeaderImage.setVisibility(View.GONE);
			}
			else {
				mHeaderImage.setVisibility(View.VISIBLE);
				mHeaderImage.setImageDrawable(mHeaderDrawable);
			}
		}
	}

	private void displayHeaderLeg() {
		if (mSectionFlightLeg != null) {
			if (mLegPosition == 0) {
				mSectionFlightLeg.setVisibility(View.GONE);
			}
			else {
				mSectionFlightLeg.setVisibility(View.VISIBLE);
				mSectionFlightLeg.bind(Db.getFlightSearch().getSelectedLegs()[0]);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// List control

	// Call whenever leg position changes
	public void loadList() {
		mAdapter.setLegPosition(mLegPosition);
		mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));

		// Scroll to top after reloading list with new results
		if (getView() != null) {
			getListView().setSelection(0);
		}
	}
}
