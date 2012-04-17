package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity.SetShowDistanceListener;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelListFragment.HotelListFragmentListener;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class SearchListActivity extends FragmentActivity implements SearchListener,
		OnFilterChangedListener, SetShowDistanceListener, HotelListFragmentListener {

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private HotelListFragment mHotelListFragment;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHotelListFragment = Ui.findOrAddSupportFragment(this,
				HotelListFragment.class, getString(R.string.tag_hotel_list));

		PhoneSearchActivity parent = (PhoneSearchActivity) getParent();
		parent.addSearchListener(this);
		parent.addSetShowDistanceListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener

	@Override
	public void onFilterChanged() {
		Log.i("onFilterChanged() in SearchListACtivity!");

		mHotelListFragment.notifyFilterChanged();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		clearResults();
		Db.getFilter().removeOnFilterChangedListener(this);
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		mHotelListFragment.notifySearchComplete();
		Db.getFilter().addOnFilterChangedListener(this);
	}

	@Override
	public void onSetShowDistance(boolean showDistance) {
		mHotelListFragment.setShowDistances(showDistance);
	}

	@Override
	public void clearResults() {
		mHotelListFragment.notifySearchStarted();
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelListFragmentListener

	@Override
	public void onSortButtonClicked() {
		// Do nothing, this shouldn't even appear
	}

	@Override
	public void onListItemClicked(Property property, int position) {
		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, Db.getSearchParams().toString());
		intent.putExtra(HotelActivity.EXTRA_POSITION, position);
		startActivity(intent);
	}
}