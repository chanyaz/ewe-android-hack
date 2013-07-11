package com.expedia.bookings.widget;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AirportAutocompleteProvider;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class AirportDropDownAdapter extends CursorAdapter {
	// Where we save the recent airport searches
	public static final String RECENT_AIRPORTS_FILE = "recent-airports-list.dat";

	private static final int MAX_RECENTS = 10;

	// Minimum time ago that we will use location stats
	private static final long MINIMUM_TIME_AGO = 1000 * 60 * 60; // 1 hour

	// Maximum # of nearby airports to report
	private static final int MAX_NEARBY = 2;

	private Map<String, String> mCountryCodeMap;

	private ContentResolver mContent;

	private RecentList<Location> mRecentSearches;

	private boolean mShowNearbyAirports;

	public AirportDropDownAdapter(Context context) {
		super(context, null, 0);

		mContent = context.getContentResolver();

		mRecentSearches = new RecentList<Location>(Location.class, context, RECENT_AIRPORTS_FILE, MAX_RECENTS);

		Resources r = context.getResources();
		mCountryCodeMap = new HashMap<String, String>();
		String[] countryCodes = r.getStringArray(R.array.country_codes);
		String[] countryNames = r.getStringArray(R.array.country_names);
		for (int a = 0; a < countryCodes.length; a++) {
			mCountryCodeMap.put(countryCodes[a], countryNames[a]);
		}
	}

	public void setShowNearbyAirports(boolean showNearbyAirports) {
		mShowNearbyAirports = showNearbyAirports;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (TextUtils.isEmpty(constraint)) {
			int a = 0;
			MatrixCursor cursor = new MatrixCursor(AirportAutocompleteProvider.COLUMNS);

			// Add nearby airports if we know the user's recent location
			long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
			android.location.Location loc = LocationServices.getLastBestLocation(mContext, minTime);
			if (mShowNearbyAirports && loc != null) {
				List<Airport> airports = FlightStatsDbUtils.getNearestAirports(loc.getLatitude(), loc.getLongitude(),
						true, MAX_NEARBY);

				for (Airport airport : airports) {
					Object[] row = new Object[AirportAutocompleteProvider.COLUMNS.length];
					row[0] = a++;
					row[1] = StrUtils.formatAirport(airport);
					row[2] = airport.mAirportCode + "-" + airport.mName;
					row[3] = airport.mAirportCode;
					row[4] = R.drawable.ic_nearby_search;
					row[5] = airport.mCountryCode;
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

			return cursor;
		}
		else {
			Uri uri = Uri.withAppendedPath(
					AirportAutocompleteProvider.CONTENT_FILTER_URI,
					Uri.encode(constraint.toString()));

			return mContent.query(uri, null, null, null, null);
		}
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_2);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder vh = (ViewHolder) view.getTag();

		int iconResId = cursor.getInt(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_ICON_1);
		if (iconResId == 0) {
			vh.mIcon1.setVisibility(View.GONE);
		}
		else {
			vh.mIcon1.setImageResource(iconResId);
			vh.mIcon1.setVisibility(View.VISIBLE);
		}

		vh.mTextView1.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_1));
		vh.mTextView2.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_2));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);

		ViewHolder vh = new ViewHolder();
		vh.mIcon1 = Ui.findView(v, android.R.id.icon1);
		vh.mTextView1 = Ui.findView(v, android.R.id.text1);
		vh.mTextView2 = Ui.findView(v, android.R.id.text2);
		v.setTag(vh);

		return v;
	}

	private static class ViewHolder {
		private ImageView mIcon1;
		private TextView mTextView1;
		private TextView mTextView2;
	}

	public Location getLocation(int position) {
		Cursor c = getCursor();

		if (c == null || c.getCount() <= position) {
			return null;
		}

		c.moveToPosition(position);

		return AirportAutocompleteProvider.createLocationFromRow(c);
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
				MAX_RECENTS);
		recents.addItem(location);
		recents.saveList(context, RECENT_AIRPORTS_FILE);
	}
}
