package com.expedia.bookings.content;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;

public class AutocompleteProvider extends ContentProvider {

	public static final String[] COLUMNS = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
			SearchManager.SUGGEST_COLUMN_ICON_1 };

	public static final int COLUMN_TEXT_INDEX = 1;
	public static final int COLUMN_JSON_INDEX = 3;
	public static final int COLUMN_ICON_INDEX = 4;

	public static Uri generateSearchUri(String query, int limit) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("com.expedia.booking.autocomplete");
		builder.appendPath("search_suggest_query");
		if (query != null) {
			builder.appendPath(query);
		}
		builder.appendQueryParameter("limit", Integer.toString(limit));
		return builder.build();
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String query = "";

		if (uri.getPathSegments().size() > 1) {
			query = URLDecoder.decode(uri.getLastPathSegment());
		}

		return getSuggestions(getContext(), query);
	}

	public static Cursor getSuggestions(Context context, String query) {
		String currentLocation = context.getString(R.string.current_location);

		Set<String> suggestedLocations = new HashSet<String>();

		MatrixCursor cursor = new MatrixCursor(COLUMNS);
		int id = 1;

		// First check if the query exists in recent searches
		List<Search> recentSearches = Search.getRecentSearches(context, 5);
		boolean recentSearchesContainsQuery = false;
		for (Search search : recentSearches) {
			if (search.getQuery().equals(query)) {
				recentSearchesContainsQuery = true;
				break;
			}
		}

		// Don't bother hitting the network in some cases
		if (!recentSearchesContainsQuery && !query.equals(currentLocation) && !TextUtils.isEmpty(query)) {
			ExpediaServices services = new ExpediaServices(context);
			SuggestResponse response = services.suggest(query);

			if (response != null) {
				for (Search search : response.getSuggestions()) {
					String freeformLocation = search.getQuery();
					suggestedLocations.add(freeformLocation);
					JSONObject json = search.toJson();
					Object[] row = { id, freeformLocation, freeformLocation, json, R.drawable.ic_suggestion_place };
					cursor.addRow(row);
					id++;
				}
			}
		}

		// If there were no autosuggestions, then suggest "current location"
		if (id == 1) {
			final Object[] row = { id, currentLocation, currentLocation, null, R.drawable.ic_suggestion_current_location };
			cursor.addRow(row);
			id++;
		}

		// Then suggest history
		if (id <= 15) {
			for (Search search : recentSearches) {
				SearchParams p = new SearchParams();
				p.fillFromSearch(search);

				final String freeformLocation = search.getQuery();
				if (!suggestedLocations.contains(freeformLocation)) {
					suggestedLocations.add(freeformLocation);
					final Object[] historyRow = { id, freeformLocation, freeformLocation, p.toJson(),
							R.drawable.ic_suggestion_place };
					cursor.addRow(historyRow);
					id++;
				}
			}
		}

		// Then suggest from array of random cool cities
		if (id <= 15) {
			for (SearchParams p : getStaticSuggestions(context)) {
				final String freeformLocation = p.getQuery();
				if (!suggestedLocations.contains(freeformLocation)) {
					suggestedLocations.add(freeformLocation);
					final Object[] suggestionRow = { id, freeformLocation, freeformLocation, p.toJson(),
							R.drawable.ic_suggestion_place };
					cursor.addRow(suggestionRow);
					id++;
				}
			}
		}

		return cursor;
	}

	public static Object extractSearchOrString(Cursor cursor) {
		try {
			String searchJson = cursor.getString(COLUMN_JSON_INDEX);
			if (!TextUtils.isEmpty(searchJson)) {
				Search search = new Search();
				search.fromJson(new JSONObject(searchJson));
				return search;
			}
			else {
				String text = cursor.getString(COLUMN_TEXT_INDEX);
				return text;
			}
		}
		catch (JSONException e) {
			Log.e("Unable to parse Search object");
		}
		return null;
	}

	private static List<SearchParams> sStaticSuggestions;

	// 13812: this is "synchronized" so we don't try to create sStaticSuggestions more than once
	private synchronized static List<SearchParams> getStaticSuggestions(Context context) {
		if (sStaticSuggestions == null) {
			Resources resources = context.getResources();
			List<String> suggestions = Arrays.asList(resources.getStringArray(R.array.suggestions));
			List<String> regionIds = Arrays.asList(resources.getStringArray(R.array.suggestion_region_ids));
			List<String> latitudes = Arrays.asList(resources.getStringArray(R.array.suggestion_latitudes));
			List<String> longitudes = Arrays.asList(resources.getStringArray(R.array.suggestion_longitudes));

			sStaticSuggestions = new ArrayList<SearchParams>(suggestions.size());
			for (int i = 0; i < suggestions.size(); i++) {
				SearchParams p = new SearchParams();
				p.setQuery(suggestions.get(i));
				p.setRegionId(regionIds.get(i));
				p.setSearchLatLon(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i)));
				p.setSearchType(SearchType.CITY);
				sStaticSuggestions.add(p);
			}

			Collections.shuffle(sStaticSuggestions); // Randomly shuffle them for each launch
		}
		return sStaticSuggestions;
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
