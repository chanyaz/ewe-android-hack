package com.expedia.bookings.data;

import java.io.IOException;

import android.text.TextUtils;

import com.expedia.bookings.data.SuggestionV2.RegionType;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.data.SuggestionV2.SearchType;
import com.mobiata.android.Log;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SuggestionV2TypeAdapter extends TypeAdapter<SuggestionV2> {

	@Override
	public SuggestionV2 read(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		SuggestionV2 suggestion = new SuggestionV2();
		Location location = new Location();

		if (token.equals(JsonToken.BEGIN_OBJECT)) {
			reader.beginObject();
			while (!reader.peek().equals(JsonToken.END_OBJECT)) {
				if (reader.peek().equals(JsonToken.NAME)) {
					switch (reader.nextName()) {
					case "@type": {
						String resultType = reader.nextString();
						if (resultType.equals("regionResult")) {
							suggestion.setResultType(ResultType.REGION);
						}
						else if (resultType.equals("hotelResult")) {
							suggestion.setResultType(ResultType.HOTEL);
						}
						else if (!TextUtils.isEmpty(resultType)) {
							Log.w("Unknown suggest result type: \"" + resultType + "\"");
						}
						break;
					}
					case "t": {
						String searchType = reader.nextString();
						if (searchType.equals("CITY")) {
							suggestion.setSearchType(SearchType.CITY);
						}
						else if (searchType.equals("ATTRACTION")) {
							suggestion.setSearchType(SearchType.ATTRACTION);
						}
						else if (searchType.equals("AIRPORT")) {
							suggestion.setSearchType(SearchType.AIRPORT);
						}
						else if (searchType.equals("HOTEL")) {
							suggestion.setSearchType(SearchType.HOTEL);
						}
						else if (!TextUtils.isEmpty(searchType)) {
							Log.w("Unknown suggest search type: \"" + searchType + "\"");
						}
						break;
					}
					case "rt": {
						String regionType = reader.nextString();
						if (regionType.equals("CITY")) {
							suggestion.setRegionType(RegionType.CITY);
						}
						else if (regionType.equals("MULTICITY")) {
							suggestion.setRegionType(RegionType.MULTICITY);
						}
						else if (regionType.equals("NEIGHBORHOOD")) {
							suggestion.setRegionType(RegionType.NEIGHBORHOOD);
						}
						else if (regionType.equals("POI")) {
							suggestion.setRegionType(RegionType.POI);
						}
						else if (regionType.equals("AIRPORT")) {
							suggestion.setRegionType(RegionType.AIRPORT);
						}
						else if (regionType.equals("METROCODE")) {
							suggestion.setRegionType(RegionType.METROCODE);
						}
						else if (regionType.equals("HOTEL")) {
							suggestion.setRegionType(RegionType.HOTEL);
						}
						else if (!TextUtils.isEmpty(regionType)) {
							Log.w("Unknown suggest region type: \"" + regionType + "\"");
						}
						location.setRegionType(regionType);
						break;
					}
					case "f": {
						suggestion.setFullName(reader.nextString());
						break;
					}
					case "d": {
						suggestion.setDisplayName(reader.nextString());
						break;
					}
					case "i": {
						suggestion.setIndex(reader.nextInt());
						break;
					}
					case "id": {
						suggestion.setRegionId(reader.nextInt());
						break;
					}
					case "a": {
						suggestion.setAirportCode(reader.nextString());
						break;
					}
					case "amc": {
						suggestion.setMultiCityRegionId(reader.nextInt());
						break;
					}
					case "ad": {
						location.addStreetAddressLine(reader.nextString());
						break;
					}
					case "ci": {
						location.setCity(reader.nextString());
						break;
					}
					case "pr": {
						location.setStateCode(reader.nextString());
						break;
					}
					case "ccc": {
						location.setStateCode(reader.nextString());
						break;
					}
					case "ll": {
						reader.beginObject();
						while (!reader.peek().equals(JsonToken.END_OBJECT)) {
							switch (reader.nextName()) {
							case "lat": {
								location.setLatitude(reader.nextDouble());
								break;
							}
							case "lng": {
								location.setLongitude(reader.nextDouble());
								break;
							}
							}
						}
						reader.endObject();
						break;
					}
					}
				}
				else {
					reader.skipValue();
				}
			}
			reader.endObject();
		}
		suggestion.setLocation(location);
		return suggestion;
	}

	@Override
	public void write(JsonWriter out, SuggestionV2 value) throws IOException {
		// ignore
	}
}
