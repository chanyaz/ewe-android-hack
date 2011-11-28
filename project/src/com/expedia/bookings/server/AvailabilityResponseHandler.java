package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

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
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.FormatUtils.Conjunction;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class AvailabilityResponseHandler extends JsonResponseHandler<AvailabilityResponse> {

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
			List<ServerError> errors = ParserUtils.parseErrors(mContext, response);
			if (errors != null) {
				for (ServerError error : errors) {
					availResponse.addError(error);
				}
				return availResponse;
			}

			// Parse property info
			Property property = new Property();
			availResponse.setProperty(property);
			property.setDescriptionText(response.optString("longDescription", null));
			JSONArray photos = response.optJSONArray("photos");
			if (photos != null) {
				int len = photos.length();
				for (int a = 0; a < len; a++) {
					JSONObject photo = photos.optJSONObject(a);
					String url = photo.optString("url");
					if (!url.startsWith("http://")) {
						url = "http://media.expedia.com" + url;
					}
					Media media = new Media(Media.TYPE_STILL_IMAGE, url);
					property.addMedia(media);
				}
			}

			int numberOfNights = response.optInt("numberOfNights");

			// TODO: REMOVE THIS ONCE FULLY SWITCHED TO NEW API
			// ALL THIS DOES IS COVER FOR THE APP EXPECTING A SESSION. ~dlew
			availResponse.setSession(new Session("DUMMY_SESSION"));

			Policy checkInPolicy = null;
			String checkInInstructions = response.optString("checkInInstructions", null);
			if (checkInInstructions != null && checkInInstructions.length() > 0) {
				checkInPolicy = new Policy();
				checkInPolicy.setType(Policy.TYPE_CHECK_IN);
				checkInPolicy.setDescription(checkInInstructions);
			}

			JSONArray roomRates = response.getJSONArray("hotelRoomResponse");
			int len = roomRates.length();
			for (int a = 0; a < len; a++) {
				Rate rate = new Rate();
				RateRules rateRules = new RateRules();
				rate.setRateRules(rateRules);

				JSONObject jsonRate = roomRates.getJSONObject(a);
				JSONObject rateInfo = jsonRate.getJSONObject("rateInfo");
				JSONObject chargeableRateInfo = rateInfo.optJSONObject("chargeableRateInfo");

				rate.setRateKey(jsonRate.getString("productKey"));
				rate.setRatePlanCode(jsonRate.optString("rateCode", null));
				rate.setRoomTypeCode(jsonRate.optString("roomTypeCode", null));
				rate.setRoomDescription(jsonRate.getString("roomTypeDescription"));
				rate.setRateChange(rateInfo.optBoolean("rateChange", false));
				rate.setNumRoomsLeft(jsonRate.optInt("currentAllotment", 0));
				rate.setNumberOfNights(numberOfNights);

				if (!mProperty.isMerchant()) {
					rate.setRatePlanName(jsonRate.getString("rateDescription"));
				}

				String currencyCode = chargeableRateInfo.getString("currencyCode");

				// The rate info passed to merchant vs. agent hotels is very different, so
				// handle the parsing separately here
				if (mProperty.isMerchant()) {
					Money averageRate = ParserUtils.createMoney(chargeableRateInfo.getString("averageRate"),
							currencyCode);
					rate.setDailyAmountBeforeTax(averageRate);
					rate.setAverageRate(averageRate);
					rate.setAverageBaseRate(ParserUtils.createMoney(chargeableRateInfo.getString("averageBaseRate"),
							currencyCode));

					Money surchargeTotalForEntireStay = ParserUtils.createMoney(
							chargeableRateInfo.optString("surchargeTotalForEntireStay", "0.0"),
							currencyCode);
					Money total = ParserUtils.createMoney(chargeableRateInfo.getString("total"), currencyCode);
					Money totalBeforeTax = total.copy();
					totalBeforeTax.subtract(surchargeTotalForEntireStay);

					rate.setTotalAmountBeforeTax(totalBeforeTax);
					rate.setTotalAmountAfterTax(total);
					rate.setTotalSurcharge(surchargeTotalForEntireStay);

					if (jsonRate.has("taxRate")) {
						rate.setTaxesAndFeesPerRoom(ParserUtils.createMoney(jsonRate.getDouble("taxRate"), currencyCode));
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
								rate.setExtraGuestFee(ParserUtils.createMoney(surcharge.getDouble("amount"),
										currencyCode));
							}
						}
					}
				}
				else {
					rate.setDailyAmountBeforeTax(ParserUtils.createMoney(
							chargeableRateInfo.getString("maxNightlyRate"), currencyCode));

					// Taxes here is a policy info rather than a money
					Policy policy = new Policy();
					policy.setType(Policy.TYPE_TAX);
					policy.setDescription(jsonRate.getString("taxRate"));
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
					String cancellationPolicy = jsonRate.getString("cancellationPolicy");
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
					sb.append(jsonRate.getString("policy"));
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
						rate.addValueAdd(valueAdd.getString("description"));
					}
				}

				if (jsonRate.has("bedTypes")) {
					// #6852: If there are multiple bed types, we just "or" them together now
					JSONArray bedTypes = jsonRate.getJSONArray("bedTypes");
					List<String> bedTypeElements = new ArrayList<String>();
					for (int b = 0; b < bedTypes.length(); b++) {
						JSONObject bedType = bedTypes.getJSONObject(b);
						String bedTypeDescription = bedType.getString("description");
						bedTypeElements.add(bedTypeDescription);
						rate.addBedType(bedType.getString("id"), bedTypeDescription);
					}
					String ratePlanName = FormatUtils.series(mContext, bedTypeElements, ",", Conjunction.OR);
					rate.setRatePlanName(ratePlanName);
				}

				availResponse.addRate(rate);
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON availability response.", e);
			return null;
		}

		return availResponse;
	}
}
