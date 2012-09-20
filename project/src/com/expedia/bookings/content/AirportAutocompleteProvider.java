package com.expedia.bookings.content;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.server.ExpediaServices;

public class AirportAutocompleteProvider extends ContentProvider {

	public static final String[] COLUMNS = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_QUERY
	};

	public static final int COL_ID = 0;
	public static final int COL_SUGGEST_COLUMN_TEXT_1 = 1;
	public static final int COL_SUGGEST_COLUMN_TEXT_2 = 2;
	public static final int COL_SUGGEST_COLUMN_QUERY = 3;

	public static final Uri CONTENT_FILTER_URI = Uri.parse("content://com.expedia.booking.autocomplete.air");

	private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^(.+)\\((.+)\\)$");

	private ExpediaServices mServices;

	@Override
	public boolean onCreate() {
		mServices = new ExpediaServices(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
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
					Matcher m = DISPLAY_NAME_PATTERN.matcher(suggestion.getDisplayName());
					if (m.matches()) {
						Object[] row = new Object[COLUMNS.length];
						row[0] = suggestion.getId();
						row[1] = m.group(1);
						row[2] = m.group(2);
						row[3] = suggestion.getAirportLocationCode();
						cursor.addRow(row);
					}
				}
			}
		}

		return cursor;
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
