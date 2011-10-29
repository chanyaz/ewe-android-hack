package com.expedia.bookings.content;

import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.Suggestion;

public class AutocompleteProvider extends ContentProvider {

	private static final String[] COLUMNS = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_QUERY,
			SearchManager.SUGGEST_COLUMN_ICON_1
	};

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
			String query = uri.getLastPathSegment();

			// Ignore string if it's just "current location"
			if (query.equals(currentLocation)) {
				return null;
			}

			Log.d("Autocomplete query: " + query);

			GoogleServices services = new GoogleServices(getContext());
			List<Suggestion> suggestions = services.getSuggestions(query);
			if (suggestions != null && suggestions.size() > 0) {
				for (Suggestion suggestion : suggestions) {
					Object[] row = { id, suggestion.mSuggestion, suggestion.mSuggestion,
							R.drawable.autocomplete_pin };
					cursor.addRow(row);
					id++;
				}

				return cursor;
			}
		}
		else {
			// If there is nothing to query, suggest "current location"
			Object[] row = { id, currentLocation, currentLocation, R.drawable.autocomplete_location };
			cursor.addRow(row);
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
