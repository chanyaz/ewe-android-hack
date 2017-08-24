package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightRoutes;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightRouteAdapter extends BaseAdapter {


	private final Context mContext;

	private final RecentList<Location> mRecentSearches;

	private final FlightRoutes mRoutes;

	private final List<Row> mRows = new ArrayList<>();

	private final boolean mIsOrigin;

	private String mOrigin;

	private FlightRouteAdapterListener mListener;

	private final int mDropDownRowPaddingLeft;

	private final boolean dropDownMode;

	private final boolean rowDividersEnabled;

	@LayoutRes private final int dropdownLayoutResourceId;

	public FlightRouteAdapter(Context context, FlightRoutes routes, RecentList<Location> recentSearches,
		boolean isOrigin) {
		this(context, routes, recentSearches, isOrigin, false, true, R.layout.spinner_airport_dropdown_row);
	}

	public FlightRouteAdapter(Context context, FlightRoutes routes, RecentList<Location> recentSearches,
		boolean isOrigin, boolean dropDownMode, boolean rowDividersEnabled, @LayoutRes int dropdownLayoutResourceId) {
		this.dropDownMode = dropDownMode;
		this.mContext = context;
		this.mRecentSearches = recentSearches;
		this.mRoutes = routes;
		this.mIsOrigin = isOrigin;
		this.mDropDownRowPaddingLeft = context.getResources().getDimensionPixelSize(
			R.dimen.flight_search_airport_padding_left);
		this.rowDividersEnabled = rowDividersEnabled;
		this.dropdownLayoutResourceId = dropdownLayoutResourceId;
		generateRows();
	}

	public void setListener(FlightRouteAdapterListener listener) {
		mListener = listener;
	}

	public void setOrigin(String origin) {
		mOrigin = origin;
		onDataSetChanged();
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
		return 1;
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
		Row row = mRows.get(position);
		if (this.dropDownMode) {
			return row.getDropDownView(position, convertView, parent);
		}
		else {
			return row.getView(convertView, parent);
		}
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (mListener != null) {
			mListener.onSpinnerClicked();
		}

		// Note: convertView doesn't work properly in getDropDownView(), because
		// it assumes there's only one View type (for some dumb reason).  So we
		// use a single row that we modify visibility on so we can get some reuse.
		return mRows.get(position).getDropDownView(position, convertView, parent);
	}

	//////////////////////////////////////////////////////////////////////////
	// Data generation

	private void generateRows() {
		mRows.clear();

		// Get our airports
		List<Airport> airports = new ArrayList<>();

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
			boolean addedRecent = false;

			for (Location recent : mRecentSearches.getList()) {
				Airport airport = mRoutes.getAirport(recent.getDestinationId());
				if (airport != null && airports.contains(airport)) {
					mRows.add(new AirportRow(airport));
					addedRecent = true;
				}
			}

			// Only add recent row if we've had any valid recents to show
			if (addedRecent) {
				mRows.add(1, new HeaderRow(mContext.getString(R.string.recent)));
			}
		}

		String currCountry = null;
		for (Airport airport : airports) {
			// Add header row if the country changes
			if (currCountry == null || !currCountry.equals(airport.mCountryCode)) {
				currCountry = airport.mCountryCode;
				mRows.add(new HeaderRow(currCountry));
			}

			// Add the airport
			mRows.add(new AirportRow(airport));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface FlightRouteAdapterListener {
		void onSpinnerClicked();
	}

	//////////////////////////////////////////////////////////////////////////
	// Internal classes

	private enum RowType {
		HINT,
		HEADER,
		AIRPORT
	}

	private interface Row {
		View getView(View convertView, ViewGroup parent);

		View getDropDownView(int position, View convertView, ViewGroup parent);

		RowType getViewType();
	}

	private class HintRow implements Row {

		@Override
		public View getView(View convertView, ViewGroup parent) {
			TextView textView;
			if (convertView == null) {
				textView = Ui.inflate(getAirportLayoutResId(), parent, false);
			}
			else {
				textView = (TextView) convertView;
			}

			textView.setHint(mIsOrigin ? R.string.hint_departure_airport : R.string.hint_arrival_airport);

			return textView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			convertView = useDropDownConvertView(convertView, parent);
			DropDownViewHolder holder = (DropDownViewHolder) convertView.getTag();

			// Make everything invisible
			holder.mAirportLocationTextView.setVisibility(View.GONE);
			holder.mAirportDetailsTextView.setVisibility(View.GONE);
			holder.mHeaderTextView.setVisibility(View.GONE);
			setDropDownRowBackground(holder, 0);

			return convertView;
		}

		@Override
		public RowType getViewType() {
			return RowType.HINT;
		}

	}

	private class HeaderRow implements Row {
		private final String mText;

		public HeaderRow(String country) {
			mText = country;
		}

		@Override
		public View getView(View convertView, ViewGroup parent) {
			// This row is not selectable, so this should never happen
			return null;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			convertView = useDropDownConvertView(convertView, parent);
			DropDownViewHolder holder = (DropDownViewHolder) convertView.getTag();

			// Configure visibility for airport style
			holder.mAirportLocationTextView.setVisibility(View.GONE);
			holder.mAirportDetailsTextView.setVisibility(View.GONE);
			holder.mHeaderTextView.setVisibility(View.VISIBLE);

			setDropDownRowBackground(holder, R.drawable.bg_textview_divider_large_dark);

			holder.mHeaderTextView.setText(mText);

			return convertView;
		}

		@Override
		public RowType getViewType() {
			return RowType.HEADER;
		}

	}

	private class AirportRow implements Row {
		private final Airport mAirport;

		public AirportRow(Airport airport) {
			mAirport = airport;
		}

		@Override
		public View getView(View convertView, ViewGroup parent) {
			TextView textView;
			if (convertView == null) {
				textView = Ui.inflate(getAirportLayoutResId(), parent, false);
			}
			else {
				textView = (TextView) convertView;
			}

			textView.setText(HtmlCompat.fromHtml(mContext.getString(R.string.dropdown_airport_selection,
				mAirport.mAirportCode, mAirport.mName)));

			return textView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			convertView = useDropDownConvertView(convertView, parent);
			DropDownViewHolder holder = (DropDownViewHolder) convertView.getTag();

			// Configure visibility for airport style
			holder.mAirportLocationTextView.setVisibility(View.VISIBLE);
			holder.mAirportDetailsTextView.setVisibility(View.VISIBLE);
			holder.mHeaderTextView.setVisibility(View.GONE);

			// Disable the divider if this is the last row before a country
			if (mRows.size() == position + 1 || mRows.get(position + 1).getViewType() == RowType.HEADER) {
				setDropDownRowBackground(holder, 0);
			}
			else {
				setDropDownRowBackground(holder, R.drawable.bg_textview_divider_small_dark);
			}

			holder.mAirportLocationTextView.setText(mAirport.mName + ", " + mAirport.mCountryCode);
			Airport fullAirport = FlightStatsDbUtils.getAirport(mAirport.mAirportCode);
			holder.mAirportDetailsTextView.setText(mAirport.mAirportCode + " - " + fullAirport.mName);

			return convertView;
		}

		@Override
		public RowType getViewType() {
			return RowType.AIRPORT;
		}

		public Airport getAirport() {
			return mAirport;
		}
	}

	private View useDropDownConvertView(View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = Ui.inflate(dropdownLayoutResourceId, parent, false);

			DropDownViewHolder holder = new DropDownViewHolder();
			holder.mContainer = convertView;
			holder.mAirportLocationTextView = Ui.findView(convertView, R.id.airport_location_text_view);
			holder.mAirportDetailsTextView = Ui.findView(convertView, R.id.airport_details_text_view);
			holder.mHeaderTextView = Ui.findView(convertView, R.id.header_text_view);

			holder.mHeaderTextView.setAllCaps(true);

			convertView.setTag(holder);
		}

		return convertView;
	}

	private void setDropDownRowBackground(DropDownViewHolder holder, int bgResId) {
		if (!rowDividersEnabled) {
			return;
		}
		holder.mContainer.setBackgroundResource(bgResId);
		holder.mContainer.setPadding(mDropDownRowPaddingLeft, 0, 0, 0);
	}

	private static class DropDownViewHolder {
		View mContainer;
		TextView mAirportLocationTextView;
		TextView mAirportDetailsTextView;
		TextView mHeaderTextView;
	}

	private int getAirportLayoutResId() {
		return mIsOrigin ? R.layout.spinner_airport_row_departure : R.layout.spinner_airport_row_arrival;
	}
}
