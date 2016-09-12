package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AirportAutocompleteProvider;
import com.expedia.bookings.data.AirportSuggestion;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.SuggestionUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class AirportDropDownAdapter extends ArrayAdapter<AirportSuggestion> implements Filterable {
	// Where we save the recent airport searches
	public static final String RECENT_AIRPORTS_FILE = "recent-airports-list.dat";

	private static final int DEFAULT_MAX_NEARBY = 2;

	private Context mContext;
	private RecentList<Location> mRecentSearches;
	private boolean mShowNearbyAirports;

	private ArrayList<AirportSuggestion> data = new ArrayList<>();
	private ContentResolver mContent;
	private SuggestFilter mFilter = new SuggestFilter();
	private LayoutInflater mInflater;

	public AirportDropDownAdapter(Context context) {
		super(context, R.layout.simple_dropdown_item_2line);

		mContext = context;
		mContent = context.getContentResolver();
		mInflater = LayoutInflater.from(getContext());

		mRecentSearches = new RecentList<>(Location.class, context, RECENT_AIRPORTS_FILE, FlightSearchParamsFragment.MAX_RECENTS);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public AirportSuggestion getItem(int position) {
		return data.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AirportSuggestion suggestionV2 = data.get(position);
		ViewHolder vh;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
			vh = new ViewHolder();
			vh.mIcon1 = Ui.findView(convertView, R.id.icon1);
			vh.mTextView1 = Ui.findView(convertView, R.id.text1);
			vh.mTextView2 = Ui.findView(convertView, R.id.text2);
			convertView.setTag(vh);
		}
		else {
			vh = (ViewHolder) convertView.getTag();
		}

		int iconResId = suggestionV2.getIcon();
		if (iconResId == 0) {
			vh.mIcon1.setVisibility(View.GONE);
		}
		else {
			vh.mIcon1.setImageResource(iconResId);
			vh.mIcon1.setVisibility(View.VISIBLE);
		}

		vh.mTextView1.setText(suggestionV2.getText1());
		vh.mTextView2.setText(suggestionV2.getText2());
		return convertView;
	}

	public Filter getFilter() {
		return mFilter;
	}

	private class SuggestFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			final ArrayList<AirportSuggestion> suggestionV2s = new ArrayList<>();

			Cursor c;

			if (TextUtils.isEmpty(constraint)) {
				int a = 0;
				MatrixCursor cursor = new MatrixCursor(AirportAutocompleteProvider.COLUMNS);

				if (mShowNearbyAirports) {
					List<SuggestionV2> airportSuggestions = SuggestionUtils
						.getNearbyAirportSuggestions(mContext, DEFAULT_MAX_NEARBY);
					Airport airport;

					for (SuggestionV2 suggestion : airportSuggestions) {
						airport = FlightStatsDbUtils.getAirport(suggestion.getAirportCode());
						Object[] row = new Object[AirportAutocompleteProvider.COLUMNS.length];
						row[0] = a++;
						row[1] = StrUtils.formatAirport(airport, null);
						row[2] = airport.mAirportCode + "-" + airport.mName;
						row[3] = airport.mAirportCode;
						row[4] = R.drawable.ic_nearby_search;
						cursor.addRow(row);
					}
				}

				for (Location location : mRecentSearches.getList()) {
					Object[] row = new Object[AirportAutocompleteProvider.COLUMNS.length];
					row[0] = a++;
					row[1] = location.getCity();
					row[2] = location.getDescription();
					row[3] = location.getDestinationId();
					row[4] = R.drawable.ic_recent_search;
					row[5] = location.getCountryCode();
					cursor.addRow(row);
				}

				c = cursor;
			}
			else {
				Uri uri = Uri.withAppendedPath(
					AirportAutocompleteProvider.getContentFilterUri(mContext),
					Uri.encode(constraint.toString()));

				c = mContent.query(uri, null, null, null, null);
			}

			while (c.moveToNext()) {
				suggestionV2s.add(AirportAutocompleteProvider.rowToSuggestion(c));
			}

			results.values = suggestionV2s;
			results.count = suggestionV2s.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			data = (ArrayList<AirportSuggestion>) results.values;
			if (data == null) {
				data = new ArrayList<>();
			}
			notifyDataSetChanged();
		}

	}


	public void setShowNearbyAirports(boolean showNearbyAirports) {
		mShowNearbyAirports = showNearbyAirports;
	}

	private static class ViewHolder {
		private ImageView mIcon1;
		private TextView mTextView1;
		private TextView mTextView2;
	}

	public Location getLocation(int position) {
		if (data == null || position >= data.size()) {
			return null;
		}

		return data.get(position).getLocation();
	}

	//////////////////////////////////////////////////////////////////////////
	// RecentSearchList interaction

	public void onAirportSelected(Location location) {
		// Don't save if it's a completely custom code and we don't have any info on it
		if (!TextUtils.isEmpty(location.getCity()) && !TextUtils.isEmpty(location.getDescription())) {
			mRecentSearches.addItem(location);

			(new Thread(new Runnable() {
				@Override
				public void run() {
					mRecentSearches.saveList(mContext, RECENT_AIRPORTS_FILE);
				}
			})).start();
		}
	}

	public static void addAirportToRecents(Context context, Location location) {
		RecentList<Location> recents = new RecentList<Location>(Location.class, context, RECENT_AIRPORTS_FILE,
			FlightSearchParamsFragment.MAX_RECENTS);
		recents.addItem(location);
		recents.saveList(context, RECENT_AIRPORTS_FILE);
	}

	public static void clearRecentAirports(Context context) {
		RecentList<Location> recents = new RecentList<Location>(Location.class);
		recents.saveList(context, RECENT_AIRPORTS_FILE);
	}
}
