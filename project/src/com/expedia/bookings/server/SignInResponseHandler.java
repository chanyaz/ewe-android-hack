package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.Traveler.SeatPreference;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.UserPreference;
import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class SignInResponseHandler extends JsonResponseHandler<SignInResponse> {

	private Context mContext;

	public SignInResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public SignInResponse handleJson(JSONObject response) {
		SignInResponse signInResponse = new SignInResponse();

		ParserUtils.logActivityId(response);

		try {
			// Check for errors
			signInResponse.addErrors(ParserUtils.parseErrors(mContext, ServerError.ApiMethod.SIGN_IN, response));
			signInResponse.setSuccess(response.optBoolean("success", true));

			if (signInResponse.isSuccess()) {
				User user = new User();

				// Parse the primary traveler
				Traveler traveler = parseBasicTraveler(response);

				if (response.has("homeAddress")) {
					JSONObject addr = response.optJSONObject("homeAddress");
					Location loc = new Location();
					loc.setCity(addr.optString("city", null));
					loc.setStateCode(addr.optString("province", null));
					loc.setPostalCode(addr.optString("postalCode", null));
					loc.setCountryCode(addr.optString("countryAlpha3Code", null));

					List<String> addrLines = new ArrayList<String>();
					for (String key : new String[] { "firstAddressLine", "secondAddressLine" }) {
						String line = addr.optString(key, null);
						if (line != null) {
							addrLines.add(line);
						}
					}
					loc.setStreetAddress(addrLines);

					traveler.setHomeAddress(loc);
				}

				if (response.has("phoneNumbers")) {
					JSONArray phoneArr = response.optJSONArray("phoneNumbers");
					int size = phoneArr.length();
					for (int a = 0; a < size; a++) {
						JSONObject phoneJson = phoneArr.optJSONObject(a);
						Phone phone = new Phone();
						phone.setNumber(phoneJson.optString("number", null));
						phone.setAreaCode(phoneJson.optString("areaCode", null));
						phone.setCategory(UserPreference.parseCategoryString(phoneJson.optString("category", null)));
						phone.setCountryCode(phoneJson.optString("countryCode", null));
						phone.setExtensionNumber(phoneJson.optString("extensionNumber", null));
						traveler.addPhoneNumber(phone);
					}
				}

				traveler.setEmail(response.optString("email", null));

				traveler.setSmokingPreferred(response.optBoolean("isSmokingPreferred"));

				if (response.has("passports")) {
					JSONArray passports = response.optJSONArray("passports");
					int len = passports.length();
					for (int a = 0; a < len; a++) {
						JSONObject passport = passports.optJSONObject(a);
						traveler.addPassportCountry(passport.optString("countryCode"));
					}
				}

				JSONObject tsaDetails = response.optJSONObject("tsaDetails");
				if (tsaDetails != null) {
					String gender = tsaDetails.optString("gender", null);
					if (gender != null) {
						if (gender.equalsIgnoreCase("male")) {
							traveler.setGender(Gender.MALE);
						}
						else if (gender.equalsIgnoreCase("female")) {
							traveler.setGender(Gender.FEMALE);
						}
						else {
							traveler.setGender(Gender.OTHER);
						}
					}

					String dateOfBirth = tsaDetails.optString("dateOfBirth", null);
					if (dateOfBirth != null) {
						traveler.setBirthDate(LocalDate.parse(dateOfBirth));
					}

					traveler.setRedressNumber(tsaDetails.optString("redressNumber", null));
				}

				traveler.setSeatPreference(JSONUtils.getEnum(response, "seatPreference", SeatPreference.class));
				traveler.setAssistance(JSONUtils.getEnum(response, "specialAssistance", AssistanceType.class));

				user.setPrimaryTraveler(traveler);

				// Parse stored credit cards
				JSONArray ccArr = response.optJSONArray("storedCreditCards");
				if (ccArr != null && ccArr.length() > 0) {
					int size = ccArr.length();
					for (int a = 0; a < size; a++) {
						JSONObject sccJson = ccArr.optJSONObject(a);
						StoredCreditCard scc = new StoredCreditCard();
						String type = sccJson.optString("creditCardType", null);
						if (!TextUtils.isEmpty(type)) {
							scc.setType(CurrencyUtils.parseCardType(type));
						}
						scc.setDescription(sccJson.optString("description", null));
						scc.setId(sccJson.optString("paymentsInstrumentsId", null));
						user.addStoredCreditCard(scc);
					}
				}

				// Parse associated travelers
				if (response.has("associatedTravelers")) {
					JSONArray associatedArr = response.optJSONArray("associatedTravelers");
					int size = associatedArr.length();
					for (int a = 0; a < size; a++) {
						user.addAssociatedTraveler(parseBasicTraveler(associatedArr.optJSONObject(a)));
					}
				}

				signInResponse.setUser(user);
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON SignIn response.", e);
			return null;
		}

		return signInResponse;
	}

	private Traveler parseBasicTraveler(JSONObject obj) {
		Traveler traveler = new Traveler();
		traveler.setTuid(obj.optLong("tuid"));
		traveler.setFirstName(obj.optString("firstName", null));
		traveler.setMiddleName(obj.optString("middleName", null));
		traveler.setLastName(obj.optString("lastName", null));
		traveler.setLoyaltyMembershipNumber(obj.optString("loyaltyMembershipNumber", null));

		// Wow, bad speeling.
		traveler.setLoyaltyMembershipActive(obj.optBoolean("loyaltyMemebershipActive", false));
		traveler.setLoyaltyMembershipName(obj.optString("loyaltyMemebershipName", null));

		traveler.setMembershipTierName(obj.optString("membershipTierName", null));
		return traveler;
	}
}
