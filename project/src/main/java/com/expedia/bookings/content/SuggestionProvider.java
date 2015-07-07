package com.expedia.bookings.content;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.expedia.bookings.data.RecentList;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.RegionType;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.data.SuggestionV2.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.SuggestionUtils;

public class SuggestionProvider extends ContentProvider {

	public static final String SUGGEST_COLUMN_RESULT_TYPE = "suggest_result_type";
	public static final String SUGGEST_COLUMN_SEARCH_TYPE = "suggest_search_type";
	public static final String SUGGEST_COLUMN_REGION_TYPE = "suggest_region_type";
	public static final String SUGGEST_COLUMN_FULL_NAME = "suggest_full_name";
	public static final String SUGGEST_COLUMN_DISPLAY_NAME = "suggest_display_name";
	public static final String SUGGEST_COLUMN_REGION_ID = "suggest_region_id";
	public static final String SUGGEST_COLUMN_AIRPORT_CODE = "suggest_airport_code";
	public static final String SUGGEST_COLUMN_MULTI_CITY_REGION_ID = "suggest_multi_city_region_id";
	public static final String SUGGEST_COLUMN_ADDRESS = "suggest_address";
	public static final String SUGGEST_COLUMN_CITY = "suggest_city";
	public static final String SUGGEST_COLUMN_STATE_CODE = "suggest_state_code";
	public static final String SUGGEST_COLUMN_COUNTRY_CODE = "suggest_country_code";
	public static final String SUGGEST_COLUMN_LATITUDE = "suggest_latitude";
	public static final String SUGGEST_COLUMN_LONGITUDE = "suggest_longitude";

	public static final String[] COLUMNS = {
		BaseColumns._ID,

		// REQUIRED - We try to suggest a display, but you can shove it and use the full/display names instead
		SearchManager.SUGGEST_COLUMN_TEXT_1,
		SearchManager.SUGGEST_COLUMN_TEXT_2,

		// REQUIRED - Matches "query" from SuggestionResponse
		SearchManager.SUGGEST_COLUMN_QUERY,

		// OPTIONAL - Helps in display though
		SearchManager.SUGGEST_COLUMN_ICON_1,

		// Fields that directly translate from Suggestion
		SUGGEST_COLUMN_RESULT_TYPE,
		SUGGEST_COLUMN_SEARCH_TYPE,
		SUGGEST_COLUMN_REGION_TYPE,
		SUGGEST_COLUMN_FULL_NAME,
		SUGGEST_COLUMN_DISPLAY_NAME,
		SUGGEST_COLUMN_REGION_ID,
		SUGGEST_COLUMN_AIRPORT_CODE,
		SUGGEST_COLUMN_MULTI_CITY_REGION_ID,
		SUGGEST_COLUMN_ADDRESS,
		SUGGEST_COLUMN_CITY,
		SUGGEST_COLUMN_STATE_CODE,
		SUGGEST_COLUMN_COUNTRY_CODE,
		SUGGEST_COLUMN_LATITUDE,
		SUGGEST_COLUMN_LONGITUDE,
	};

	public static final int COL_ID = 0;
	public static final int COL_TEXT_1 = 1;
	public static final int COL_TEXT_2 = 2;
	public static final int COL_QUERY = 3;
	public static final int COL_ICON_1 = 4;
	public static final int COL_RESULT_TYPE = 5;
	public static final int COL_SEARCH_TYPE = 6;
	public static final int COL_REGION_TYPE = 7;
	public static final int COL_FULL_NAME = 8;
	public static final int COL_DISPLAY_NAME = 9;
	public static final int COL_REGION_ID = 10;
	public static final int COL_AIRPORT_CODE = 11;
	public static final int COL_MULTI_CITY_REGION_ID = 12;
	public static final int COL_ADDRESS = 13;
	public static final int COL_CITY = 14;
	public static final int COL_STATE_CODE = 15;
	public static final int COL_COUNTRY_CODE = 16;
	public static final int COL_LATITUDE = 17;
	public static final int COL_LONGITUDE = 18;

	private static final String KEY_SUGGESTION = "KEY_SUGGESTION";

	private static final String RECENTS_FILENAME = "suggestion-recents.dat";

	private static final int MAX_RECENTS = 10;

	private static final int MAX_NUM_NEARBY_AIRPORTS = 5;

	private static boolean sIncludeCurrentLocation = true;

	private static boolean sShowNearbyAirports = false;

	private RecentList<SuggestionV2> mRecents;

	@Override
	public boolean onCreate() {
		mRecents = new RecentList<SuggestionV2>(SuggestionV2.class, getContext(), RECENTS_FILENAME, MAX_RECENTS);

		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Static methods

	public static Uri getContentFilterUri(Context context) {
		return Uri.parse("content://" + context.getString(R.string.authority_autocomplete_suggestions));
	}

	public static void enableCurrentLocation(boolean isEnabled) {
		if (isEnabled != sIncludeCurrentLocation) {
			sIncludeCurrentLocation = isEnabled;
		}
	}

	public static void setShowNearbyAiports(boolean showNearbyAirports) {
		sShowNearbyAirports = showNearbyAirports;
	}

	//////////////////////////////////////////////////////////////////////////
	// Queries

	private String mQuery;
	private int mId;
	private MatrixCursor mQueryCursor;

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String query = "";
		Events.post(new Events.SuggestionQueryStarted());

		if (uri.getPathSegments().size() > 0) {
			// uri.getLastPathSegment automatically URLDecodes
			query = uri.getLastPathSegment();
		}

		// Startup some vars
		mId = 0;
		mQuery = query;
		mQueryCursor = new MatrixCursor(COLUMNS);

		// If the user has no query parameter, then add some special rows; otherwise
		// depend entirely on the suggestions service.
		if (TextUtils.isEmpty(query)) {
			if (sIncludeCurrentLocation) {
				// Add current location
				SuggestionV2 currentLocationSuggestion = new SuggestionV2();
				currentLocationSuggestion.setResultType(ResultType.CURRENT_LOCATION);
				currentLocationSuggestion.setFullName(getContext().getString(R.string.current_location));
				currentLocationSuggestion.setDisplayName(currentLocationSuggestion.getFullName());
				addSuggestion(currentLocationSuggestion);
			}

			if (sShowNearbyAirports) {
				List<SuggestionV2> airportSuggestions = SuggestionUtils
					.getNearbyAirportSuggestions(getContext(), MAX_NUM_NEARBY_AIRPORTS);
				for (SuggestionV2 suggestion : airportSuggestions) {
					addSuggestion(suggestion);
				}
			}

			// Add recent suggestions
			for (SuggestionV2 recentSuggestion : mRecents.getList()) {
				addSuggestion(recentSuggestion);
			}
		}
		else {
			// Try getting suggestions from server
			ExpediaServices services = new ExpediaServices(getContext());
			SuggestionResponse response = services.suggestions(query);
			if (response != null) {

				// #3200 special-case sorting for 3 char queries. We suspect the user may be entering
				// an airport code explicitly and want to bubble AIRPORT Suggestions to the top.
				if (mQuery.length() == 3) {
					Collections.sort(response.getSuggestions(), new Comparator<SuggestionV2>() {
						@Override
						public int compare(SuggestionV2 suggestionV2, SuggestionV2 suggestionV22) {
							boolean oneIsAirport = mQuery.equalsIgnoreCase(suggestionV2.getAirportCode())
								&& suggestionV2.getRegionType() == RegionType.AIRPORT;
							boolean twoIsAirport = mQuery.equalsIgnoreCase(suggestionV22.getAirportCode())
								&& suggestionV22.getRegionType() == RegionType.AIRPORT;

							// TODO is this stupid? should I shove this logic into SuggestionV2 compareTo?
							// TODO can this be more elegant?
							if (oneIsAirport && !twoIsAirport) {
								return -1;
							}
							else if (!oneIsAirport && twoIsAirport) {
								return 1;
							}
							else {
								return suggestionV2.compareTo(suggestionV22);
							}
						}
					});
				}

				for (SuggestionV2 suggestion : response.getSuggestions()) {
					addSuggestion(suggestion);
				}
			}
		}

		return mQueryCursor;
	}

	private void addSuggestion(SuggestionV2 suggestion) {
		Object[] row = suggestionToRow(suggestion);
		row[COL_ID] = mId++;
		row[COL_QUERY] = mQuery;
		mQueryCursor.addRow(row);
	}

	//////////////////////////////////////////////////////////////////////////
	// Recently used suggestions

	public static void addSuggestionToRecents(Context context, SuggestionV2 suggestion) {
		if (suggestion.getResultType() == ResultType.CURRENT_LOCATION) {
			// Don't try to save meta-suggestions like CURRENT_LOCATION
			return;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_SUGGESTION, suggestion.toJson().toString());
		context.getContentResolver().insert(getContentFilterUri(context), values);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		try {
			SuggestionV2 suggestion = new SuggestionV2();
			String jsonStr = values.getAsString(KEY_SUGGESTION);
			JSONObject obj = new JSONObject(jsonStr);
			suggestion.fromJson(obj);
			mRecents.addItem(suggestion);
			mRecents.saveList(getContext(), RECENTS_FILENAME);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete will always delete all of them. It'll ignore the selection and selectionArgs.
	 *
	 * @param uri
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = mRecents.getList().size();
		mRecents.clear();
		mRecents.saveList(getContext(), RECENTS_FILENAME);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	//////////////////////////////////////////////////////////////////////////
	// Conversions

	private static Object[] suggestionToRow(SuggestionV2 suggestion) {
		Object[] row = new Object[COLUMNS.length];

		ResultType resultType = suggestion.getResultType();
		SearchType searchType = suggestion.getSearchType();
		RegionType regionType = suggestion.getRegionType();

		Pair<String, String> splitName = suggestion.splitFullName();
		if (splitName != null) {
			row[COL_TEXT_1] = splitName.first;
			row[COL_TEXT_2] = splitName.second;
		}
		else {
			row[COL_TEXT_1] = suggestion.getFullName();
		}

		if (resultType == ResultType.CURRENT_LOCATION) {
			row[COL_ICON_1] = R.drawable.ic_suggest_current_location;
		}
		else if (searchType == null) {
			// For unknown types
			row[COL_ICON_1] = R.drawable.ic_suggest_place;
		}
		else {
			switch (searchType) {
			case AIRPORT:
				row[COL_ICON_1] = R.drawable.ic_suggest_airport;
				break;
			case HOTEL:
				row[COL_ICON_1] = R.drawable.ic_suggest_hotel;
				break;
			default:
				row[COL_ICON_1] = R.drawable.ic_suggest_place;
				break;
			}
		}

		row[COL_RESULT_TYPE] = resultType != null ? resultType.ordinal() : -1;
		row[COL_SEARCH_TYPE] = searchType != null ? searchType.ordinal() : -1;
		row[COL_REGION_TYPE] = regionType != null ? regionType.ordinal() : -1;

		row[COL_FULL_NAME] = suggestion.getFullName();
		row[COL_DISPLAY_NAME] = suggestion.getDisplayName();

		row[COL_MULTI_CITY_REGION_ID] = suggestion.getMultiCityRegionId();
		row[COL_REGION_ID] = suggestion.getRegionId();
		row[COL_AIRPORT_CODE] = suggestion.getAirportCode();

		Location location = suggestion.getLocation();
		if (location != null) {
			row[COL_ADDRESS] = location.getStreetAddressString();
			row[COL_CITY] = location.getCity();
			row[COL_STATE_CODE] = location.getStateCode();
			row[COL_COUNTRY_CODE] = location.getCountryCode();
			row[COL_LATITUDE] = location.getLatitude();
			row[COL_LONGITUDE] = location.getLongitude();
		}

		return row;
	}

	public static SuggestionV2 rowToSuggestion(Cursor c) {
		SuggestionV2 suggestion = new SuggestionV2();

		Location location = new Location();
		location.addStreetAddressLine(c.getString(COL_ADDRESS));
		location.setCity(c.getString(COL_CITY));
		location.setStateCode(c.getString(COL_STATE_CODE));
		location.setCountryCode(c.getString(COL_COUNTRY_CODE));
		location.setLatitude(c.getDouble(COL_LATITUDE));
		location.setLongitude(c.getDouble(COL_LONGITUDE));
		suggestion.setLocation(location);

		int resultTypeOrdinal = c.getInt(COL_RESULT_TYPE);
		if (resultTypeOrdinal != -1) {
			suggestion.setResultType(ResultType.values()[resultTypeOrdinal]);
		}

		int searchTypeOrdinal = c.getInt(COL_SEARCH_TYPE);
		if (searchTypeOrdinal != -1) {
			suggestion.setSearchType(SearchType.values()[searchTypeOrdinal]);
		}

		int regionTypeOrdinal = c.getInt(COL_REGION_TYPE);
		if (regionTypeOrdinal != -1) {
			suggestion.setRegionType(RegionType.values()[regionTypeOrdinal]);
			location.setRegionType(suggestion.getRegionType().name());
		}

		suggestion.setFullName(c.getString(COL_FULL_NAME));
		suggestion.setDisplayName(c.getString(COL_DISPLAY_NAME));

		suggestion.setMultiCityRegionId(c.getInt(COL_MULTI_CITY_REGION_ID));
		suggestion.setRegionId(c.getInt(COL_REGION_ID));
		suggestion.setAirportCode(c.getString(COL_AIRPORT_CODE));
		suggestion.setIcon(c.getInt(COL_ICON_1));

		return suggestion;
	}

	//////////////////////////////////////////////////////////////////////////
	// Unsupported Operations

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException("Not expecting people to call getType().");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("You cannot update suggestions.");
	}

	//////////////////////////////////////////////////////////////////////////
	// Clear

	public static void clearRecents(Context context) {
		Uri uri = getContentFilterUri(context);
		context.getContentResolver().delete(uri, null, null);
	}
}
