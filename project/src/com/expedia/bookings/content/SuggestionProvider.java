package com.expedia.bookings.content;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.RegionType;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.data.SuggestionV2.SearchType;
import com.expedia.bookings.server.ExpediaServices;

public class SuggestionProvider extends ContentProvider {

	public static final String SUGGEST_COLUMN_RESULT_TYPE = "suggest_result_type";
	public static final String SUGGEST_COLUMN_SEARCH_TYPE = "suggest_search_type";
	public static final String SUGGEST_COLUMN_REGION_TYPE = "suggest_region_type";
	public static final String SUGGEST_COLUMN_FULL_NAME = "suggest_full_name";
	public static final String SUGGEST_COLUMN_DISPLAY_NAME = "suggest_display_name";
	public static final String SUGGEST_COLUMN_HOTEL_ID = "suggest_hotel_id";
	public static final String SUGGEST_COLUMN_AIRPORT_CODE = "suggest_airport_code";
	public static final String SUGGEST_COLUMN_REGION_ID = "suggest_region_id";
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
		SUGGEST_COLUMN_HOTEL_ID,
		SUGGEST_COLUMN_AIRPORT_CODE,
		SUGGEST_COLUMN_REGION_ID,
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
	public static final int COL_HOTEL_ID = 10;
	public static final int COL_AIRPORT_CODE = 11;
	public static final int COL_REGION_ID = 12;
	public static final int COL_ADDRESS = 13;
	public static final int COL_CITY = 14;
	public static final int COL_STATE_CODE = 15;
	public static final int COL_COUNTRY_CODE = 16;
	public static final int COL_LATITUDE = 17;
	public static final int COL_LONGITUDE = 18;

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String query = "";

		if (uri.getPathSegments().size() > 0) {
			// uri.getLastPathSegment automatically URLDecodes
			query = uri.getLastPathSegment();
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		int id = 0;

		// Try getting suggestions from server
		ExpediaServices services = new ExpediaServices(getContext());
		SuggestionResponse response = services.suggestions(query, 0);
		if (response != null) {
			for (SuggestionV2 suggestion : response.getSuggestions()) {
				Object[] row = suggestionToRow(suggestion);
				if (row != null) {
					row[COL_ID] = id++;
					row[COL_QUERY] = query;
					cursor.addRow(row);
				}
			}
		}

		return cursor;
	}

	public static Uri getContentFilterUri(Context context) {
		return Uri.parse("content://" + context.getString(R.string.authority_autocomplete_suggestions));
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

		switch (searchType) {
		case AIRPORT:
			row[COL_ICON_1] = R.drawable.ic_location_search;
			break;
		case HOTEL:
			row[COL_ICON_1] = R.drawable.ic_suggestion_hotel;
			break;
		default:
			row[COL_ICON_1] = R.drawable.ic_suggestion_place;
			break;
		}

		row[COL_RESULT_TYPE] = resultType.ordinal();
		row[COL_SEARCH_TYPE] = searchType.ordinal();
		row[COL_REGION_TYPE] = regionType != null ? regionType.ordinal() : -1;

		row[COL_FULL_NAME] = suggestion.getFullName();
		row[COL_DISPLAY_NAME] = suggestion.getDisplayName();

		row[COL_HOTEL_ID] = suggestion.getHotelId();
		row[COL_AIRPORT_CODE] = suggestion.getAirportCode();

		Location location = suggestion.getLocation();
		row[COL_REGION_ID] = location.getDestinationId();
		row[COL_ADDRESS] = location.getStreetAddressString();
		row[COL_CITY] = location.getCity();
		row[COL_STATE_CODE] = location.getStateCode();
		row[COL_COUNTRY_CODE] = location.getCountryCode();
		row[COL_LATITUDE] = location.getLatitude();
		row[COL_LONGITUDE] = location.getLongitude();

		return row;
	}

	public static SuggestionV2 rowToSuggestion(Cursor c) {
		SuggestionV2 suggestion = new SuggestionV2();

		suggestion.setResultType(ResultType.values()[c.getInt(COL_RESULT_TYPE)]);
		suggestion.setSearchType(SearchType.values()[c.getInt(COL_SEARCH_TYPE)]);
		int regionTypeOrdinal = c.getInt(COL_REGION_TYPE);
		if (regionTypeOrdinal != -1) {
			suggestion.setRegionType(RegionType.values()[regionTypeOrdinal]);
		}

		suggestion.setFullName(c.getString(COL_FULL_NAME));
		suggestion.setDisplayName(c.getString(COL_DISPLAY_NAME));

		suggestion.setHotelId(c.getInt(COL_HOTEL_ID));
		suggestion.setAirportCode(c.getString(COL_AIRPORT_CODE));

		Location location = new Location();
		location.addStreetAddressLine(c.getString(COL_ADDRESS));
		location.setCity(c.getString(COL_CITY));
		location.setStateCode(c.getString(COL_STATE_CODE));
		location.setCountryCode(c.getString(COL_COUNTRY_CODE));
		location.setLatitude(c.getDouble(COL_LATITUDE));
		location.setLongitude(c.getDouble(COL_LONGITUDE));
		suggestion.setLocation(location);

		return suggestion;
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
