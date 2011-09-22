package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.data.Distance;
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

	private int mNumNights = 1;

	public SearchResponseHandler(Context context) {
		// Purposefully leaving this constructor, because I can definitely
		// foresee us wanting the context in this handler someday.
	}

	public void setNumNights(int numNights) {
		mNumNights = numNights;
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

		// TODO: REMOVE THIS ONCE FULLY SWITCHED TO NEW API
		// ALL THIS DOES IS COVER FOR THE APP EXPECTING A SESSION. ~dlew
		searchResponse.setSession(new Session("DUMMY_SESSION"));

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

			if (name.equals("errors")) {
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					readServerError(parser, searchResponse);
				}
			}
			else if (name.equals("hotelList")) {
				if (token != JsonToken.START_ARRAY) {
					throw new IOException("Expected hotelList to start with an Array, started with "
							+ parser.getCurrentToken() + " instead.");
				}

				while (parser.nextToken() != JsonToken.END_ARRAY) {
					readHotelSummary(parser, searchResponse);
				}
			}
			else {
				parser.skipChildren();
			}
		}

		Log.d("Search response parse time: " + (System.currentTimeMillis() - start) + " ms");

		return searchResponse;
	}

	private void readHotelSummary(JsonParser parser, SearchResponse searchResponse) throws IOException {
		Property property = new Property();
		property.setAvailable(true);

		Location location = new Location();
		property.setLocation(location);

		// These are some variables that are stored between fields that are parsed
		String promoDesc = null;

		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readHotelSummary() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String name, amenityName;
		JsonToken token, amenityToken;
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
			else if (name.equals("shortDescription")) {
				property.setDescriptionText(parser.getText());
			}
			else if (name.equals("largeThumbnailUrl")) {
				// The thumbnail url can sometimes assume a prefix
				String url = parser.getText();
				if (!url.startsWith("http://")) {
					url = "http://media.expedia.com" + url;
				}
				property.setThumbnail(new Media(Media.TYPE_STILL_IMAGE, url));
			}
			else if (name.equals("supplierType")) {
				property.setSupplierType(parser.getText());
			}
			else if (name.equals("hotelStarRating")) {
				property.setHotelRating(parser.getValueAsDouble());
			}
			else if (name.equals("totalRecommendations")) {
				property.setTotalRecommendations(parser.getValueAsInt());
			}
			else if (name.equals("totalReviews")) {
				property.setTotalReviews(parser.getValueAsInt());
			}
			else if (name.equals("hotelGuestRating")) {
				property.setAverageExpediaRating(parser.getValueAsDouble());
			}
			else if (name.equals("proximityDistanceInMiles")) {
				property.setDistanceFromUser(new Distance(parser.getValueAsDouble(), Distance.DistanceUnit.MILES));
			}
			else if (name.equals("address")) {
				List<String> streetAddress = new ArrayList<String>();
				streetAddress.add(parser.getText());
				location.setStreetAddress(streetAddress);
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
			else if (name.equals("discountMessage")) {
				promoDesc = parser.getText();
			}
			else if (name.equals("lowRateInfo")) {
				Rate lowestRate = readLowRateInfo(parser);
				lowestRate.setNumberOfNights(mNumNights);
				property.setLowestRate(lowestRate);
			}
			else if (name.equals("amenities")) {
				// TODO: REMOVE THIS IF amenityMask COMES BACK

				// This assumes that the ids between EAN and E3 are the same.  If they are not,
				// then this is very buggy code!
				if (token != JsonToken.START_ARRAY) {
					throw new IOException("Expected amenities to start with an Array, started with " + token
							+ " instead.");
				}

				int mask = 0;
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
						throw new IOException("Expected amenity item to start with an Object, started with "
								+ parser.getCurrentToken() + " instead.");
					}

					while (parser.nextToken() != JsonToken.END_OBJECT) {
						amenityName = parser.getCurrentName();
						amenityToken = parser.nextToken();

						if (amenityName.equals("id") && amenityToken != JsonToken.VALUE_NULL) {
							mask += parser.getValueAsInt();
						}
						else {
							parser.skipChildren();
						}
					}
				}

				property.setAmenityMask(mask);
			}
			else {
				parser.skipChildren();
			}
		}

		if (promoDesc != null && property.getLowestRate() != null) {
			property.getLowestRate().setPromoDescription(promoDesc);
		}

		searchResponse.addProperty(property);
	}

	private Rate readLowRateInfo(JsonParser parser) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readLowRateInfo() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String currencyCode = null;
		double averageRate = 0;
		double averageBaseRate = 0;
		double surcharge = 0;

		String name;
		JsonToken token;
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			token = parser.nextToken();

			if (token == JsonToken.VALUE_NULL) {
				// Skip null values
			}
			else if (name.equals("averageRate")) {
				averageRate = parser.getValueAsDouble();
			}
			else if (name.equals("averageBaseRate")) {
				averageBaseRate = parser.getValueAsDouble();
			}
			else if (name.equals("surchargeTotal")) {
				surcharge = parser.getValueAsDouble();
			}
			else if (name.equals("currencyCode")) {
				currencyCode = parser.getText();
			}
			else {
				parser.skipChildren();
			}
		}

		Rate rate = new Rate();
		rate.setAverageRate(ParserUtils.createMoney(averageRate, currencyCode));
		rate.setAverageBaseRate(ParserUtils.createMoney(averageBaseRate, currencyCode));
		rate.setSurcharge(ParserUtils.createMoney(surcharge, currencyCode));
		return rate;
	}

	private void readServerError(JsonParser parser, Response response) throws IOException {
		ServerError serverError = new ServerError();

		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readServerError() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		// TODO: FIGURE OUT MESSAGE TO DISPLAY TO USER ON ERROR

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			JsonToken token = parser.nextToken();

			if (name.equals("errorCode")) {
				serverError.setCode(parser.getText());
			}
			else if (name.equals("errorInfo")) {
				if (token != JsonToken.START_OBJECT) {
					throw new IOException("Expected errorInfo to start with an Object, started with "
							+ parser.getCurrentToken() + " instead.");
				}
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					String name2 = parser.getCurrentName();
					parser.nextToken();
					if (name2.equals("field")) {
						serverError.addExtra("field", parser.getText());
					}
					else if (name2.equals("summary")) {
						serverError.addExtra("summary", parser.getText());
					}
				}
			}
			else {
				parser.skipChildren();
			}
		}

		response.addError(serverError);
	}
}
