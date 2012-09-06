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

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

public class SearchResponseHandler implements ResponseHandler<SearchResponse> {
	private Context mContext;

	private int mNumNights = 1;

	private double mLatitude;
	private double mLongitude;

	private boolean mCachedIsRelease = false;

	public SearchResponseHandler(Context context) {
		mContext = context;
		mCachedIsRelease = AndroidUtils.isRelease(mContext);
	}

	public void setNumNights(int numNights) {
		mNumNights = numNights;
	}

	public void setLatLng(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
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

		String name, mediaName;
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
			else if (name.equals("shortDescription")) {
				property.setDescriptionText(parser.getText());
			}
			else if (name.equals("locationDescription")) {
				location.setDescription(parser.getText());
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
				int totalReviews = parser.getValueAsInt();
				if (totalReviews < 0)
					totalReviews = 0;
				property.setTotalReviews(totalReviews);
			}
			else if (name.equals("hotelGuestRating")) {
				property.setAverageExpediaRating(parser.getValueAsDouble());
			}
			// E3 is calculating miles by converting from km and then rounding; i.e. the km result is more accurate.
			else if (name.equals("proximityDistanceInKiloMeters")) {
				property.setDistanceFromUser(new Distance(parser.getValueAsDouble(), DistanceUnit.KILOMETERS));
			}
			// Use proximityDistanceInMiles as a backup in case proximityDistanceInKiloMeters isn't available.
			else if (name.equals("proximityDistanceInMiles") && property.getDistanceFromUser() == null) {
				property.setDistanceFromUser(new Distance(parser.getValueAsDouble(), DistanceUnit.MILES));
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
			else if (name.equals("lowRateInfo")) {
				Rate lowestRate = readLowRateInfo(parser);
				lowestRate.setNumberOfNights(mNumNights);
				property.setLowestRate(lowestRate);
			}
			else if (name.equals("roomsLeftAtThisRate")) {
				property.setRoomsLeftAtThisRate(parser.getValueAsInt());
			}
			else if (name.equals("isDiscountRestrictedToCurrentSourceType")) {
				property.setIsLowestRateMobileExclusive(parser.getValueAsBoolean());
			}
			else if (name.equals("isSameDayDRR")) {
				property.setIsLowestRateTonightOnly(parser.getValueAsBoolean());
			}
			else {
				parser.skipChildren();
			}
		}

		if (promoDesc != null && property.getLowestRate() != null) {
			property.getLowestRate().setPromoDescription(promoDesc);
		}

		// If we didn't get a distance but we have a search latitude/longitude,
		// calculate the distance based on the params.
		Distance distanceFromUser = property.getDistanceFromUser();
		if ((distanceFromUser == null || distanceFromUser.getDistance() == 0) && mLatitude != 0 && mLongitude != 0
				&& location.getLatitude() != 0 && location.getLongitude() != 0) {
			property.setDistanceFromUser(new Distance(mLatitude, mLongitude, location.getLatitude(), location
					.getLongitude(), DistanceUnit.getDefaultDistanceUnit()));
		}

		if (mCachedIsRelease) {
			searchResponse.addProperty(property);
		}
		else {
			boolean filterMerchants = SettingUtils.get(mContext, mContext.getString(R.string.preference_filter_merchant_properties), false);

			if (filterMerchants) {
				if (!property.isMerchant()) {
					searchResponse.addProperty(property);
				}
			}
			else {
				searchResponse.addProperty(property);
			}
		}
	}

	private Rate readLowRateInfo(JsonParser parser) throws IOException {
		if (parser.getCurrentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected readLowRateInfo() to start with an Object, started with "
					+ parser.getCurrentToken() + " instead.");
		}

		String currencyCode = null;
		double averageRate = 0;
		double averageBaseRate = 0;
		double discountPercent = Rate.UNSET_DISCOUNT_PERCENT;
		double surchargeTotalForEntireStay = 0;
		double totalMandatoryFees = 0;
		double totalPriceWithMandatoryFees = 0;
		double strikethroughPriceToShowUsers = 0.0d;
		double priceToShowUsers = 0.0d;
		String userPriceType = null;

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
			else if (name.equals("discountPercent")) {
				discountPercent = parser.getValueAsDouble();
			}
			else if (name.equals("surchargeTotalForEntireStay")) {
				surchargeTotalForEntireStay = parser.getValueAsDouble();
			}
			else if (name.equals("totalMandatoryFees")) {
				totalMandatoryFees = parser.getValueAsDouble();
			}
			else if (name.equals("totalPriceWithMandatoryFees")) {
				totalPriceWithMandatoryFees = parser.getValueAsDouble();
			}
			else if (name.equals("currencyCode")) {
				currencyCode = parser.getText();
			}
			else if (name.equals("userPriceType")) {
				userPriceType = parser.getText();
			}
			else if (name.equals("strikethroughPriceToShowUsers")) {
				strikethroughPriceToShowUsers = parser.getValueAsDouble();
			}
			else if (name.equals("priceToShowUsers")) {
				priceToShowUsers = parser.getValueAsDouble();
			}
			else {
				parser.skipChildren();
			}
		}

		Rate rate = new Rate();
		rate.setAverageRate(ParserUtils.createMoney(averageRate, currencyCode));
		rate.setAverageBaseRate(ParserUtils.createMoney(averageBaseRate, currencyCode));
		rate.setDiscountPercent(discountPercent);
		rate.setTotalSurcharge(ParserUtils.createMoney(surchargeTotalForEntireStay, currencyCode));
		rate.setTotalMandatoryFees(ParserUtils.createMoney(totalMandatoryFees, currencyCode));
		rate.setTotalPriceWithMandatoryFees(ParserUtils.createMoney(totalPriceWithMandatoryFees, currencyCode));
		rate.setUserPriceType(userPriceType);
		rate.setPriceToShowUsers(ParserUtils.createMoney(priceToShowUsers, currencyCode));
		rate.setStrikethroughPriceToShowUsers(ParserUtils.createMoney(strikethroughPriceToShowUsers, currencyCode));
		return rate;
	}

	private void readServerError(JsonParser parser, Response response) throws IOException {
		ServerError serverError = new ServerError(ApiMethod.SEARCH_RESULTS);

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
						serverError.setMessage(parser.getText());
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
