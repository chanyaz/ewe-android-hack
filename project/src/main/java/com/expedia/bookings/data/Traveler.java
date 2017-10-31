package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership;
import com.expedia.bookings.data.user.UserPreference.Category;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * This class represents a traveler for booking
 */
public class Traveler implements JSONable, Comparable<Traveler> {

	// Expedia
	private Long mTuid = 0L;
	private Long mExpediaUserId;

	// General
	private TravelerName mName = new TravelerName();
	private Location mHomeAddress;
	private List<Phone> mPhoneNumbers = new ArrayList<>();
	private String mEmail;
	private boolean isStoredTraveler = false;

	// Hotels
	private boolean mIsSmokingPreferred;

	// Flights
	private Gender mGender = Gender.GENDER;
	private LocalDate mBirthDate;
	private String mRedressNumber;
	private String mKnownTravelerNumber;
	private List<String> mPassportCountries;
	private SeatPreference mSeatPreference = SeatPreference.WINDOW;
	private AssistanceType mAssistance = AssistanceType.NONE;
	private PassengerCategory mPassengerCategory;
	private int mSearchedAge = -1;
	private int mAge;
	private Map<String, TravelerFrequentFlyerMembership> frequentFlyerMemberships = new HashMap<>();


	private static final int MIN_CHILD_AGE = 2;
	private static final int MIN_ADULT_CHILD_AGE = 12;
	private static final int MIN_ADULT_AGE = 18;

	private static final String NON_NUMBERS_REGEX = "[^0-9]";

	// Utility - not actually coming from the Expedia
	private boolean mSaveTravelerToExpediaAccount = false;

	// Is the Traveler from Google Wallet?  Treat them differently!
	private boolean mFromGoogleWallet;

	// (Tablet Checkout) When user is logged in, can this traveler be selected from the list of saved travelers or disabled?
	private boolean mIsSelectable = true;

	// (Tablet Checkout) Is the current Traveler being newly added. ONLY used when a user is logged in.
	private boolean mIsNew;

	private boolean mChangedPrimaryPassportCountry;

	public enum Gender {
		GENDER, MALE, FEMALE, OTHER
	}

	//This is silly, we only want to offer WINDOW and AISLE, but when downloading from an expedia account
	//ANY is the default. When commiting a traveler to the account ANY is invalid and we must use UNASSIGNED
	public enum SeatPreference {
		ANY, WINDOW, AISLE, UNASSIGNED
	}

	//These names should be consistance with valid api values
	public enum AssistanceType {
		NONE,
		BLIND_WITH_SEEING_EYE_DOG,
		DEAF_WITH_HEARING_DOG,
		WHEELCHAIR_CAN_CLIMB_STAIRS,
		WHEELCHAIR_CANNOT_CLIMB_STAIRS,
		WHEELCHAIR_IMMOBILE
	}

	public Traveler() {
		// Default constructor
	}

	//////////////////////////////////////////////////////////////////////////
	// Getters

	public Long getTuid() {
		return mTuid;
	}

	public Long getExpediaUserId() {
		return mExpediaUserId;
	}

	public boolean hasTuid() {
		return (mTuid != 0);
	}

	public TravelerName getName() {
		return mName;
	}

	public String getFirstName() {
		return mName.getFirstName();
	}

	@Nullable
	public String getMiddleName() {
		return mName.getMiddleName();
	}

	public String getLastName() {
		return mName.getLastName();
	}

	public String getFullName() {
		return mName.getFullName();
	}

	public String getFirstAndLastName() {
		return mName.getFirstAndLastName();
	}

	public String getReversedFullName() {
		return mName.getReversedFullName();
	}

	public String getFullNameBasedOnPos() {
		if (PointOfSale.getPointOfSale().showLastNameFirst()) {
			return getReversedFullName();
		}
		else {
			return getFullName();
		}
	}

	public Location getHomeAddress() {
		return mHomeAddress;
	}

	// Assumes there is only one primary phone number (which should be
	// a correct assumption).
	public Phone getPrimaryPhoneNumber() {
		for (Phone phone : mPhoneNumbers) {
			if (phone.getCategory() == Category.PRIMARY) {
				return phone;
			}
		}

		// If we got here and still didn't find one, return the first
		// phone # as the default
		if (mPhoneNumbers.size() > 0) {
			return mPhoneNumbers.get(0);
		}

		return null;
	}

	public Phone getOrCreatePrimaryPhoneNumber() {
		Phone phone = getPrimaryPhoneNumber();
		if (phone == null) {
			phone = new Phone();
			phone.setCategory(Category.PRIMARY);
			mPhoneNumbers.add(phone);
		}
		return phone;
	}

	public String getEmail() {
		return mEmail;
	}

	public boolean isSmokingPreferred() {
		return mIsSmokingPreferred;
	}

	public Gender getGender() {
		return mGender;
	}

	@Nullable
	public LocalDate getBirthDate() {
		return mBirthDate;
	}

	public String getRedressNumber() {
		return mRedressNumber;
	}

	public String getKnownTravelerNumber() {
		return mKnownTravelerNumber;
	}

	public boolean hasPassportCountry() {
		return mPassportCountries != null && mPassportCountries.size() != 0;
	}

	public String getPrimaryPassportCountry() {
		if (mPassportCountries == null || mPassportCountries.size() == 0 || Strings.isEmpty(mPassportCountries.get(0))) {
			return null;
		}

		return mPassportCountries.get(0);
	}

	public List<String> getPassportCountries() {
		if (mPassportCountries == null) {
			return Collections.emptyList();
		}
		return mPassportCountries;
	}

	public SeatPreference getSeatPreference() {
		return mSeatPreference;
	}

	public SeatPreference getSafeSeatPreference() {
		if (getSeatPreference().equals(SeatPreference.WINDOW) || getSeatPreference().equals(SeatPreference.AISLE)) {
			return getSeatPreference();
		}
		else {
			return SeatPreference.WINDOW;
		}
	}

	public String getSeatPreferenceString(Context context) {
		SeatPreference pref = getSeatPreference();
		Resources res = context.getResources();
		String retStr;

		switch (pref) {
		case WINDOW:
			retStr = res.getString(R.string.window);
			break;
		case AISLE:
			retStr = res.getString(R.string.aisle);
			break;
		case ANY:
		case UNASSIGNED:
		default:
			retStr = res.getString(R.string.any);
		}
		return retStr;
	}

	public AssistanceType getAssistance() {
		return mAssistance == null ? AssistanceType.NONE : mAssistance;
	}

	public String getAssistanceString(Context context, AssistanceType assistanceType) {
		Resources res = context.getResources();
		String retStr;

		switch (assistanceType) {
		case WHEELCHAIR_IMMOBILE:
			retStr = res.getString(R.string.wheelchair_immobile);
			break;
		case WHEELCHAIR_CANNOT_CLIMB_STAIRS:
			retStr = res.getString(R.string.wheelchair_no_stairs);
			break;
		case WHEELCHAIR_CAN_CLIMB_STAIRS:
			retStr = res.getString(R.string.wheelchair_stairs_ok);
			break;
		case DEAF_WITH_HEARING_DOG:
			retStr = res.getString(R.string.deaf_with_hearing_dog);
			break;
		case BLIND_WITH_SEEING_EYE_DOG:
			retStr = res.getString(R.string.blind_with_guide_dog);
			break;
		case NONE:
			retStr = res.getString(R.string.none);
			break;
		default:
			retStr = res.getString(R.string.none);
		}

		return retStr;
	}

	public void addFrequentFlyerMembership(TravelerFrequentFlyerMembership frequentFlyerMembership) {
		this.frequentFlyerMemberships.put(frequentFlyerMembership.getAirlineCode(), frequentFlyerMembership);
	}

	public Map<String, TravelerFrequentFlyerMembership> getFrequentFlyerMemberships() {
		return frequentFlyerMemberships;
	}

	public void setPassengerCategory(PassengerCategory passengerCategory) {
		mPassengerCategory = passengerCategory;
	}

	@Nullable
	public PassengerCategory getPassengerCategory() {
		return mPassengerCategory;
	}

	public PassengerCategory getPassengerCategory(LocalDate endOfTrip, Boolean infantsInLap) {
		if (mPassengerCategory == null) {
			mPassengerCategory = determinePassengerCategory(endOfTrip, infantsInLap);
		}
		return mPassengerCategory;
	}

	public PassengerCategory getPassengerCategory(FlightSearchParams searchParams) {
		// If we haven't assigned a passengerCategory yet (e.g. passenger is from account)
		// Passenger category is determine by their max age during the duration of their trip
		if (mPassengerCategory == null) {
			LocalDate endOfTrip = searchParams.getReturnDate() != null ?
				searchParams.getReturnDate() :
				searchParams.getDepartureDate();
			mPassengerCategory = determinePassengerCategory(endOfTrip, searchParams.getInfantSeatingInLap());
		}
		return mPassengerCategory;
	}

	private PassengerCategory determinePassengerCategory(LocalDate endOfTrip, boolean infantsInLap) {
		if (endOfTrip != null && mBirthDate != null) {
			int yearsOld = Years.yearsBetween(mBirthDate, endOfTrip).getYears();
			return fromAgeInYearsToPassengerCategory(yearsOld, infantsInLap);
		}
		return null;
	}

	private PassengerCategory fromAgeInYearsToPassengerCategory(int years, boolean infantsInLap) {
		if (years < MIN_CHILD_AGE) {
			if (infantsInLap) {
				return PassengerCategory.INFANT_IN_LAP;
			}
			else {
				return PassengerCategory.INFANT_IN_SEAT;
			}
		}
		else if (years < MIN_ADULT_CHILD_AGE) {
			return PassengerCategory.CHILD;
		}
		else if (years < MIN_ADULT_AGE) {
			return PassengerCategory.ADULT_CHILD;
		}
		else {
			return PassengerCategory.ADULT;
		}
		// The API never returns "SENIOR" right now, so we don't need to, either.
	}

	/***
	 * Does the traveler have non-blank first and last name values
	 */
	public boolean hasName() {
		return !TextUtils.isEmpty(getFirstName()) && !TextUtils.isEmpty(getLastName());
	}

	public boolean getSaveTravelerToExpediaAccount() {
		if (mFromGoogleWallet) {
			return false;
		}

		return mSaveTravelerToExpediaAccount;
	}

	public int getSearchedAge() {
		return mSearchedAge;
	}

	//////////////////////////////////////////////////////////////////////////
	// Setters

	public void setTuid(Long tuid) {
		mTuid = tuid;
	}

	public void setExpediaUserId(Long expediaUserId) {
		mExpediaUserId = expediaUserId;
	}

	public void setFirstName(String firstName) {
		mName.setFirstName(firstName);
	}

	public void setMiddleName(String middleName) {
		mName.setMiddleName(middleName);
	}

	public void setLastName(String lastName) {
		mName.setLastName(lastName);
	}

	/**
	 * Note that setFullName was added for Itins where we get fullname back from the server.
	 * Calling setFullName WILL NOT change the values of first/middle/last name.
	 * However, calling getFullName() will return the value supplied here.
	 * If this method is never called, calling getFullName() will return first + middle + last with proper spacing.
	 */
	public void setFullName(String fullName) {
		mName.setFullName(fullName);
	}

	public void setHomeAddress(Location homeAddress) {
		mHomeAddress = homeAddress;
	}

	public void addPhoneNumber(Phone phoneNumber) {
		mPhoneNumbers.add(phoneNumber);
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public void setSmokingPreferred(boolean isPreferred) {
		mIsSmokingPreferred = isPreferred;
	}

	public void setGender(Gender gender) {
		mGender = gender;
	}

	public void setBirthDate(LocalDate date) {
		mBirthDate = date;
	}

	public void setRedressNumber(String redressNumber) {
		mRedressNumber = redressNumber;
	}

	public void setKnownTravelerNumber(String knownTravelerNumber) {
		mKnownTravelerNumber = knownTravelerNumber;
	}

	public void setPrimaryPassportCountry(String passportCountry) {
		mChangedPrimaryPassportCountry = true;
		if (mPassportCountries != null && mPassportCountries.size() > 0) {
			boolean countryFound = false;

			// See if the country is already in the list, if so set it as primary and move old primary
			if (passportCountry != null) {
				for (int i = 0; i < mPassportCountries.size(); i++) {
					String pCountry = mPassportCountries.get(i);
					if (pCountry != null && passportCountry.compareToIgnoreCase(pCountry) == 0) {
						mPassportCountries.set(i, mPassportCountries.get(0));
						mPassportCountries.set(0, passportCountry);
						countryFound = true;
						break;
					}
				}
			}

			//It wasn't in the list, so we just add it to the front making it the first one.
			if (!countryFound) {
				mPassportCountries.add(0, passportCountry);
			}
		}
		else {
			addPassportCountry(passportCountry);
		}
	}

	public void addPassportCountry(String passportCountry) {
		if (mPassportCountries == null) {
			mPassportCountries = new ArrayList<>();
		}

		mPassportCountries.add(passportCountry);
	}

	public void setSeatPreference(SeatPreference pref) {
		if (pref == null || pref == SeatPreference.UNASSIGNED || pref == SeatPreference.ANY) {
			mSeatPreference = SeatPreference.WINDOW;
		}
		else {
			mSeatPreference = pref;
		}
	}

	public void setAssistance(AssistanceType assistance) {
		mAssistance = assistance;
	}

	public void setSaveTravelerToExpediaAccount(boolean save) {
		mSaveTravelerToExpediaAccount = save;
	}

	public void setFromGoogleWallet(boolean fromGoogleWallet) {
		mFromGoogleWallet = fromGoogleWallet;
	}

	public void setSearchedAge(int searchedAge) {
		mSearchedAge = searchedAge;
	}

	public boolean fromGoogleWallet() {
		return mFromGoogleWallet;
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience methods

	// Most of the time we only want to deal with a single, primarly phone #
	// So the methods below handle that case

	public void setPhoneNumber(String phoneNumber) {
		getOrCreatePrimaryPhoneNumber().setNumber(phoneNumber);
	}

	public String getPhoneNumber() {
		return getOrCreatePrimaryPhoneNumber().getNumber();
	}

	public void setPhoneCountryCode(String phoneCountryCode) {
		getOrCreatePrimaryPhoneNumber().setCountryCode(phoneCountryCode);
	}

	public String getPhoneCountryCode() {
		return getOrCreatePrimaryPhoneNumber().getCountryCode();
	}

	public void setPhoneCountryName(String phoneCountryCode) {
		getOrCreatePrimaryPhoneNumber().setCountryName(phoneCountryCode);
	}

	public String getPhoneCountryName() {
		return getOrCreatePrimaryPhoneNumber().getCountryName();
	}

	public boolean isSelectable() {
		return mIsSelectable;
	}

	public void setIsSelectable(boolean isSelectable) {
		mIsSelectable = isSelectable;
	}

	public boolean isNew() {
		return mIsNew;
	}

	public void setIsNew(boolean isNew) {
		mIsNew = isNew;
	}
//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("tuid", mTuid);
			obj.putOpt("expUserId", mExpediaUserId);

			mName.toJson(obj);
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			obj.putOpt("email", mEmail);

			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

			JSONUtils.putEnum(obj, "gender", mGender);
			JodaUtils.putLocalDateInJson(obj, "birthLocalDate", mBirthDate);
			obj.putOpt("redressNumber", mRedressNumber);
			obj.putOpt("knownTravelerNumber", mKnownTravelerNumber);
			JSONUtils.putStringList(obj, "passportCountries", mPassportCountries);
			JSONUtils.putEnum(obj, "seatPreference", mSeatPreference);
			JSONUtils.putEnum(obj, "assistance", mAssistance);
			JSONUtils.putJSONableStringMap(obj, "frequentFlyerMemberships", frequentFlyerMemberships);

			JSONUtils.putEnum(obj, "passengerCategory", mPassengerCategory);

			obj.putOpt("searchedAge", mSearchedAge);

			obj.putOpt("saveToExpediaAccount", mSaveTravelerToExpediaAccount);

			obj.putOpt("fromGoogleWallet", mFromGoogleWallet);

			obj.putOpt("age", mAge);

			obj.putOpt("isSelectable", mIsSelectable);

			obj.putOpt("isNew", mIsNew);

			obj.putOpt("isChangedPrimaryPassportCountry", mChangedPrimaryPassportCountry);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException("Could not convert Traveler to JSON", e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTuid = obj.optLong("tuid");
		mExpediaUserId = obj.optLong("expUserId");

		mName.fromJson(obj);
		mHomeAddress = JSONUtils.getJSONable(obj, "homeAddress", Location.class);
		mPhoneNumbers = JSONUtils.getJSONableList(obj, "phoneNumbers", Phone.class);
		mEmail = obj.optString("email", null);

		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred");

		mGender = JSONUtils.getEnum(obj, "gender", Gender.class);
		mBirthDate = JodaUtils.getLocalDateFromJson(obj, "birthLocalDate");
		mRedressNumber = obj.optString("redressNumber");
		mKnownTravelerNumber = obj.optString("knownTravelerNumber");
		frequentFlyerMemberships = JSONUtils.getJSONableStringMap(obj, "frequentFlyerMemberships", TravelerFrequentFlyerMembership.class, new HashMap<String, TravelerFrequentFlyerMembership>());

		mPassportCountries = JSONUtils.getStringList(obj, "passportCountries");
		setSeatPreference(JSONUtils.getEnum(obj, "seatPreference", SeatPreference.class));
		mAssistance = JSONUtils.getEnum(obj, "assistance", AssistanceType.class);

		mPassengerCategory = JSONUtils.getEnum(obj, "passengerCategory", PassengerCategory.class);

		mSearchedAge = obj.optInt("searchedAge");

		mSaveTravelerToExpediaAccount = obj.optBoolean("saveToExpediaAccount");

		mFromGoogleWallet = obj.optBoolean("fromGoogleWallet");

		mAge = obj.optInt("age");

		mIsSelectable = obj.optBoolean("isSelectable");

		mIsNew = obj.optBoolean("isNew");

		mChangedPrimaryPassportCountry = obj.optBoolean("isChangedPrimaryPassportCountry");

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	private static final int NOT_EQUAL = -1;
	private static final int EQUAL = 0;

	public boolean nameEquals(Traveler another) {
		if (another == null) {
			return false;
		}
		return mName.equals(another.mName);
	}

	private static final int BEFORE = -1;
	private static final int AFTER = 1;

	@Override
	public int compareTo(Traveler another) {
		if (this == another) {
			//same ref
			return EQUAL;
		}
		if (another == null) {
			return BEFORE;
		}

		int diff;

		if (!mName.equals(another.mName)) {
			return NOT_EQUAL;
		}

		// Home address
		diff = compareJsonable(getHomeAddress(), another.getHomeAddress());
		if (diff != 0) {
			return diff;
		}

		// Phone number
		diff = Strings.compareTo(getPhoneNumber().replaceAll(NON_NUMBERS_REGEX, ""), another.getPhoneNumber().replaceAll(NON_NUMBERS_REGEX, ""));
		if (diff != 0) {
			return diff;
		}

		// Phone country code
		diff = Strings.compareTo(getPhoneCountryCode(), another.getPhoneCountryCode());
		if (diff != 0) {
			return diff;
		}

		// Email
		diff = Strings.compareTo(getEmail(), another.getEmail());
		if (diff != 0) {
			return diff;
		}

		// Smoking preference
		diff = compareBooleans(isSmokingPreferred(), another.isSmokingPreferred());
		if (diff != 0) {
			return diff;
		}

		// Gender
		diff = compareEnum(getGender(), another.getGender());
		if (diff != 0) {
			return diff;
		}

		// Birth date
		if (getBirthDate() == null || another.getBirthDate() == null) {
			return diff;
		}
		diff = getBirthDate().compareTo(another.getBirthDate());
		if (diff != 0) {
			return diff;
		}

		// Redress #
		diff = Strings.compareTo(getRedressNumber(), another.getRedressNumber());
		if (diff != 0) {
			return diff;
		}

		diff = Strings.compareTo(getKnownTravelerNumber(), another.getKnownTravelerNumber());
		if (diff != 0) {
			return diff;
		}

		// Passport countries
		diff = compareBooleans(hasPassportCountry(), another.hasPassportCountry());
		if (diff != 0) {
			return diff;
		}
		else if (hasPassportCountry() && !mPassportCountries.equals(another.mPassportCountries)) {
			// Compare list length
			int mySize = mPassportCountries.size();
			diff = mySize - another.mPassportCountries.size();
			if (diff != 0) {
				return diff;
			}

			// Compare each item
			for (int a = 0; a < mySize; a++) {
				diff = Strings.compareTo(mPassportCountries.get(a), another.mPassportCountries.get(a));
				if (diff != 0) {
					return diff;
				}
			}
		}

		// Seat preference
		diff = compareEnum(getSeatPreference(), another.getSeatPreference());
		if (diff != 0) {
			return diff;
		}

		// Assistance
		diff = compareEnum(getAssistance(), another.getAssistance());
		if (diff != 0) {
			return diff;
		}

		// Google Wallet
		diff = compareBooleans(fromGoogleWallet(), another.fromGoogleWallet());
		if (diff != 0) {
			return diff;
		}

		return EQUAL;
	}

	private static int compareBooleans(boolean a, boolean b) {
		if (a != b) {
			return a ? -1 : 1;
		}
		return 0;
	}

	private static int compareEnum(Enum<?> a, Enum<?> b) {
		int ordinalA = a != null ? a.ordinal() : Integer.MAX_VALUE;
		int ordinalB = b != null ? b.ordinal() : Integer.MAX_VALUE;
		return ordinalA - ordinalB;
	}

	private static int compareJsonable(JSONable a, JSONable b) {
		String jsonA = a != null ? a.toJson().toString() : "";
		String jsonB = b != null ? b.toJson().toString() : "";
		return jsonA.compareTo(jsonB);
	}

	public int getAge() {
		return mAge;
	}

	public void setAge(int age) {
		mAge = age;
	}

	public boolean isChangedPrimaryPassportCountry() {
		return mChangedPrimaryPassportCountry;
	}

	public void setIsStoredTraveler(boolean isStoredTraveler) {
		this.isStoredTraveler = isStoredTraveler;
	}

	public boolean isStoredTraveler() {
		return isStoredTraveler;
	}
}
