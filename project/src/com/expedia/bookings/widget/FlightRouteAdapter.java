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

import com.expedia.bookings.data.FlightRoutes;
import com.mobiata.flightlib.data.Airport;

public class FlightRouteAdapter extends BaseAdapter {

	private Context mContext;

	private FlightRoutes mRoutes;

	private List<Row> mRows = new ArrayList<FlightRouteAdapter.Row>();

	private String mOrigin;

	public FlightRouteAdapter(Context context, FlightRoutes routes) {
		mContext = context;
		mRoutes = routes;
		generateRows();
	}

	public void setOrigin(String origin) {
		mOrigin = origin;
		generateRows();
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return mRows.get(position).getViewType();
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
	public Object getItem(int position) {
		return mRows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
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
		if (TextUtils.isEmpty(mOrigin)) {
			airports.addAll(mRoutes.getOrigins());
		}
		else {
			airports.addAll(mRoutes.getDestinations(mOrigin));
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
		COUNTRY,
		AIRPORT
	}

	private interface Row {
		public View getView(int position, View convertView, ViewGroup parent);

		public int getViewType();
	}

	private class CountryRow implements Row {
		private String mCountry;

		public CountryRow(String country) {
			mCountry = country;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO: Implement actual view here
			TextView textView = (TextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1,
					parent);
			textView.setText(mCountry);
			return textView;
		}

		@Override
		public int getViewType() {
			return RowType.COUNTRY.ordinal();
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
			TextView textView = (TextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1,
					parent);
			textView.setText(mAirport.mName);
			return textView;
		}

		@Override
		public int getViewType() {
			return RowType.AIRPORT.ordinal();
		}
	}
}
