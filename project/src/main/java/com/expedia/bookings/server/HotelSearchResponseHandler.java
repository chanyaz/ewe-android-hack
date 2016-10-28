package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.text.HtmlCompat;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public class HotelSearchResponseHandler implements ResponseHandler<HotelSearchResponse> {
	private int mNumNights = 1;

	private double mLatitude;
	private double mLongitude;

	private boolean mIsRelease = false;
	private boolean mFilterMerchants = false;

	public HotelSearchResponseHandler(Context context) {
		mIsRelease = BuildConfig.RELEASE;
		if (!mIsRelease) {
			mFilterMerchants = SettingUtils.get(context, context.getString(R.string.preference_filter_merchant_properties), false);
		}
	}

	public void setNumNights(int numNights) {
		mNumNights = numNights;
	}

	public void setLatLng(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Override
	public HotelSearchResponse handleResponse(okhttp3.Response response) throws IOException {
		if (response == null) {
			return null;
		}

		if (Log.isLoggingEnabled()) {
			StringBuilder httpInfo = new StringBuilder();
			httpInfo.append("HTTP " + response.code());
			httpInfo.append("\n");
			httpInfo.append(response.headers().toString());
			Log.v(httpInfo.toString());
		}

		InputStream in = response.body().byteStream();
		String contentEncoding = response.headers().get("Content-Encoding");
		if (!TextUtils.isEmpty(contentEncoding) && "gzip".equalsIgnoreCase(contentEncoding)) {
			in = new GZIPInputStream(in);
		}

		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		Log.d("Starting to read streaming search response...");

		HotelSearchResponse searchResponse = null;
		try {
			searchResponse = readSearchResponse(reader);
		}
		catch (Exception e) {
			Log.e("HotelSearchResponseHandler exception parsing:", e);
			searchResponse = null;
			// Closes the underlying reader too
			reader.close();
		}

		return searchResponse;
	}

	public HotelSearchResponse readJsonStream(InputStream in) throws IOException {
		if (in == null) {
			return null;
		}

		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		HotelSearchResponse searchResponse = readSearchResponse(reader);

		// Closes the underlying reader too
		reader.close();

		return searchResponse;
	}

	private HotelSearchResponse readSearchResponse(JsonReader reader) throws IOException {
		long start = System.currentTimeMillis();

		HotelSearchResponse searchResponse = new HotelSearchResponse();

		if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
			throw new IOException("Expected readSearchResponse() to start with an Object, started with "
				+ reader.peek() + " instead.");
		}

		String name;
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("errors")) {
				ParserUtils.readServerErrors(reader, searchResponse, ApiMethod.SEARCH_RESULTS);
			}
			else if (name.equals("hotelList")) {
				if (!reader.peek().equals(JsonToken.BEGIN_ARRAY)) {
					throw new IOException("Expected hotelList to start with an Array, started with "
						+ reader.peek() + " instead.");
				}

				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					readHotelSummary(reader, searchResponse);
				}
				reader.endArray();
			}
			else if (name.equals("pageViewBeaconPixelUrl")) {
				searchResponse.setBeaconUrl(reader.nextString());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		Log.d("Hotel Search response parse time: " + (System.currentTimeMillis() - start) + " ms");

		return searchResponse;
	}

	private void readHotelSummary(JsonReader reader, HotelSearchResponse searchResponse) throws IOException {
		Property property = new Property();
		property.setAvailable(true);

		Location location = new Location();
		property.setLocation(location);

		// These are some variables that are stored between fields that are parsed
		String promoDesc = null;

		if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
			throw new IOException("Expected readHotelSummary() to start with an Object, started with "
				+ reader.peek() + " instead.");
		}

		String name, mediaName;
		JsonToken mediaToken;
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("name")) {
				// Property name can sometimes have HTML encoded entities in it (e.g. &amp;)
				property.setName(HtmlCompat.stripHtml(reader.nextString()));
			}
			else if (name.equals("hotelId")) {
				property.setPropertyId(reader.nextString());
			}
			else if (name.equals("locationDescription")) {
				location.setDescription(reader.nextString());
			}
			else if (name.equals("locationId")) {
				location.setLocationId(reader.nextInt());
			}
			else if (name.equals("largeThumbnailUrl")) {
				// The thumbnail url can sometimes assume a prefix
				property.setThumbnail(ParserUtils.parseUrl(reader.nextString()));
			}
			else if (name.equals("supplierType")) {
				property.setSupplierType(reader.nextString());
			}
			else if (name.equals("hotelStarRating")) {
				property.setHotelRating(reader.nextDouble());
			}
			else if (name.equals("totalRecommendations")) {
				property.setTotalRecommendations(reader.nextInt());
			}
			else if (name.equals("totalReviews")) {
				int totalReviews = reader.nextInt();
				if (totalReviews < 0) {
					totalReviews = 0;
				}
				property.setTotalReviews(totalReviews);
			}
			else if (name.equals("hotelGuestRating")) {
				property.setAverageExpediaRating(reader.nextDouble());
			}
			// E3 is calculating miles by converting from km and then rounding; i.e. the km result is more accurate.
			else if (name.equals("proximityDistanceInKiloMeters")) {
				property.setDistanceFromUser(new Distance(reader.nextDouble(), DistanceUnit.KILOMETERS));
			}
			// Use proximityDistanceInMiles as a backup in case proximityDistanceInKiloMeters isn't available.
			else if (name.equals("proximityDistanceInMiles") && property.getDistanceFromUser() == null) {
				property.setDistanceFromUser(new Distance(reader.nextDouble(), DistanceUnit.MILES));
			}
			else if (name.equals("address")) {
				List<String> streetAddress = new ArrayList<>();
				streetAddress.add(reader.nextString());
				location.setStreetAddress(streetAddress);
			}
			else if (name.equals("city")) {
				location.setCity(reader.nextString());
			}
			else if (name.equals("postalCode")) {
				location.setPostalCode(reader.nextString());
			}
			else if (name.equals("countryCode")) {
				location.setCountryCode(reader.nextString());
			}
			else if (name.equals("stateProvinceCode")) {
				location.setStateCode(reader.nextString());
			}
			else if (name.equals("latitude")) {
				location.setLatitude(reader.nextDouble());
			}
			else if (name.equals("longitude")) {
				location.setLongitude(reader.nextDouble());
			}
			else if (name.equals("discountMessage")) {
				promoDesc = reader.nextString();
			}
			else if (name.equals("media")) {
				if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
					throw new IOException("Expected media to start with an Array, started with "
						+ reader.peek() + " instead.");
				}

				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
						throw new IOException("Expected media item to start with an Object, started with "
							+ reader.peek() + " instead.");
					}

					reader.beginObject();
					while (!reader.peek().equals(JsonToken.END_OBJECT)) {
						mediaName = reader.nextName();
						mediaToken = reader.peek();
						if (mediaName.equals("url") && !mediaToken.equals(JsonToken.NULL)) {
							HotelMedia hotelMedia = ParserUtils.parseUrl(reader.nextString());
							if (hotelMedia != null) {
								property.addMedia(hotelMedia);
							}
						}
					}
					reader.endObject();
				}
				reader.endArray();
			}
			else if (name.equals("lowRateInfo")) {
				Rate lowestRate = readLowRateInfo(reader);
				property.setLowestRate(lowestRate);
			}
			else if (name.equals("roomsLeftAtThisRate")) {
				property.setRoomsLeftAtThisRate(reader.nextInt());
			}
			else if (name.equals("isDiscountRestrictedToCurrentSourceType")) {
				property.setIsLowestRateMobileExclusive(reader.nextBoolean());
			}
			else if (name.equals("isSameDayDRR")) {
				property.setIsLowestRateTonightOnly(reader.nextBoolean());
			}
			else if (name.equals("isSponsoredListing")) {
				property.setIsSponsored(reader.nextBoolean());
			}
			else if (name.equals("clickTrackingUrl")) {
				property.setClickTrackingUrl(reader.nextString());
			}
			else if (name.equals("impressionTrackingUrl")) {
				property.setImpressionTrackingUrl(reader.nextString());
			}
			else if (name.equals("omnitureAdDisplayedUrl")) {
				property.setOmnitureAdDisplayedUrl(reader.nextString());
			}
			else if (name.equals("omnitureAdClickedUrl")) {
				property.setOmnitureAdClickedUrl(reader.nextString());
			}
			else if (name.equals("highestSurveyPriceAsPrice")) {
				Money money = readMoney(reader);
				property.setHighestPriceFromSurvey(money);
			}
			else if (name.equals("isVipAccess")) {
				property.setIsVipAccess(reader.nextBoolean());
			}
			else if (name.equals("allowedToDisplayRatingAsStars")) {
				boolean allowedToDisplayRatingAsStars = reader.nextBoolean();
				// ignore for 4.3
				//property.setShowCircles(!allowedToDisplayRatingAsStars);
			}
			else if (name.equals("isShowEtpChoice")) {
				property.setIsETPHotel(reader.nextBoolean());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		// If we didn't get a distance but we have a search latitude/longitude,
		// calculate the distance based on the params.
		Distance distanceFromUser = property.getDistanceFromUser();
		if ((distanceFromUser == null || distanceFromUser.getDistance() == 0) && mLatitude != 0 && mLongitude != 0
			&& location.getLatitude() != 0 && location.getLongitude() != 0) {
			property.setDistanceFromUser(new Distance(mLatitude, mLongitude, location.getLatitude(), location
				.getLongitude(), DistanceUnit.getDefaultDistanceUnit()));
		}

		if (mIsRelease) {
			searchResponse.addProperty(property);
		}
		else {
			if (mFilterMerchants) {
				if (!property.isMerchant()) {
					searchResponse.addProperty(property);
				}
			}
			else {
				searchResponse.addProperty(property);
			}
		}

		if (!searchResponse.hasSponsoredListing() && property.isSponsored()) {
			searchResponse.setHasSponsoredListing(true);
		}
	}

	private Rate readLowRateInfo(JsonReader reader) throws IOException {
		if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
			throw new IOException("Expected readLowRateInfo() to start with an Object, started with "
				+ reader.peek() + " instead.");
		}

		String currencyCode = null;
		String averageRate = null;
		String averageBaseRate = null;
		double discountPercent = Rate.UNSET_DISCOUNT_PERCENT;
		String surchargeTotalForEntireStay = null;
		String totalMandatoryFees = null;
		String totalPriceWithMandatoryFees = null;
		String strikethroughPriceToShowUsers = "0"; // strikethroughPrice optional for Domain V2 Hotel API
		String priceToShowUsers = null;
		String userPriceType = null;
		String checkoutPriceType = null;
		boolean airAttached = false;

		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String name = reader.nextName();

			if (reader.peek().equals(JsonToken.NULL)) {
				// Skip null values
			}
			else if (name.equals("averageRate")) {
				averageRate = reader.nextString();
			}
			else if (name.equals("averageBaseRate")) {
				averageBaseRate = reader.nextString();
			}
			else if (name.equals("discountPercent")) {
				discountPercent = reader.nextDouble();
			}
			else if (name.equals("surchargeTotalForEntireStay")) {
				surchargeTotalForEntireStay = reader.nextString();
			}
			else if (name.equals("totalMandatoryFees")) {
				totalMandatoryFees = reader.nextString();
			}
			else if (name.equals("totalPriceWithMandatoryFees")) {
				totalPriceWithMandatoryFees = reader.nextString();
			}
			else if (name.equals("currencyCode")) {
				currencyCode = reader.nextString();
			}
			else if (name.equals("userPriceType")) {
				userPriceType = reader.nextString();
			}
			else if (name.equals("checkoutPriceType")) {
				checkoutPriceType = reader.nextString();
			}
			else if (name.equals("strikethroughPriceToShowUsers")) {
				strikethroughPriceToShowUsers = reader.nextString();
			}
			else if (name.equals("priceToShowUsers")) {
				priceToShowUsers = reader.nextString();
			}
			else if (name.equals("airAttached")) {
				airAttached = reader.nextBoolean();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		Rate rate = new Rate();
		rate.setDiscountPercent(discountPercent);
		rate.setTotalSurcharge(ParserUtils.createMoney(surchargeTotalForEntireStay, currencyCode));
		rate.setTotalMandatoryFees(ParserUtils.createMoney(totalMandatoryFees, currencyCode));
		rate.setTotalPriceWithMandatoryFees(ParserUtils.createMoney(totalPriceWithMandatoryFees, currencyCode));
		rate.setUserPriceType(userPriceType);
		rate.setCheckoutPriceType(checkoutPriceType);
		rate.setPriceToShowUsers(ParserUtils.createMoney(priceToShowUsers, currencyCode));
		rate.setStrikeThroughPriceToShowUsers(ParserUtils.createMoney(strikethroughPriceToShowUsers, currencyCode));
		rate.setAirAttached(airAttached);
		return rate;
	}

	private void readServerError(JsonReader reader, Response response) throws IOException {
		ServerError serverError = new ServerError(ApiMethod.SEARCH_RESULTS);

		if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
			throw new IOException("Expected readServerError() to start with an Object, started with "
				+ reader.peek() + " instead.");
		}

		// TODO: FIGURE OUT MESSAGE TO DISPLAY TO USER ON ERROR

		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String name = reader.nextName();

			if (name.equals("errorCode")) {
				serverError.setCode(reader.nextString());
			}
			else if (name.equals("errorInfo")) {
				if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
					throw new IOException("Expected errorInfo to start with an Object, started with "
						+ reader.peek() + " instead.");
				}

				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String name2 = reader.nextName();
					if (name2.equals("field")) {
						serverError.addExtra("field", reader.nextString());
					}
					else if (name2.equals("summary")) {
						serverError.setMessage(reader.nextString());
					}
					else {
						reader.skipValue();
					}
				}
				reader.endObject();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		response.addError(serverError);
	}

	private Money readMoney(JsonReader reader) throws IOException {
		Money money = null;
		String currencyCode = null;
		double amount = -1.0d;
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String name = reader.nextName();

			if (name.equals("amount")) {
				amount = reader.nextDouble();
			}
			else if (name.equals("currency")) {
				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String innerName = reader.nextName();
					if (innerName.equals("currencyCode")) {
						currencyCode = reader.nextString();
					}
				}
				reader.endObject();
			}
		}
		reader.endObject();

		if (!TextUtils.isEmpty(currencyCode)) {
			money = new Money();
			money.setAmount(amount);
			money.setCurrency(currencyCode);
		}

		return money;
	}
}
