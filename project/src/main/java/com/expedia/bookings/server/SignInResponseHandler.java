package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.StoredPointsCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.Traveler.SeatPreference;
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.data.user.UserPreference;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class SignInResponseHandler extends JsonResponseHandler<SignInResponse> {

	@Override
	public SignInResponse handleJson(JSONObject response) {
		SignInResponse signInResponse = new SignInResponse();

		ParserUtils.logActivityId(response);

		try {
			// Check for errors
			signInResponse.addErrors(ParserUtils.parseErrors(ServerError.ApiMethod.SIGN_IN, response));
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

					List<String> addrLines = new ArrayList<>();
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
						Phone phone = parsePhone(phoneArr.optJSONObject(a));
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
					if (traveler.getPassportCountries().size() > 1) { // multiple passports.
						// force customer to select a passport (#4834)
						traveler.setPrimaryPassportCountry(null);
					}
				}

				//Parse frequent flyer memberships
				if (response.has("frequentFlyerMemberships")) {
					JSONArray ffnArr = response.optJSONArray("frequentFlyerMemberships");
					int size = ffnArr.length();
					for (int a = 0; a < size; a++) {
						JSONObject ffnJson = ffnArr.optJSONObject(a);
						TravelerFrequentFlyerMembership frequentFlyerMembership = new TravelerFrequentFlyerMembership();
						frequentFlyerMembership.setMembershipNumber(ffnJson.optString("membershipNumber", null));
						frequentFlyerMembership.setPlanCode(ffnJson.optString("planCode", null));
						frequentFlyerMembership.setAirlineCode(ffnJson.optString("airlineCode", null));
						traveler.addFrequentFlyerMembership(frequentFlyerMembership);
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
					if (Strings.isNotEmpty(dateOfBirth)) {
						traveler.setBirthDate(LocalDate.parse(dateOfBirth));
					}

					traveler.setRedressNumber(tsaDetails.optString("redressNumber", null));
					traveler.setKnownTravelerNumber(tsaDetails.optString("knownTravelerNumber", null));
				}

				traveler.setSeatPreference(JSONUtils.getEnum(response, "seatPreference", SeatPreference.class));
				traveler.setAssistance(JSONUtils.getEnum(response, "specialAssistance", AssistanceType.class));
				traveler.setIsStoredTraveler(true);
				user.setPrimaryTraveler(traveler);

				// Parse stored credit cards
				JSONArray ccArr = response.optJSONArray("storedCreditCards");
				if (ccArr != null && ccArr.length() > 0) {
					int size = ccArr.length();
					for (int a = 0; a < size; a++) {
						JSONObject sccJson = ccArr.optJSONObject(a);
						StoredCreditCard scc = new StoredCreditCard();
						String type = sccJson.optString("creditCardType", null);
						if (Strings.isNotEmpty(type)) {
							scc.setType(CurrencyUtils.parsePaymentType(type));
							scc.setDescription(sccJson.optString("description", null));
							scc.setId(sccJson.optString("paymentsInstrumentsId", null));
							scc.setNameOnCard(sccJson.optString("nameOnCard", null));
							scc.setExpired(sccJson.optBoolean("expired"));
							user.addStoredCreditCard(scc);
						}
					}
				}

				// Parse stored points cards
				JSONArray pointsCardsArr = response.optJSONArray("storedPointsCards");
				if (pointsCardsArr != null && pointsCardsArr.length() > 0) {
					int size = pointsCardsArr.length();
					for (int a = 0; a < size; a++) {
						JSONObject spcJson = pointsCardsArr.optJSONObject(a);
						StoredPointsCard storedPointsCard = new StoredPointsCard();
						String type = spcJson.optString("creditCardType", null);
						if (Strings.isNotEmpty(type)) {
							storedPointsCard.setPaymentType(CurrencyUtils.parsePaymentType(type));
							storedPointsCard.setDescription(spcJson.optString("description", null));
							storedPointsCard.setPaymentsInstrumentId(spcJson.optString("paymentsInstrumentsId", null));
							user.addStoredPointsCard(storedPointsCard);
						}
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

				user.setRewardsMembershipId(response.optString("loyaltyAccountNumber"));
				if (response.has("loyaltyMembershipInfo")) {
					user.setLoyaltyMembershipInformation(JSONUtils.getJSONable(response, "loyaltyMembershipInfo", UserLoyaltyMembershipInformation.class));
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

	private static Traveler parseBasicTraveler(JSONObject obj) {
		Traveler traveler = new Traveler();
		traveler.setTuid(obj.optLong("tuid"));
		traveler.setExpediaUserId(obj.optLong("expUserId"));
		traveler.setFirstName(obj.optString("firstName", null));
		traveler.setMiddleName(obj.optString("middleName", null));
		traveler.setLastName(obj.optString("lastName", null));
		traveler.setIsStoredTraveler(true);
		return traveler;
	}

	private static Phone parsePhone(JSONObject phoneJson) {
		Phone phone = new Phone();

		// Historically the API has returned phone numbers in two fields, areaCode and number. They are in the
		// process of phasing out 'areaCode'; some of the newer services (user/profile) exhibit this new
		// behavior, although, not all do (such as user/sign-in). We want to unify and simplify our phone number
		// storage and parsing and this method covers all the cases.
		StringBuilder phoneNumberBuilder = new StringBuilder();
		String areaCode = phoneJson.optString("areaCode", null);
		if (Strings.isNotEmpty(areaCode)) {
			phoneNumberBuilder.append(areaCode);
		}
		String phoneNumber = phoneJson.optString("number", null);
		if (Strings.isNotEmpty(phoneNumber)) {
			phoneNumberBuilder.append(phoneNumber);
		}
		phone.setNumber(phoneNumberBuilder.toString());

		phone.setCategory(UserPreference.parseCategoryString(phoneJson.optString("category", null)));
		phone.setCountryCode(phoneJson.optString("countryCode", null));
		phone.setExtensionNumber(phoneJson.optString("extensionNumber", null));

		return phone;
	}
}
