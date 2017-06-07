package com.expedia.bookings.content;

import java.net.URLDecoder;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.data.AirportSuggestion;
import com.expedia.bookings.server.ExpediaServices;

public class AirportAutocompleteProvider extends ContentProvider {

	public static final String SUGGEST_COLUMN_COUNTRY_CODE = "suggest_column_country_code";

	public static final String[] COLUMNS = {
		BaseColumns._ID,
		SearchManager.SUGGEST_COLUMN_TEXT_1,
		SearchManager.SUGGEST_COLUMN_TEXT_2,
		SearchManager.SUGGEST_COLUMN_QUERY,
		SearchManager.SUGGEST_COLUMN_ICON_1,
		SUGGEST_COLUMN_COUNTRY_CODE,
	};

	public static final int COL_ID = 0;
	public static final int COL_SUGGEST_COLUMN_TEXT_1 = 1;
	public static final int COL_SUGGEST_COLUMN_TEXT_2 = 2;
	public static final int COL_SUGGEST_COLUMN_QUERY = 3;
	public static final int COL_SUGGEST_COLUMN_ICON_1 = 4;
	public static final int COL_SUGGEST_COLUMN_COUNTRY_CODE = 5;

	private ExpediaServices mServices;

	@Override
	public boolean onCreate() {
		return true;
	}


	public static AirportSuggestion rowToSuggestion(Cursor c) {
		AirportSuggestion suggestion = new AirportSuggestion();
		suggestion.setIcon(c.getInt(COL_SUGGEST_COLUMN_ICON_1));
		suggestion.setText1(c.getString(COL_SUGGEST_COLUMN_TEXT_1));
		suggestion.setText2(c.getString(COL_SUGGEST_COLUMN_TEXT_2));
		suggestion.setLocation(createLocationFromRow(c));

		return suggestion;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (mServices == null) {
			mServices = new ExpediaServices(getContext());
		}

		String query = "";
		if (uri.getPathSegments().size() > 0) {
			query = URLDecoder.decode(uri.getLastPathSegment());
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		if (!TextUtils.isEmpty(query)) {
			// Start a new request
			SuggestResponse response = mServices.suggest(query, ExpediaServices.F_FLIGHTS);
			if (response != null) {
				for (Suggestion suggestion : response.getSuggestions()) {
					Object[] row = createRowFromSuggestion(suggestion);
					if (row != null) {
						cursor.addRow(row);
					}
				}
			}
		}

		return cursor;
	}

	public static Uri getContentFilterUri(Context context) {
		String url = "content://" + context.getString(R.string.authority_autocomplete_airport);
		return Uri.parse(url);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility
	//
	// This just makes it unnecessary for classes outside of here to
	// understand the exact workings of the provider.

	public static Object[] createRowFromSuggestion(Suggestion suggestion) {
		Pair<String, String> displayName = suggestion.splitDisplayNameForFlights();
		if (displayName != null) {
			Object[] row = new Object[COLUMNS.length];
			row[0] = suggestion.getId();
			row[1] = displayName.first;
			row[2] = displayName.second;
			row[3] = suggestion.getAirportLocationCode();
			row[4] = R.drawable.ic_suggestion_place_pin;
			row[5] = suggestion.getCountryCode();
			return row;
		}

		return null;
	}

	public static Location createLocationFromRow(Cursor c) {
		Location loc = new Location();
		loc.setDestinationId(c.getString(COL_SUGGEST_COLUMN_QUERY));
		loc.setCity(c.getString(COL_SUGGEST_COLUMN_TEXT_1));
		loc.setDescription(c.getString(COL_SUGGEST_COLUMN_TEXT_2));
		loc.setCountryCode(c.getString(COL_SUGGEST_COLUMN_COUNTRY_CODE));
		return loc;
	}

	//////////////////////////////////////////////////////////////////////////
	// Unsupported Operations

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException("You cannot call getType() on the AirportAutocompleteProvider.");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("You cannot insert suggestions into the AirportAutocompleteProvider.");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("You cannot update suggestions in the AirportAutocompleteProvider.");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("You cannot delete suggestions from the AirportAutocompleteProvider.");
	}

}
