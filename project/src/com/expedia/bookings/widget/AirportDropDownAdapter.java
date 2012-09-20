package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AirportAutocompleteProvider;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.mobiata.android.util.Ui;

public class AirportDropDownAdapter extends CursorAdapter {
	// Where we save the recent airport searches
	public static final String RECENT_AIRPORTS_FILE = "recent-airports-list.dat";

	private Map<String, String> mCountryCodeMap;

	private ContentResolver mContent;

	private RecentList<Location> mRecentSearches;

	public AirportDropDownAdapter(Context context) {
		super(context, null, 0);

		mContent = context.getContentResolver();

		mRecentSearches = new RecentList<Location>(Location.class, context, RECENT_AIRPORTS_FILE);

		Resources r = context.getResources();
		mCountryCodeMap = new HashMap<String, String>();
		String[] countryCodes = r.getStringArray(R.array.country_codes);
		String[] countryNames = r.getStringArray(R.array.country_names);
		for (int a = 0; a < countryCodes.length; a++) {
			mCountryCodeMap.put(countryCodes[a], countryNames[a]);
		}
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (TextUtils.isEmpty(constraint)) {
			int a = 0;
			MatrixCursor cursor = new MatrixCursor(AirportAutocompleteProvider.COLUMNS);
			for (Location location : mRecentSearches.getList()) {
				Object[] row = new Object[AirportAutocompleteProvider.COLUMNS.length];
				row[0] = a++;
				row[1] = location.getCity();
				row[2] = location.getDescription();
				row[3] = location.getDestinationId();
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
		return cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_QUERY);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder vh = (ViewHolder) view.getTag();
		vh.mTextView1.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_1));
		vh.mTextView2.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_2));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);

		ViewHolder vh = new ViewHolder();
		vh.mTextView1 = Ui.findView(v, android.R.id.text1);
		vh.mTextView2 = Ui.findView(v, android.R.id.text2);
		v.setTag(vh);

		// TODO: Style the rows
		// For now, we have to set this text black (for some reason it's white-on-white in some OSes)
		vh.mTextView1.setTextColor(Color.BLACK);
		vh.mTextView2.setTextColor(Color.BLACK);

		return v;
	}

	private static class ViewHolder {
		private TextView mTextView1;
		private TextView mTextView2;
	}

	public Location getLocation(int position) {
		Cursor c = getCursor();

		if (c == null || c.getCount() <= position) {
			return null;
		}

		c.moveToPosition(position);

		Location loc = new Location();
		loc.setDestinationId(c.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_QUERY));
		loc.setCity(c.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_1));
		loc.setDescription(c.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_2));

		return loc;
	}

	//////////////////////////////////////////////////////////////////////////
	// RecentSearchList interaction

	public void onAirportSelected(Location location) {
		mRecentSearches.addItem(location);

		(new Thread(new Runnable() {
			@Override
			public void run() {
				mRecentSearches.saveList(mContext, RECENT_AIRPORTS_FILE);
			}
		})).start();
	}

}
