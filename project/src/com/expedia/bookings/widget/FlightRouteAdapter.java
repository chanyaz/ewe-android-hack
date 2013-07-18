package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightRoutes;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.mobiata.flightlib.data.Airport;

public class FlightRouteAdapter extends BaseAdapter {

	private Context mContext;

	private RecentList<Location> mRecentSearches;

	private FlightRoutes mRoutes;

	private List<Row> mRows = new ArrayList<FlightRouteAdapter.Row>();

	private boolean mIsOrigin;

	private String mOrigin;

	public FlightRouteAdapter(Context context, FlightRoutes routes, RecentList<Location> recentSearches,
			boolean isOrigin) {
		mContext = context;
		mRecentSearches = recentSearches;
		mRoutes = routes;
		mIsOrigin = isOrigin;
		generateRows();
	}

	public void setOrigin(String origin) {
		mOrigin = origin;
	}

	public void onDataSetChanged() {
		generateRows();
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return mRows.get(position).getViewType().ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return RowType.values().length;
	}

	@Override
	public int getCount() {
		return mRows.size();
	}

	@Override
	public Row getItem(int position) {
		return mRows.get(position);
	}

	public Airport getAirport(int position) {
		Row row = mRows.get(position);

		if (row.getViewType() == RowType.AIRPORT) {
			return ((AirportRow) row).getAirport();
		}

		return null;
	}

	public int getPosition(String airportCode) {
		for (int a = 0; a < mRows.size(); a++) {
			Row row = mRows.get(a);
			if (row.getViewType() == RowType.AIRPORT
					&& ((AirportRow) row).getAirport().mAirportCode.equals(airportCode)) {
				return a;
			}
		}

		return 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItem(position).getViewType() == RowType.AIRPORT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mRows.get(position).getView(position, convertView, parent);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO: Customize drop down view
		return super.getDropDownView(position, convertView, parent);
	}

	//////////////////////////////////////////////////////////////////////////
	// Data generation

	private void generateRows() {
		mRows.clear();

		// Get our airports
		List<Airport> airports = new ArrayList<Airport>();

		if (mIsOrigin) {
			airports.addAll(mRoutes.getOrigins());
		}
		else {
			if (TextUtils.isEmpty(mOrigin)) {
				airports.addAll(mRoutes.getAllDestinations());
			}
			else {
				airports.addAll(mRoutes.getDestinations(mOrigin));
			}
		}

		// Sort
		Collections.sort(airports, new Comparator<Airport>() {
			@Override
			public int compare(Airport lhs, Airport rhs) {
				// Compare country names first
				int cmp = lhs.mCountryCode.compareTo(rhs.mCountryCode);

				if (cmp != 0) {
					return cmp;
				}

				// Compare name next
				return lhs.mName.compareTo(rhs.mName);
			}
		});

		// Add rows to data set 
		mRows.add(new HintRow());

		// Add recents
		if (!mRecentSearches.isEmpty()) {
			mRows.add(new RecentRow());

			for (Location recent : mRecentSearches.getList()) {
				Airport airport = mRoutes.getAirport(recent.getDestinationId());
				if (airport != null) {
					mRows.add(new AirportRow(airport));
				}
			}
		}

		String currCountry = null;
		for (Airport airport : airports) {
			// Add header row if the country changes
			if (currCountry == null || !currCountry.equals(airport.mCountryCode)) {
				currCountry = airport.mCountryCode;
				mRows.add(new CountryRow(currCountry));
			}

			// Add the airport
			mRows.add(new AirportRow(airport));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Internal classes

	private enum RowType {
		HINT,
		COUNTRY,
		RECENT,
		AIRPORT
	}

	private interface Row {
		public View getView(int position, View convertView, ViewGroup parent);

		public RowType getViewType();
	}

	private class HintRow implements Row {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO: Implement actual view here
			android.widget.TextView textView = (android.widget.TextView) LayoutInflater.from(mContext).inflate(
					android.R.layout.simple_list_item_1,
					parent, false);

			if (mIsOrigin) {
				textView.setText(R.string.hint_departure_airport);
			}
			else {
				textView.setText(R.string.hint_arrival_airport);
			}

			return textView;
		}

		@Override
		public RowType getViewType() {
			return RowType.HINT;
		}

	}

	private class CountryRow implements Row {
		private String mCountry;

		public CountryRow(String country) {
			mCountry = country;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO: Implement actual view here
			android.widget.TextView textView = (android.widget.TextView) LayoutInflater.from(mContext).inflate(
					android.R.layout.simple_list_item_1,
					parent, false);
			textView.setText(mCountry);
			return textView;
		}

		@Override
		public RowType getViewType() {
			return RowType.COUNTRY;
		}

	}

	private class RecentRow implements Row {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO: Implement actual view here
			android.widget.TextView textView = (android.widget.TextView) LayoutInflater.from(mContext).inflate(
					android.R.layout.simple_list_item_1,
					parent, false);
			textView.setText(R.string.recently_used);
			return textView;
		}

		@Override
		public RowType getViewType() {
			return RowType.RECENT;
		}

	}

	private class AirportRow implements Row {
		private Airport mAirport;

		public AirportRow(Airport airport) {
			mAirport = airport;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO: Implement actual view here
			android.widget.TextView textView = (android.widget.TextView) LayoutInflater.from(mContext).inflate(
					android.R.layout.simple_list_item_1,
					parent, false);
			textView.setText(mAirport.mName);
			return textView;
		}

		@Override
		public RowType getViewType() {
			return RowType.AIRPORT;
		}

		public Airport getAirport() {
			return mAirport;
		}
	}

}
