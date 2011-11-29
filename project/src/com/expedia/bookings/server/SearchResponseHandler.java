package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;

public class SearchResponseHandler implements ResponseHandler<SearchResponse> {

	private Context mContext;

	// Variables used for parsing
	private int mNumNights = 1;

	public SearchResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public SearchResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		if (response == null) {
			return null;
		}

		InputStream in = AndroidHttpClient.getUngzippedContent(response.getEntity());

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(in);

		Log.d("Starting to read streaming search response...");

		SearchResponse searchResponse = readSearchResponse(parser);

		parser.close();

		return searchResponse;
	}

	public SearchResponse readJsonStream(InputStream in) throws IOException {
		if (in == null) {
			return null;
		}

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(in);

		SearchResponse searchResponse = readSearchResponse(parser);

		parser.close();

		return searchResponse;
	}

	private SearchResponse readSearchResponse(JsonParser parser) throws IOException {
		long start = System.currentTimeMillis();

		SearchResponse searchResponse = new SearchResponse();

		if (parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readSearchResponse() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String name;
		JsonToken token;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			token = parser.nextToken();

			if (name.equals("error") || name.equals("errors")) {
				readServerErrors(parser, searchResponse);
			}
			else if (name.equals("body")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected body to start with an Object, started with "
							+ parser.getCurrentToken() + " instead.");
				}
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					if (parser.getCurrentName().equals("HotelListResponse")) {
						readHotelListResponse(parser, searchResponse);
					}
					else {
						parser.skipChildren();
					}
				}
			}
			else {
				parser.skipChildren();
			}
		}

		Log.d("Search response parse time: " + (System.currentTimeMillis() - start) + " ms");

		return searchResponse;
	}

	private void readHotelListResponse(JsonParser parser, SearchResponse searchResponse) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readHotelListResponse() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String name;
		JsonToken token;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			token = parser.nextToken();

			if (name.equals("EanWsError")) {
				readEanError(parser, searchResponse);
			}
			else if (name.equals("LocationInfos")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected LocationInfos to start with an Object, started with "
							+ parser.getCurrentToken() + " instead.");
				}
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					if (parser.getCurrentName().equals("LocationInfo")) {
						readLocations(parser, searchResponse);
					}
					else {
						parser.skipChildren();
					}
				}
			}
			else if (name.equals("cacheKey")) {
				searchResponse.setCacheKey(parser.getText());
			}
			else if (name.equals("cacheLocation")) {
				searchResponse.setCacheLocation(parser.getText());
			}
			else if (name.equals("numberOfNights")) {
				mNumNights = parser.getValueAsInt();

				// In case we parse "numberOfNights" after "HotelList", we need to go back
				// and adjust things if the value has changed from 1 (the default).
				if (searchResponse.getPropertiesCount() > 0 && mNumNights > 1) {
					for (Property property : searchResponse.getProperties()) {
						Rate lowestRate = property.getLowestRate();
						if (lowestRate != null) {
							lowestRate.setNumberOfNights(mNumNights);
						}
					}
				}
			}
			else if (name.equals("HotelList")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected HotelList to start with an Object, started with "
							+ parser.getCurrentToken() + " instead.");
				}

				while (parser.nextToken() != JsonToken.END_OBJECT) {
					if (parser.getCurrentName().equals("HotelSummary")) {
						if (parser.nextToken() == JsonToken.START_OBJECT) {
							readHotelSummary(parser, searchResponse);
						}
						else {
							while (parser.nextToken() != JsonToken.END_ARRAY) {
								readHotelSummary(parser, searchResponse);
							}
						}
					}
				}
			}
			else if (name.equals("customerSessionId")) {
				searchResponse.setSession(new Session(parser.getText()));
			}
			else {
				parser.skipChildren();
			}
		}
	}

	private void readHotelSummary(JsonParser parser, SearchResponse searchResponse) throws IOException {
		Property property = new Property();
		property.setAvailable(true);

		Location location = new Location();
		property.setLocation(location);

		// These are some variables that are stored between fields that are parsed
		String rateCurrencyCode = null;
		Double proximityDistance = null;
		String proximityUnit = null;
		String address1 = null;
		String address2 = null;
		String address3 = null;
		Double averageRate = null;
		Double averageBaseRate = null;
		Rate lowestRate = null;
		Double surcharges = null;

		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readHotelSummary() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String name, reviewName, mediaName;
		JsonToken token, mediaToken;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			token = parser.nextToken();

			if (token == JsonToken.VALUE_NULL) {
				// Skip null values
			}
			else if (name.equals("name")) {
				// Property name can sometimes have HTML encoded entities in it (e.g. &amp;)
				property.setName(Html.fromHtml(parser.getText()).toString());
			}
			else if (name.equals("hotelId")) {
				property.setPropertyId(parser.getText());
			}
			else if (name.equals("expediaPropertyId")) {
				property.setExpediaPropertyId(parser.getValueAsInt());
			}
			else if (name.equals("description")) {
				String description = parser.getText();
				if (description.length() > 0) {
					property.setDescriptionText(fixDescription(description));
				}
			}
			else if (name.equals("thumbNailUrl")) {
				// The thumbnail url can sometimes assume a prefix
				String url = parser.getText();
				if (!url.startsWith("http://")) {
					url = "http://images.travelnow.com" + url;
				}
				property.setThumbnail(new Media(Media.TYPE_STILL_IMAGE, url));
			}
			else if (name.equals("amenityMask")) {
				// Convert the amenity mask to a string
				int amenityMask = parser.getValueAsInt();
				property.setAmenityMask(amenityMask);
			}
			else if (name.equals("supplierType")) {
				property.setSupplierType(parser.getText());
			}
			else if (name.equals("rateCurrencyCode")) {
				rateCurrencyCode = parser.getText();
			}
			else if (name.equals("hotelRating")) {
				property.setHotelRating(parser.getValueAsDouble());
			}
			else if (name.equals("reviewScoreSummary")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected reviewScoreSummary to start with an Object, started with " + token
							+ " instead.");
				}

				while (parser.nextToken() != JsonToken.END_OBJECT) {
					reviewName = parser.getCurrentName();
					parser.nextToken();

					if (reviewName.equals("TotalRecommendations")) {
						property.setTotalRecommendations(parser.getValueAsInt());
					}
					else if (reviewName.equals("TotalReviews")) {
						property.setTotalReviews(parser.getValueAsInt());
					}
					else if (reviewName.equals("AverageOverallSatisfaction")) {
						property.setAverageExpediaRating(parser.getValueAsDouble());
					}
					else {
						parser.skipChildren();
					}
				}
			}
			else if (name.equals("proximityDistance")) {
				proximityDistance = parser.getValueAsDouble();
			}
			else if (name.equals("proximityUnit")) {
				proximityUnit = parser.getText();
			}
			else if (name.equals("address1")) {
				address1 = parser.getText();
			}
			else if (name.equals("address2")) {
				address2 = parser.getText();
			}
			else if (name.equals("address3")) {
				address3 = parser.getText();
			}
			else if (name.equals("city")) {
				location.setCity(parser.getText());
			}
			else if (name.equals("postalCode")) {
				location.setPostalCode(parser.getText());
			}
			else if (name.equals("countryCode")) {
				location.setCountryCode(parser.getText());
			}
			else if (name.equals("stateProvinceCode")) {
				location.setStateCode(parser.getText());
			}
			else if (name.equals("latitude")) {
				location.setLatitude(parser.getValueAsDouble());
			}
			else if (name.equals("longitude")) {
				location.setLongitude(parser.getValueAsDouble());
			}
			else if (name.equals("media")) {
				if (token != JsonToken.START_ARRAY) {
					throw new IOException("Expected media to start with an Array, started with "
							+ parser.getCurrentToken() + " instead.");
				}

				while (parser.nextToken() != JsonToken.END_ARRAY) {
					if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
						throw new IOException("Expected media item to start with an Object, started with "
								+ parser.getCurrentToken() + " instead.");
					}

					while (parser.nextToken() != JsonToken.END_OBJECT) {
						mediaName = parser.getCurrentName();
						mediaToken = parser.nextToken();
						if (mediaName.equals("url") && mediaToken != JsonToken.VALUE_NULL) {
							property.addMedia(new Media(Media.TYPE_STILL_IMAGE, parser.getText()));
						}
					}
				}

			}
			else if (name.equals("promoDescription")) {
				if (lowestRate == null) {
					lowestRate = new Rate();
				}
				lowestRate.setPromoDescription(parser.getText());
			}
			else if (name.equals("averageRate")) {
				averageRate = parser.getValueAsDouble();
				if (lowestRate == null) {
					lowestRate = new Rate();
				}
			}
			else if (name.equals("averageBaseRate")) {
				averageBaseRate = parser.getValueAsDouble();
				if (lowestRate == null) {
					lowestRate = new Rate();
				}
			}
			else if (name.equals("Surcharges")) {
				surcharges = readSurcharges(parser);
			}
			else {
				parser.skipChildren();
			}
		}

		// Handle any fields which needed stored data
		if (proximityDistance != null && proximityUnit != null) {
			DistanceUnit unit = (proximityUnit.equals("MI")) ? DistanceUnit.MILES : DistanceUnit.KILOMETERS;
			property.setDistanceFromUser(new Distance(proximityDistance, unit));
		}

		List<String> streetAddress = new ArrayList<String>();
		if (address1 != null) {
			streetAddress.add(address1);
		}
		if (address2 != null) {
			streetAddress.add(address2);
		}
		if (address3 != null) {
			streetAddress.add(address3);
		}
		location.setStreetAddress(streetAddress);

		if (lowestRate != null) {
			if (averageBaseRate != null) {
				lowestRate.setAverageBaseRate(ParserUtils.createMoney(averageBaseRate, rateCurrencyCode));
			}
			if (averageRate != null) {
				lowestRate.setAverageRate(ParserUtils.createMoney(averageRate, rateCurrencyCode));
			}

			if (surcharges != null) {
				lowestRate.setSurcharge(ParserUtils.createMoney(surcharges, rateCurrencyCode));
			}

			lowestRate.setNumberOfNights(mNumNights);

			property.setLowestRate(lowestRate);
		}

		searchResponse.addProperty(property);
	}

	private double readSurcharges(JsonParser parser) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected Surcharges to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		double total = 0;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			JsonToken token = parser.nextToken();
			if (name.equals("Surcharge")) {
				if (token == JsonToken.START_OBJECT) {
					total += readSurcharge(parser);
				}
				else {
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						total += readSurcharge(parser);
					}
				}
			}
			else {
				parser.skipChildren();
			}
		}
		return total;
	}

	private double readSurcharge(JsonParser parser) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readSurcharge() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		double amount = 0;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("@amount")) {
				amount += parser.getValueAsDouble();
			}
			else {
				parser.skipChildren();
			}
		}
		return amount;
	}

	private void readLocations(JsonParser parser, SearchResponse searchResponse) throws IOException {
		List<Location> locations = new ArrayList<Location>();

		if (parser.getCurrentToken() != JsonToken.START_ARRAY && parser.nextToken() != JsonToken.START_ARRAY) {
			throw new IOException("Expected readLocations() to start with an Array, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		while (parser.nextToken() != JsonToken.END_ARRAY) {
			Location location = new Location();

			if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected location to start with an Object, started with "
						+ parser.getCurrentToken() + " instead.");
			}

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String name = parser.getCurrentName();
				parser.nextToken();

				if (name.equals("city")) {
					location.setCity(parser.getText());
				}
				else if (name.equals("countryCode")) {
					location.setCountryCode(parser.getText());
				}
				else if (name.equals("stateProvinceCode")) {
					location.setStateCode(parser.getText());
				}
				else if (name.equals("destinationId")) {
					location.setDestinationId(parser.getText());
				}
				else {
					parser.skipChildren();
				}
			}

			locations.add(location);
		}

		if (locations.size() > 0) {
			searchResponse.setLocations(locations);
		}
	}

	private void readServerErrors(JsonParser parser, Response response) throws IOException {
		if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
			readServerError(parser, response);
		}
		else {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				readServerError(parser, response);
			}
		}
	}

	private void readServerError(JsonParser parser, Response response) throws IOException {
		ServerError serverError = new ServerError();

		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readServerError() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			JsonToken token = parser.nextToken();

			if (name.equals("msg")) {
				serverError.setMessage(parser.getText());
			}
			else if (name.equals("code")) {
				serverError.setCode(parser.getText());
			}
			else if (name.equals("extras")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected data to start with an Object, started with "
							+ parser.getCurrentToken() + " instead.");
				}
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					String name2 = parser.getCurrentName();
					parser.nextToken();
					if (name2.equals("url")) {
						serverError.addExtra("url", parser.getText());
					}
					else if (name2.equals("message")) {
						serverError.addExtra("message", parser.getText());
					}
				}
			}
			else {
				parser.skipChildren();
			}
		}

		response.addError(serverError);
	}

	// Expects parser to have read the START_OBJECT token before passing in
	private void readEanError(JsonParser parser, Response response) throws IOException {
		ServerError serverError = new ServerError();
		serverError.setCode("-1");

		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readEanError() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("verboseMessage")) {
				String errMsg = parser.getText();
				serverError.setVerboseMessage(errMsg);

				// For backwards compatibility with old versions of HP
				if (ServerError.ERRORS.containsKey(errMsg)) {
					errMsg = mContext.getString(ServerError.ERRORS.get(errMsg));
				}
				serverError.setMessage(errMsg);
			}
			else if (name.equals("presentationMessage")) {
				String errMsg = parser.getText();
				serverError.setPresentationMessage(errMsg);
			}
			else if (name.equals("category")) {
				serverError.setCategory(parser.getText());
			}
			else if (name.equals("handling")) {
				serverError.setHandling(parser.getText());
			}
			else {
				parser.skipChildren();
			}
		}

		response.addError(serverError);
	}

	private final static Pattern PATTERN_HEADER = Pattern.compile("<strong>(.+?)[\\.:]</strong>");

	/**
	 * Fixes the description field, which is often a little wonky.
	 * 
	 * A lot of the HTML we get has repeated, pointless things.
	 * e.g. <p></p>, or <br /><br />.  This deduplicates breaks and
	 * gets rid of empty paragraphs.
	 */
	private String fixDescription(String source) {
		if (source == null) {
			return null;
		}

		// Remove pointless markup
		source = source.replaceAll("<p></p>", "").replaceAll("<br /><br />", "<br />").replaceAll("<ul> </ul>", "");

		// Replace <strong> with <b>, remove pointless punctuation
		Matcher m = PATTERN_HEADER.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "<b>" + m.group(1) + "</b>");
		}
		m.appendTail(sb);

		return sb.toString();
	}
}
