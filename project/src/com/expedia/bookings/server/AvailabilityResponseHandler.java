package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.RateRules;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.FormatUtils.Conjunction;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class AvailabilityResponseHandler extends JsonResponseHandler<AvailabilityResponse> {

	public static final String DOWNLOAD_KEY_PREFIX = "AVAILABILITY_RESPONSE_HANDLER";

	private Context mContext;
	private SearchParams mSearchParams;
	private Property mProperty;

	public AvailabilityResponseHandler(Context context, SearchParams searchParams, Property property) {
		mContext = context;
		mSearchParams = searchParams;
		mProperty = property;
	}

	@Override
	public AvailabilityResponse handleJson(JSONObject response) {
		AvailabilityResponse availResponse = new AvailabilityResponse();
		try {
			// Check for errors, return if found
			availResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.HOTEL_OFFERS, response));
			if (!availResponse.isSuccess()) {
				return availResponse;
			}

			// Parse property info
			Property property = new Property();
			availResponse.setProperty(property);
			property.setDescriptionText(JSONUtils.optNormalizedString(response, "longDescription", null));
			property.setPropertyId(response.optString("hotelId", null));

			int len;
			JSONArray photos = response.optJSONArray("photos");
			if (photos != null) {
				len = photos.length();
				for (int a = 0; a < len; a++) {
					JSONObject photo = photos.optJSONObject(a);
					String url = photo.optString("url");
					if (!url.startsWith("http://")) {
						// No need to worry about POS here.
						url = "http://media.expedia.com" + url;
					}

					Media media = new Media(Media.TYPE_STILL_IMAGE, url);
					property.addMedia(media);
				}
			}

			int numberOfNights = response.optInt("numberOfNights");

			Policy checkInPolicy = null;
			String checkInInstructions = JSONUtils.optNormalizedString(response, "checkInInstructions", null);
			if (!TextUtils.isEmpty(checkInInstructions)) {
				checkInPolicy = new Policy();
				checkInPolicy.setType(Policy.TYPE_CHECK_IN);
				checkInPolicy.setDescription(checkInInstructions);
			}

			JSONArray roomRates = response.optJSONArray("hotelRoomResponse");
			if (roomRates != null) {
				len = roomRates.length();
				for (int a = 0; a < len; a++) {
					JSONObject jsonRate = roomRates.getJSONObject(a);
					Rate rate = parseJsonHotelOffer(jsonRate, numberOfNights, checkInPolicy);
					availResponse.addRate(rate);
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

	public Rate parseJsonHotelOffer(JSONObject jsonRate, int numberOfNights, Policy checkInPolicy)
			throws JSONException {
		Rate rate = new Rate();
		RateRules rateRules = new RateRules();
		rate.setRateRules(rateRules);

		JSONObject rateInfo = jsonRate.getJSONObject("rateInfo");
		JSONObject chargeableRateInfo = rateInfo.optJSONObject("chargeableRateInfo");

		rate.setRateKey(jsonRate.getString("productKey"));
		rate.setRatePlanCode(jsonRate.optString("rateCode", null));
		rate.setRoomTypeCode(jsonRate.optString("roomTypeCode", null));
		rate.setRoomDescription(JSONUtils.getNormalizedString(jsonRate, "roomTypeDescription"));
		rate.setRoomLongDescription(JSONUtils.optNormalizedString(jsonRate, "roomLongDescription", null));

		rate.setRateChange(rateInfo.optBoolean("rateChange", false));
		rate.setNumRoomsLeft(jsonRate.optInt("currentAllotment", 0));
		rate.setNumberOfNights(numberOfNights);

		rate.setNonRefundable(jsonRate.optBoolean("nonRefundable", false));

		String currencyCode = chargeableRateInfo.getString("currencyCode");

		// The rate info passed to merchant vs. agent hotels is very different, so
		// handle the parsing separately here
		Money averageRate = ParserUtils.createMoney(chargeableRateInfo.getString("averageRate"), currencyCode);
		rate.setDailyAmountBeforeTax(averageRate);
		rate.setAverageRate(averageRate);
		rate.setAverageBaseRate(ParserUtils.createMoney(chargeableRateInfo.getString("averageBaseRate"),
				currencyCode));
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

		rate.setTotalAmountBeforeTax(totalBeforeTax);
		rate.setTotalAmountAfterTax(total);
		rate.setTotalSurcharge(surchargeTotalForEntireStay);

		rate.setUserPriceType(chargeableRateInfo.optString("userPriceType"));

		Money priceToShowUsers = ParserUtils.createMoney(chargeableRateInfo.getString("priceToShowUsers"),
				currencyCode);
		Money strikethroughPriceToShowUsers = ParserUtils.createMoney(
				chargeableRateInfo.getString("strikethroughPriceToShowUsers"), currencyCode);

		rate.setPriceToShowUsers(priceToShowUsers);
		rate.setStrikethroughPriceToShowUsers(strikethroughPriceToShowUsers);

		if (jsonRate.has("taxRate")) {
			rate.setTaxesAndFeesPerRoom(ParserUtils.createMoney(jsonRate.optString("taxRate"), currencyCode));
		}

		JSONArray nightlyRates = chargeableRateInfo.optJSONArray("nightlyRatesPerRoom");
		for (int b = 0; b < nightlyRates.length(); b++) {
			Calendar cal = (Calendar) mSearchParams.getCheckInDate().clone();
			cal.add(Calendar.DAY_OF_YEAR, b);

			JSONObject nightlyRate = nightlyRates.getJSONObject(b);
			RateBreakdown rateBreakdown = new RateBreakdown();
			rateBreakdown.setAmount(ParserUtils.createMoney(nightlyRate.getString("rate"), currencyCode));
			rateBreakdown.setDate(new Date(cal));

			rate.addRateBreakdown(rateBreakdown);
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

		if (!mProperty.isMerchant()) {
			// Taxes here is a policy info rather than a money
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_TAX);
			if (jsonRate.has("taxRate")) {
				policy.setDescription(jsonRate.getString("taxRate"));
			}
			rateRules.addPolicy(policy);
		}

		// Look for policy info
		if (checkInPolicy != null) {
			rateRules.addPolicy(checkInPolicy);
		}

		boolean immediateChargeRequired = jsonRate.optBoolean("immediateChargeRequired", false);
		if (immediateChargeRequired) {
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_IMMEDIATE_CHARGE);
			policy.setDescription(mContext.getString(R.string.PolicyImmediateChargeRequired));
			rateRules.addPolicy(policy);
		}
		if (jsonRate.optBoolean("nonRefundable", false)) {
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_NONREFUNDABLE);
			policy.setDescription(mContext.getString(R.string.PolicyNonRefundable));
			rateRules.addPolicy(policy);
		}
		// #5415: It makes no sense to display the "deposit required" warning when we already
		// tell the user that they're going to be charged in full; so only show this if
		// there is no immediate charge required.
		if (!immediateChargeRequired && jsonRate.optBoolean("depositRequired", false)) {
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_DEPOSIT);
			policy.setDescription(mContext.getString(R.string.PolicyDepositRequired));
			rateRules.addPolicy(policy);
		}
		if (jsonRate.optBoolean("guaranteeRequired", false)) {
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_GUARANTEE);
			policy.setDescription(mContext.getString(R.string.PolicyGuaranteeRequired));
			rateRules.addPolicy(policy);
		}

		if (jsonRate.has("cancellationPolicy")) {
			String cancellationPolicy = JSONUtils.getNormalizedString(jsonRate, "cancellationPolicy");
			if (cancellationPolicy.startsWith("<![CDATA[")) {
				cancellationPolicy = cancellationPolicy.substring(9, cancellationPolicy.length() - 3);
			}
			Policy policy = new Policy();
			policy.setType(Policy.TYPE_CANCEL);
			policy.setDescription(cancellationPolicy);
			rateRules.addPolicy(policy);
		}

		// The "general" policy info is composed of policies provided and policies Expedia 
		// wants us to display
		StringBuilder sb = new StringBuilder();
		if (jsonRate.has("policy")) {
			sb.append(JSONUtils.getNormalizedString(jsonRate, "policy"));
			sb.append("\n\n");
		}

		if (mProperty.isMerchant()) {
			sb.append(mContext.getString(R.string.PolicyExpediaMerchant));
		}
		else {
			sb.append(mContext.getString(R.string.PolicyExpediaAgent));
		}

		Policy policy = new Policy();
		policy.setType(Policy.TYPE_OTHER_INFO);
		policy.setDescription(sb.toString());
		rateRules.addPolicy(policy);

		// Value adds
		if (jsonRate.has("valueAdds")) {
			JSONArray valueAdds = jsonRate.getJSONArray("valueAdds");
			for (int b = 0; b < valueAdds.length(); b++) {
				JSONObject valueAdd = valueAdds.getJSONObject(b);
				rate.addValueAdd(JSONUtils.getNormalizedString(valueAdd, "description"));
			}
		}

		if (mProperty.isMerchant()) {
			if (jsonRate.has("bedTypes")) {
				// #6852: If there are multiple bed types, we just "or" them together now
				JSONArray bedTypes = jsonRate.getJSONArray("bedTypes");
				List<String> bedTypeElements = new ArrayList<String>();
				for (int b = 0; b < bedTypes.length(); b++) {
					JSONObject bedType = bedTypes.getJSONObject(b);
					if (!bedType.has("description")) {
						Log.w("No description for bed type. Skipping.");
						continue;
					}

					String bedTypeDescription = JSONUtils.getNormalizedString(bedType, "description");
					bedTypeElements.add(bedTypeDescription);

					if (bedType.has("id") && bedType.getString("id") != null & !"".equals(bedTypeDescription)) {
						rate.addBedType(bedType.getString("id"), bedTypeDescription);
					}
				}
				String ratePlanName = FormatUtils.series(mContext, bedTypeElements, ",", Conjunction.OR);
				rate.setRatePlanName(ratePlanName);
			}
		}
		else {
			String des = rate.getRoomDescription();
			int cut = des.indexOf(" -");
			if (cut == -1) {
				cut = des.indexOf('_');
			}
			String bedType = cut <= 0 ? des : des.substring(0, cut);
			rate.addBedType("UNKNOWN", bedType);
			rate.setRatePlanName(bedType);
		}

		return rate;
	}
}
