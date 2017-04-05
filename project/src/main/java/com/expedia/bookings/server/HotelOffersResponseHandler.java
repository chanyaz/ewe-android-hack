package com.expedia.bookings.server;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class HotelOffersResponseHandler extends JsonResponseHandler<HotelOffersResponse> {


	private Context mContext;
	private HotelSearchParams mSearchParams;

	public HotelOffersResponseHandler(Context context, HotelSearchParams searchParams) {
		mContext = context;
		mSearchParams = searchParams;
	}

	@Override
	public HotelOffersResponse handleJson(JSONObject response) {
		HotelOffersResponse availResponse = new HotelOffersResponse();
		try {
			// Check for errors, return if found
			availResponse.addErrors(ParserUtils.parseErrors(ApiMethod.HOTEL_OFFERS, response));
			if (!availResponse.isSuccess()) {
				return availResponse;
			}

			// Parse property info
			Property property = new Property();
			availResponse.setProperty(property);
			property.setName(response.optString("hotelName", null));
			property.setHotelRating(response.optDouble("hotelStarRating", 0));
			property.setTotalReviews(response.optInt("totalReviews", 0));
			property.setTotalRecommendations(response.optInt("totalRecommendations", 0));
			property.setAverageExpediaRating(response.optDouble("hotelGuestRating", 0));
			property.setPropertyId(response.optString("hotelId", null));
			property.setTelephoneSalesNumber(response.optString("telesalesNumber", null));
			property.setIsDesktopOverrideNumber(response.optBoolean("deskTopOverrideNumber", true));
			property.setIsVipAccess(response.optBoolean("isVipAccess", false));
			//property.setShowCircles(!response.optBoolean("allowedToDisplayRatingAsStars", true));

			// Parse text sections
			JSONArray overviewTextArr = response.optJSONArray("hotelOverviewText");
			if (overviewTextArr != null) {
				for (int a = 0; a < overviewTextArr.length(); a++) {
					property.addOverviewText(parseHotelTextSection(overviewTextArr.optJSONObject(a)));
				}
			}
			property.setAmenitiesText(parseHotelTextSection(response.optJSONObject("hotelAmenitiesText")));
			property.setPoliciesText(parseHotelTextSection(response.optJSONObject("hotelPoliciesText")));
			property.setFeesText(parseHotelTextSection(response.optJSONObject("hotelFeesText")));
			property.setMandatoryFeesText(parseHotelTextSection(response.optJSONObject("hotelMandatoryFeesText")));
			property.setRenovationText(parseHotelTextSection(response.optJSONObject("hotelRenovationText")));

			Location location = new Location();
			location.setLatitude(response.optDouble("latitude", 0));
			location.setLongitude(response.optDouble("longitude", 0));
			location.setCity(response.optString("hotelCity", null));
			location.setStateCode(response.optString("hotelStateProvince", null));
			location.setCountryCode(response.optString("hotelCountry", null));
			List<String> streetAddress = new ArrayList<String>();
			streetAddress.add(response.optString("hotelAddress", null));
			location.setStreetAddress(streetAddress);
			location.setDescription(response.optString("locationDescription", null));
			property.setLocation(location);

			int len;
			JSONArray photos = response.optJSONArray("photos");
			if (photos != null) {
				len = photos.length();
				for (int a = 0; a < len; a++) {
					JSONObject photo = photos.optJSONObject(a);
					HotelMedia hotelMedia = ParserUtils.parseUrl(photo.optString("url"));
					if (hotelMedia != null) {
						property.addMedia(hotelMedia);
					}
				}

				// Adding the first media as the thumbnail media, if it exists
				if (property.getMediaCount() > 0) {
					HotelMedia hotelMedia = property.getMedia(0);
					HotelMedia thumbnailHotelMedia = new HotelMedia(hotelMedia.getUrl(HotelMedia.Size.BIG));
					property.setThumbnail(thumbnailHotelMedia);
				}
			}

			int numberOfNights = response.optInt("numberOfNights");

			JSONArray roomRates = response.optJSONArray("hotelRoomResponse");
			if (roomRates != null) {
				len = roomRates.length();
				for (int a = 0; a < len; a++) {
					JSONObject jsonRate = roomRates.getJSONObject(a);
					property.setIsLowestRateMobileExclusive(jsonRate
						.optBoolean("isDiscountRestrictedToCurrentSourceType"));
					property.setIsLowestRateTonightOnly(jsonRate.optBoolean("isSameDayDRR"));
					Rate rate = parseJsonHotelOffer(jsonRate, numberOfNights);
					availResponse.addRate(rate);

					property.setAvailable(true); // Once we have a rate, we're available!

					// #1686: The supplier type is only set in these rates; parse here if we don't have one set already
					if (TextUtils.isEmpty(property.getSupplierType())) {
						property.setSupplierType(jsonRate.optString("supplierType", null));
					}

					// If this rate has an associated pay later offer, reflect that in hotel's info
					if (!property.hasEtpOffer() && (rate.getEtpRate() != null || rate.isPayLater())) {
						property.setHasEtpOffer(true);
					}
				}
			}

			// Amenities
			// see EHiOS implementation: https://team.mobiata.com/redmine/issues/8992
			JSONArray amenityArray = response.optJSONArray("hotelAmenities");
			if (amenityArray != null) {
				int amenityMask = 0;
				len = amenityArray.length();
				for (int a = 0; a < len; a++) {
					JSONObject jsonAmenity = amenityArray.getJSONObject(a);
					switch (jsonAmenity.optInt("id")) {
					case 2065: // Business Center
					case 2213: // Free Business Center Access
					case 2538: // 24-hour business center
						amenityMask |= Property.Amenity.BUSINESS_CENTER.getFlag();
						break;

					case 9: // Fitness equipment
						amenityMask |= Property.Amenity.FITNESS_CENTER.getFlag();
						break;

					case 371: // Spa tub
						amenityMask |= Property.Amenity.HOT_TUB.getFlag();
						break;

					case 2046: // Internet access - high speed
					case 2097: // In-Room High Speed Internet Acce
					case 2100: // Internet access - wireless
					case 2101: // Internet access - wireless
					case 2125: // Internet access - high speed
					case 2126: // Internet access - dial-up
					case 2127: // Internet access - complimentary
					case 2156: // Internet access - complimentary
					case 2191: // Free High-Speed Internet
					case 2192: // Free Wireless Internet
					case 2220: // In-Room Wireless Internet Access
					case 2390: // Wireless Internet access - compl
					case 2392: // High-speed (w) Internet -comp
					case 2394: // Dial-up Internet access - compli
					case 2403: // Wireless Internet access - compl
					case 2405: // High-speed (w) Internet acc-comp
					case 2407: // Dial-up Internet access - compli
						amenityMask |= Property.Amenity.INTERNET.getFlag();
						break;

					case 2186: // Children's club
						amenityMask |= Property.Amenity.KIDS_ACTIVITIES.getFlag();
						break;

					case 312: // Kitchenette
					case 2158: // Kitchen
					case 2208: // Full Kitchen
						amenityMask |= Property.Amenity.KITCHEN.getFlag();
						break;

					case 51: // Pets accepted
					case 2338: // Pet-friendly Hotel
						amenityMask |= Property.Amenity.PETS_ALLOWED.getFlag();
						break;

					case 14: // Swimming pool - indoor
					case 24: // Swimming pool - outdoor
					case 2074: // Swimming pool
					case 2138: // Swimming pool - outdoor seasonal
					case 2859: // Private pool
						amenityMask |= Property.Amenity.POOL.getFlag();
						break;

					case 19: // Restaurant
						amenityMask |= Property.Amenity.RESTAURANT.getFlag();
						break;

					case 2017: // Spa services on site
					case 2123: // Full-service health spa
					case 2341: // Spa Hotel
						amenityMask |= Property.Amenity.SPA.getFlag();
						break;

					case 361: // Breakfast available (surcharge)
					case 2001: // Complimentary breakfast
					case 2077: // Free Breakfast
					case 2098: // Free Breakfast
					case 2102: // All Meals
					case 2103: // Continental Breakfast
					case 2104: // Full Breakfast
					case 2105: // English Breakfast
					case 2193: // Continental Breakfast for 2
					case 2194: // Breakfast for 2
					case 2205: // Breakfast Buffet
					case 2209: // Continental Breakfast
					case 2210: // Full Breakfast
					case 2211: // Buffet Breakfast
						amenityMask |= Property.Amenity.BREAKFAST.getFlag();
						break;

					case 6: // Babysitting or child care
						amenityMask |= Property.Amenity.BABYSITTING.getFlag();
						break;

					case 28: // Parking (valet)
					case 2011: // Parking (free)
					case 2013: // Self parking
					case 2109: // Free Parking
					case 2110: // Free Airport Parking
					case 2132: // Parking garage
					case 2133: // Parking (secure)
					case 2195: // Free Valet Parking
					case 2215: // Free Hotel Parking
					case 2216: // Free Valet Parking
					case 2553: // Free Parking During Stay
					case 2798: // Extended parking
						amenityMask |= Property.Amenity.PARKING.getFlag();
						break;

					case 20: // Room service - (limited hours)
					case 2015: // Room service (24 hours)
					case 2053: // Room service
						amenityMask |= Property.Amenity.ROOM_SERVICE.getFlag();
						break;

					case 2419: // Accessible path of travel
						amenityMask |= Property.Amenity.ACCESSIBLE_PATHS.getFlag();
						break;

					case 2420: // Accessible bathroom
						amenityMask |= Property.Amenity.ACCESSIBLE_BATHROOM.getFlag();
						break;

					case 2421: // Roll-in shower
						amenityMask |= Property.Amenity.ROLL_IN_SHOWER.getFlag();
						break;

					case 2422: // Handicapped parking
						amenityMask |= Property.Amenity.HANDICAPPED_PARKING.getFlag();
						break;

					case 2423: // In-room accessibility
						amenityMask |= Property.Amenity.IN_ROOM_ACCESSIBILITY.getFlag();
						break;

					case 2424: // Accessibility equipment for deaf
						amenityMask |= Property.Amenity.DEAF_ACCESSIBILITY_EQUIPMENT.getFlag();
						break;

					case 2425: // Braille or raised signage
						amenityMask |= Property.Amenity.BRAILLE_SIGNAGE.getFlag();
						break;

					case 10: // Airport transportation (comp)
					case 2196: // Free Airport Shuttle
					case 2214: // Free Airport Shuttle
					case 2353: // Airport transportation
						amenityMask |= Property.Amenity.FREE_AIRPORT_SHUTTLE.getFlag();
						break;
					}
				}
				property.setAmenityMask(amenityMask);
			}
			else {
				property.setAmenityMask(0);
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON availability response.", e);
			return null;
		}
		return availResponse;
	}

	private HotelTextSection parseHotelTextSection(JSONObject obj) {
		if (obj == null) {
			return null;
		}

		String name = obj.optString("name");
		String content = obj.optString("content");
		return new HotelTextSection(name, content);
	}

	public Rate parseJsonHotelOffer(JSONObject jsonRate, int numberOfNights)
		throws JSONException {
		Rate rate = new Rate();

		JSONObject rateInfo = jsonRate.getJSONObject("rateInfo");
		JSONObject chargeableRateInfo = rateInfo.optJSONObject("chargeableRateInfo");

		rate.setRateKey(jsonRate.getString("productKey"));
		rate.setAirAttached(chargeableRateInfo.optBoolean("airAttached", false));
		rate.setRoomDescription(JSONUtils.getNormalizedString(jsonRate, "roomTypeDescription"));
		rate.setRoomLongDescription(JSONUtils.optNormalizedString(jsonRate, "roomLongDescription", null));
		rate.setThumbnail(ParserUtils.parseUrl(jsonRate.optString("roomThumbnailUrl", null)));
		rate.setIsPayLater(jsonRate.optBoolean("isPayLater", false));
		rate.setRateChange(rateInfo.optBoolean("rateChange", false));
		rate.setNumRoomsLeft(jsonRate.optInt("currentAllotment", 0));

		rate.setHasFreeCancellation(jsonRate.optBoolean("hasFreeCancellation", false));
		if (jsonRate.has("freeCancellationWindowDate")) {
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
			try {
				DateTime dateTime = dtf.parseDateTime(jsonRate.getString("freeCancellationWindowDate"));
				rate.setFreeCancellationWindowDate(dateTime.withZoneRetainFields(DateTimeZone.UTC));
			}
			catch (IllegalFieldValueException e) {
				Log.w("Could not parse free cancellation window date", e);
			}
		}
		rate.setNonRefundable(jsonRate.optBoolean("nonRefundable", false));

		String currencyCode = chargeableRateInfo.getString("currencyCode");

		// The rate info passed to merchant vs. agent hotels is very different, so
		// handle the parsing separately here
		Money averageRate = ParserUtils.createMoney(chargeableRateInfo.getString("averageRate"), currencyCode);
		rate.setDiscountPercent(chargeableRateInfo.getDouble("discountPercent"));

		Money totalMandatoryFees = ParserUtils.createMoney(
			chargeableRateInfo.optString("totalMandatoryFees", "0.0"), currencyCode);
		rate.setTotalMandatoryFees(totalMandatoryFees);

		Money totalPriceWithMandatoryFees = ParserUtils.createMoney(
			chargeableRateInfo.optString("totalPriceWithMandatoryFees", "0.0"), currencyCode);
		rate.setTotalPriceWithMandatoryFees(totalPriceWithMandatoryFees);

		Money surchargeTotalForEntireStay = ParserUtils.createMoney(
			chargeableRateInfo.optString("surchargeTotalForEntireStay", "0.0"), currencyCode);
		Money total = ParserUtils.createMoney(chargeableRateInfo.getString("total"), currencyCode);
		Money totalBeforeTax = total.copy();
		totalBeforeTax.subtract(surchargeTotalForEntireStay);

		rate.setTotalAmountAfterTax(total);
		rate.setTotalSurcharge(surchargeTotalForEntireStay);

		rate.setUserPriceType(chargeableRateInfo.optString("userPriceType"));
		rate.setCheckoutPriceType(chargeableRateInfo.optString("checkoutPriceType"));
		rate.setShowResortFeesMessaging(chargeableRateInfo.optBoolean("showResortFeeMessage", false));
		rate.setResortFeesInclusion(chargeableRateInfo.optBoolean("resortFeeInclusion", false));
		rate.setIsDepositRequired(jsonRate.optBoolean("depositRequired", false));

		rate.setTaxStatusType(chargeableRateInfo.optString("taxStatusType"));

		Money depositAmount = ParserUtils.createMoney(chargeableRateInfo.optString("depositAmount", "0.0"), currencyCode);
		Money depositToShowUsers = ParserUtils.createMoney(chargeableRateInfo.optString("depositAmountToShowUsers",
				"0.0"),
			currencyCode);

		Money priceToShowUsers = ParserUtils.createMoney(chargeableRateInfo.getString("priceToShowUsers"),
			currencyCode);
		// strikethroughPrice optional for Domain V2 Hotel API
		String strikethroughPrice =
			(chargeableRateInfo.getString("strikethroughPriceToShowUsers") != null) ? chargeableRateInfo.getString("strikethroughPriceToShowUsers") : "0";
		Money strikethroughPriceToShowUsers = ParserUtils.createMoney(
			strikethroughPrice, currencyCode);
		Money nightlyRateTotal = ParserUtils.createMoney(chargeableRateInfo.getString("nightlyRateTotal"),
			currencyCode);
		rate.setNightlyRateTotal(nightlyRateTotal);
		rate.setDepositAmount(depositAmount);
		rate.setDepositToShowUsers(depositToShowUsers);
		rate.setPriceToShowUsers(priceToShowUsers);
		rate.setStrikeThroughPriceToShowUsers(strikethroughPriceToShowUsers);

		JSONArray nightlyRates = chargeableRateInfo.optJSONArray("nightlyRatesPerRoom");
		if (mSearchParams != null && nightlyRates != null) {
			for (int b = 0; b < nightlyRates.length(); b++) {
				LocalDate checkInDate = mSearchParams.getCheckInDate();
				LocalDate night = checkInDate.plusDays(b);

				JSONObject nightlyRate = nightlyRates.getJSONObject(b);
				RateBreakdown rateBreakdown = new RateBreakdown();
				rateBreakdown.setAmount(ParserUtils.createMoney(nightlyRate.getString("rate"), currencyCode));
				rateBreakdown.setDate(night);

				rate.addRateBreakdown(rateBreakdown);
			}
		}

		// Surcharges
		JSONArray surchargesForEntireStay = chargeableRateInfo.optJSONArray("surchargesForEntireStay");
		if (surchargesForEntireStay != null) {
			for (int b = 0; b < surchargesForEntireStay.length(); b++) {
				JSONObject surcharge = surchargesForEntireStay.getJSONObject(b);
				if (surcharge.optString("type").equals("EXTRA")) {
					rate.setExtraGuestFee(ParserUtils.createMoney(surcharge.optString("amount"), currencyCode));
				}
			}
		}

		JSONArray priceAdjustments = chargeableRateInfo.optJSONArray("priceAdjustments");
		if (priceAdjustments != null) {
			Money totalAdjustments = new Money();
			totalAdjustments.setAmount("0");
			for (int b = 0; b < priceAdjustments.length(); b++) {
				JSONObject adjustment = priceAdjustments.getJSONObject(b);
				totalAdjustments.add(ParserUtils.createMoney(adjustment.optString("amount"), currencyCode));
			}
			rate.setTotalPriceAdjustments(totalAdjustments);
		}

		if (jsonRate.has("cancellationPolicy")) {
			String cancellationPolicy = JSONUtils.getNormalizedString(jsonRate, "cancellationPolicy");
			if (cancellationPolicy.startsWith("<![CDATA[")) {
				cancellationPolicy = cancellationPolicy.substring(9, cancellationPolicy.length() - 3);
			}
			rate.setCancellationPolicy(cancellationPolicy);
		}

		// Value adds
		if (jsonRate.has("valueAdds")) {
			JSONArray valueAdds = jsonRate.getJSONArray("valueAdds");
			for (int b = 0; b < valueAdds.length(); b++) {
				JSONObject valueAdd = valueAdds.getJSONObject(b);
				rate.addValueAdd(JSONUtils.getNormalizedString(valueAdd, "description"));
			}
		}

		if (jsonRate.has("bedTypes")) {
			// #6852: If there are multiple bed types, we just "or" them together now
			JSONArray bedTypes = jsonRate.getJSONArray("bedTypes");
			for (int b = 0; b < bedTypes.length(); b++) {
				JSONObject bedType = bedTypes.getJSONObject(b);

				String id = bedType.optString("id", null);
				String description = JSONUtils.optNormalizedString(bedType, "description", null);

				if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(description)) {
					rate.addBedType(id, description);
				}
			}

			String formattedBedTypes = rate.getFormattedBedNames();
			if (!TextUtils.isEmpty(formattedBedTypes)) {
				rate.setRatePlanName(formattedBedTypes);
			}
		}

		// For some non-merchant hotels
		if (TextUtils.isEmpty(rate.getRatePlanName())) {
			String des = rate.getRoomDescription();
			int cut = des.indexOf(" -");
			if (cut == -1) {
				cut = des.indexOf('_');
			}
			String bedType = cut <= 0 ? des : des.substring(0, cut);
			if (rate.getBedTypes() == null || rate.getBedTypes().isEmpty()) {
				rate.addBedType("UNKNOWN", bedType);
			}
			rate.setRatePlanName(bedType);
		}

		if (jsonRate.has("payLaterOffer")) {
			JSONObject etpOffer = jsonRate.getJSONObject("payLaterOffer");
			Rate etpOfferRate = parseJsonHotelOffer(etpOffer, numberOfNights);
			rate.addEtpOffer(etpOfferRate);
		}

		if (jsonRate.has("depositPolicy") && jsonRate.getJSONArray("depositPolicy").length() > 1) {
			JSONArray deposityPolicyJSON = jsonRate.getJSONArray("depositPolicy");
			String[] depositPolicy = new String[2];
			depositPolicy[0] = deposityPolicyJSON.getString(0);
			depositPolicy[1] = deposityPolicyJSON.getString(1);
			rate.setDepositPolicy(depositPolicy);
		}

		return rate;
	}

	public String parseRewardPoints(JSONObject obj) {
		String points = "";
		try {
			JSONObject rewards = obj.getJSONObject("rewards");
			if (rewards != null) {
				if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
					points = NumberFormat.getInstance().format(rewards.optInt("totalPointsToEarn"));
				}
				else {
					JSONObject totalAmountToEarn = rewards.optJSONObject("totalAmountToEarn");
					if (totalAmountToEarn != null) {
						String amount = totalAmountToEarn.optString("amount");
						String currencyCode = totalAmountToEarn.optString("currencyCode");
						Money totalAmountToEarnMoney = ParserUtils.createMoney(amount, currencyCode);
						if (totalAmountToEarnMoney.isZero()) {

							points = "0";
						}
						else {
							points = totalAmountToEarnMoney
								.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL);
						}
					}
				}
			}
		}
		catch (JSONException e) {
			Log.e("Error parsing Expedia rewards", e);
		}

		return points;
	}
}
