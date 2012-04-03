package com.expedia.bookings.content;

import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;

public class AutocompleteProvider extends ContentProvider {

	public static final String[] COLUMNS = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
			SearchManager.SUGGEST_COLUMN_ICON_1 };

	public static Uri generateSearchUri(String query, int limit) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("com.expedia.booking.autocomplete");
		builder.appendPath("search_suggest_query");
		builder.appendPath(query);
		builder.appendQueryParameter("limit", Integer.toString(limit));
		return builder.build();
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String currentLocation = getContext().getString(R.string.current_location);

		MatrixCursor cursor = new MatrixCursor(COLUMNS);
		int id = 1;
		if (uri.getPathSegments().size() > 1) {
			String query = URLDecoder.decode(uri.getLastPathSegment());

			// Ignore string if it's just "current location"
			if (query.equals(currentLocation)) {
				return null;
			}

			Log.d("Autocomplete query: " + query);

			ExpediaServices services = new ExpediaServices(getContext());
			SuggestResponse response = services.suggest(query);

			if (response != null) {
				for (Search search : response.getSuggestions()) {
					String freeformLocation = search.getFreeformLocation();
					JSONObject json = search.toJson();
					Object[] row = { id, freeformLocation, freeformLocation, json, R.drawable.autocomplete_pin };
					cursor.addRow(row);
					id++;
				}

				return cursor;
			}
		}
		else {
			// If there is nothing to query, suggest "current location"
			final Object[] row = { id, currentLocation, currentLocation, null, R.drawable.autocomplete_location };
			cursor.addRow(row);

			// Then suggest history
			for (Search search : Search.getRecentSearches(getContext(), 5)) {

				SearchParams p = new SearchParams();
				p.fillFromSearch(search);

				final String freeformLocation = search.getFreeformLocation();
				final Object[] historyRow = { id, freeformLocation, freeformLocation, p.toJson(),
						R.drawable.autocomplete_pin };
				cursor.addRow(historyRow);
			}

			// Then suggest from array
			for (String suggestion : getContext().getResources().getStringArray(R.array.suggestions)) {
				final Object[] suggestionRow = { id, suggestion, suggestion, null, R.drawable.autocomplete_pin };
				cursor.addRow(suggestionRow);
			}

			id++;
		}

		return cursor;
	}

	//////////////////////////////////////////////////////////////////////////
	// Unsupported Operations

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException("Not expecting people to call getType() on the AutocompleteProvider.");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("You cannot insert suggestions into the AutocompleteProvider.");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("You cannot update suggestions in the AutocompleteProvider.");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("You cannot delete suggestions from the AutocompleteProvider.");
	}
}
