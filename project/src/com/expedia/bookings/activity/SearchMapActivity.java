package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentMapActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity.ExactSearchLocationSearchedListener;
import com.expedia.bookings.activity.PhoneSearchActivity.MapViewListener;
import com.expedia.bookings.activity.PhoneSearchActivity.SetShowDistanceListener;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.utils.Ui;
import com.google.android.maps.GeoPoint;

public class SearchMapActivity extends FragmentMapActivity implements SearchListener, OnFilterChangedListener,
		MapViewListener, SetShowDistanceListener, ExactSearchLocationSearchedListener, HotelMapFragmentListener {

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private HotelMapFragment mHotelMapFragment;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHotelMapFragment = Ui.findOrAddSupportFragment(this,
				HotelMapFragment.class, getString(R.string.tag_hotel_map));

		final PhoneSearchActivity parent = (PhoneSearchActivity) getParent();
		parent.addSearchListener(this);
		parent.setMapViewListener(this);
		parent.addSetShowDistanceListener(this);
		parent.addExactLocationSearchedListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		mHotelMapFragment.notifySearchStarted();
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		mHotelMapFragment.notifySearchComplete();
	}

	@Override
	public void onSetShowDistance(boolean showDistance) {
		mHotelMapFragment.setShowDistances(showDistance);
	}

	@Override
	public void clearResults() {
		mHotelMapFragment.notifySearchStarted();
	}

	@Override
	public void onFilterChanged() {
		mHotelMapFragment.notifyFilterChanged();
	}

	@Override
	public GeoPoint onRequestMapCenter() {
		return mHotelMapFragment.getCenter();
	}

	public void focusOnProperties() {
		// DO NOTHING - POSSIBLY SHOW ALL RESULTS?
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// ExactSearchLocationSearchedListener implementation

	@Override
	public void onExactSearchLocationSpecified(double latitude, double longitude, String address) {
		mHotelMapFragment.setShowDistances(true);
	}

	@Override
	public void onNoExactSearchLocationSpecified() {
		mHotelMapFragment.setShowDistances(false);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onBalloonShown(Property property) {
		// Do nothing
	}

	@Override
	public void onBalloonClicked(Property property) {
		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, Db.getSearchParams().toString());
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// FragmentMapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
