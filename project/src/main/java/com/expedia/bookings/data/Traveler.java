package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.UserPreference.Category;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * This class represents a traveler for booking
 */
public class Traveler implements JSONable, Comparable<Traveler> {

	// Expedia
	private Long mTuid = 0L;
	private String mLoyaltyMembershipNumber;
	private String mLoyaltyMembershipName;
	private boolean mIsLoyaltyMembershipActive = false;
	private LoyaltyMembershipTier mLoyaltyMembershipTier = LoyaltyMembershipTier.NONE;
	private Long mExpediaUserId;

	// General
	private String mFirstName;
	private String mMiddleName;
	private String mLastName;
	private String mFullName;
	private Location mHomeAddress;
	private List<Phone> mPhoneNumbers = new ArrayList<Phone>();
	private String mEmail;

	// Hotels
	private boolean mIsSmokingPreferred;

	// Flights
	private Gender mGender = Gender.MALE;
	private LocalDate mBirthDate;
	private String mRedressNumber;
	private List<String> mPassportCountries;
	private SeatPreference mSeatPreference = SeatPreference.WINDOW;
	private AssistanceType mAssistance = AssistanceType.NONE;
	private PassengerCategory mPassengerCategory;
	private int mSearchedAge = -1;
	private int mAge;


	private static final int MIN_CHILD_AGE = 2;
	private static final int MIN_ADULT_CHILD_AGE = 12;
	private static final int MIN_ADULT_AGE = 18;

	// Activities
	private boolean mIsRedeemer;

	// Utility - not actually coming from the Expedia
	private boolean mSaveTravelerToExpediaAccount = false;

	// Is the Traveler from Google Wallet?  Treat them differently!
	private boolean mFromGoogleWallet;

	// (Tablet Checkout) When user is logged in, can this traveler be selected from the list of saved travelers or disabled?
	private boolean mIsSelectable = true;

	// (Tablet Checkout) Is the current Traveler being newly added. ONLY used when a user is logged in.
	private boolean mIsNew;

	public enum Gender {
		MALE, FEMALE, OTHER
	}

	public enum LoyaltyMembershipTier {
		NONE, BLUE, SILVER, GOLD;

		public boolean isGoldOrSilver() {
			return this == SILVER || this == GOLD;
		}
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

	public void resetTuid() {
		mTuid = 0L;
	}

	public String getLoyaltyMembershipNumber() {
		if (!mIsLoyaltyMembershipActive || TextUtils.isEmpty(mLoyaltyMembershipNumber)) {
			return null;
		}
		return mLoyaltyMembershipNumber;
	}

	public boolean getIsLoyaltyMembershipActive() {
		return mIsLoyaltyMembershipActive;
	}

	public String getLoyaltyMembershipName() {
		return mLoyaltyMembershipName;
	}

	public LoyaltyMembershipTier getLoyaltyMembershipTier() {
		return mLoyaltyMembershipTier;
	}

	public boolean isLoyaltyMember() {
		return mLoyaltyMembershipTier != LoyaltyMembershipTier.NONE;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public String getMiddleName() {
		return mMiddleName;
	}

	public String getLastName() {
		return mLastName;
	}

	/**
	 * If we have called setFullName() any time in the past, we return the value supplied then.
	 * If we have never called setFullName() then we return first + middle + last with proper spacing.
	 * @return
	 */
	public String getFullName() {
		if (TextUtils.isEmpty(mFullName)) {
			String fullName = "";
			if (!TextUtils.isEmpty(mFirstName)) {
				fullName += mFirstName;
			}
			if (!TextUtils.isEmpty(mMiddleName)) {
				fullName += " " + mMiddleName;
			}
			if (!TextUtils.isEmpty(mLastName)) {
				fullName += " " + mLastName;
			}
			return fullName.trim();
		}
		return mFullName;
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

	public List<Phone> getPhoneNumbers() {
		return mPhoneNumbers;
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

	public LocalDate getBirthDate() {
		return mBirthDate;
	}

	public String getRedressNumber() {
		return mRedressNumber;
	}

	public boolean hasPassportCountry() {
		return mPassportCountries != null && mPassportCountries.size() != 0;
	}

	public String getPrimaryPassportCountry() {
		if (mPassportCountries == null || mPassportCountries.size() == 0) {
			return null;
		}
		return mPassportCountries.get(0);
	}

	public List<String> getPassportCountries() {
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
		String retStr = "";

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

	public String getAssistanceString(Context context) {
		AssistanceType assistanceType = getAssistance();
		Resources res = context.getResources();
		String retStr = "";

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
			retStr = res.getString(R.string.blind_with_seeing_eye_dog);
			break;
		case NONE:
			retStr = res.getString(R.string.none);
			break;
		default:
			retStr = res.getString(R.string.none);
		}

		return retStr;
	}

	public void setPassengerCategory(PassengerCategory passengerCategory) {
		mPassengerCategory = passengerCategory;
	}

	public void determinePassengerCategory() {
		LocalDate endOfTrip = Db.getTripBucket().getFlight().getFlightSearchParams().getReturnDate() != null ?
			Db.getTripBucket().getFlight().getFlightSearchParams().getReturnDate() :
			Db.getTripBucket().getFlight().getFlightSearchParams().getDepartureDate();
		if (endOfTrip != null && mBirthDate != null) {
			int yearsOld = Years.yearsBetween(mBirthDate, endOfTrip).getYears();
			mPassengerCategory = yearsToPassengerCategory(yearsOld);
		}
	}

	public PassengerCategory getPassengerCategory() {
		// If we haven't assigned a passengerCategory yet (e.g. passenger is from account)
		// Passenger category is determine by their max age during the duration of their trip
		if (mPassengerCategory == null) {
			determinePassengerCategory();
		}
		return mPassengerCategory;
	}

	private PassengerCategory yearsToPassengerCategory(int years) {
		if (years < MIN_CHILD_AGE) {
			if (Db.getTripBucket().getFlight().getFlightSearchParams().getInfantSeatingInLap()) {
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
	 * @return
	 */
	public boolean hasName() {
		return !TextUtils.isEmpty(getFirstName()) && !TextUtils.isEmpty(getLastName());
	}

	public boolean hasEmail() {
		return !TextUtils.isEmpty(getEmail());
	}

	public boolean getSaveTravelerToExpediaAccount() {
		if (mFromGoogleWallet) {
			return false;
		}

		return mSaveTravelerToExpediaAccount;
	}

	public boolean getIsRedeemer() {
		return mIsRedeemer;
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

	public void setLoyaltyMembershipNumber(String loyaltyMembershipNumber) {
		mLoyaltyMembershipNumber = loyaltyMembershipNumber;
	}

	public void setLoyaltyMembershipActive(boolean active) {
		mIsLoyaltyMembershipActive = active;
	}

	public void setLoyaltyMembershipName(String loyaltyMembershipName) {
		mLoyaltyMembershipName = loyaltyMembershipName;
	}

	public void setLoyaltyMembershipTier(String tierString) {
		try {
			if (tierString != null) {
				setLoyaltyMembershipTier(LoyaltyMembershipTier.valueOf(tierString.toUpperCase()));
				return;
			}
		}
		catch (IllegalArgumentException e) {
			// tierString doesn't match anything
		}
		setLoyaltyMembershipTier(LoyaltyMembershipTier.NONE);
	}

	public void setLoyaltyMembershipTier(LoyaltyMembershipTier loyaltyMembershipTier) {
		mLoyaltyMembershipTier = loyaltyMembershipTier;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public void setMiddleName(String middleName) {
		mMiddleName = middleName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	/**
	 * Note that setFullName was added for Itins where we get fullname back from the server.
	 * Calling setFullName WILL NOT change the values of first/middle/last name.
	 * However, calling getFullName() will return the value supplied here.
	 * If this method is never called, calling getFullName() will return first + middle + last with proper spacing.
	 * @param fullName
	 */
	public void setFullName(String fullName) {
		mFullName = fullName;
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

	public void setPrimaryPassportCountry(String passportCountry) {
		if (mPassportCountries != null && mPassportCountries.size() > 0) {
			boolean countryFound = false;

			//See if the country is already in the list, if so set it as primary and move old primary
			for (int i = 0; i < mPassportCountries.size(); i++) {
				if (passportCountry.compareToIgnoreCase(mPassportCountries.get(i)) == 0) {
					mPassportCountries.set(i, mPassportCountries.get(0));
					mPassportCountries.set(0, passportCountry);
					countryFound = true;
					break;
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
			mPassportCountries = new ArrayList<String>();
		}

		mPassportCountries.add(passportCountry);
	}

	public void setSeatPreference(SeatPreference pref) {
		mSeatPreference = pref;
	}

	public void setAssistance(AssistanceType assistance) {
		mAssistance = assistance;
	}

	public void setSaveTravelerToExpediaAccount(boolean save) {
		mSaveTravelerToExpediaAccount = save;
	}

	public void setIsRedeemer(boolean isRedeemer) {
		mIsRedeemer = isRedeemer;
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
			obj.putOpt("loyaltyMembershipNumber", mLoyaltyMembershipNumber);
			obj.putOpt("loyaltyMemebershipActive", mIsLoyaltyMembershipActive);
			obj.putOpt("loyaltyMemebershipName", mLoyaltyMembershipName);
			obj.putOpt("membershipTier", mLoyaltyMembershipTier.name());

			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);
			obj.putOpt("fullName", mFullName);
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			obj.putOpt("email", mEmail);

			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

			JSONUtils.putEnum(obj, "gender", mGender);
			JodaUtils.putLocalDateInJson(obj, "birthLocalDate", mBirthDate);
			obj.putOpt("redressNumber", mRedressNumber);
			JSONUtils.putStringList(obj, "passportCountries", mPassportCountries);
			if (mSeatPreference.equals(SeatPreference.ANY) || mSeatPreference.equals(SeatPreference.UNASSIGNED)) {
				//We only want to support window and AISLE with window being the default
				JSONUtils.putEnum(obj, "seatPreference", SeatPreference.WINDOW);
			}
			else {
				JSONUtils.putEnum(obj, "seatPreference", mSeatPreference);
			}
			JSONUtils.putEnum(obj, "assistance", mAssistance);

			JSONUtils.putEnum(obj, "passengerCategory", mPassengerCategory);

			obj.putOpt("searchedAge", mSearchedAge);

			obj.putOpt("isRedeemer", mIsRedeemer);

			obj.putOpt("saveToExpediaAccount", mSaveTravelerToExpediaAccount);

			obj.putOpt("fromGoogleWallet", mFromGoogleWallet);

			obj.putOpt("age", mAge);

			obj.putOpt("isSelectable", mIsSelectable);

			obj.putOpt("isNew", mIsNew);

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
		mLoyaltyMembershipNumber = obj.optString("loyaltyMembershipNumber", null);
		mIsLoyaltyMembershipActive = obj.optBoolean("loyaltyMemebershipActive", false);
		mLoyaltyMembershipName = obj.optString("loyaltyMemebershipName", null);
		setLoyaltyMembershipTier(obj.optString("membershipTier", null));

		mFirstName = obj.optString("firstName", null);
		mMiddleName = obj.optString("middleName", null);
		mLastName = obj.optString("lastName", null);
		mFullName = obj.optString("fullName", null);
		mHomeAddress = JSONUtils.getJSONable(obj, "homeAddress", Location.class);
		mPhoneNumbers = JSONUtils.getJSONableList(obj, "phoneNumbers", Phone.class);
		mEmail = obj.optString("email", null);

		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred");

		mGender = JSONUtils.getEnum(obj, "gender", Gender.class);
		mBirthDate = JodaUtils.getLocalDateFromJsonBackCompat(obj, "birthLocalDate", "birthDate");
		mRedressNumber = obj.optString("redressNumber");
		mPassportCountries = JSONUtils.getStringList(obj, "passportCountries");
		mSeatPreference = JSONUtils.getEnum(obj, "seatPreference", SeatPreference.class);
		if (mSeatPreference.equals(SeatPreference.ANY) || mSeatPreference.equals(SeatPreference.UNASSIGNED)) {
			//We only want to support window and AISLE with window being the default
			mSeatPreference = SeatPreference.WINDOW;
		}
		mAssistance = JSONUtils.getEnum(obj, "assistance", AssistanceType.class);

		mPassengerCategory = JSONUtils.getEnum(obj, "passengerCategory", PassengerCategory.class);

		mSearchedAge = obj.optInt("searchedAge");

		mIsRedeemer = obj.optBoolean("isRedeemer");

		mSaveTravelerToExpediaAccount = obj.optBoolean("saveToExpediaAccount");

		mFromGoogleWallet = obj.optBoolean("fromGoogleWallet");

		mAge = obj.optInt("age");

		mIsSelectable = obj.optBoolean("isSelectable");

		mIsNew = obj.optBoolean("isNew");

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

	/**
	 * Compare the name of this traveler to another traveler (currently we just compare first and last name)
	 * If either traveler has null for their first or last name we consider them not equal
	 * @param another
	 * @return
	 */
	public int compareNameTo(Traveler another) {
		final int NOT_EQUAL = -1;
		final int EQUAL = 0;

		if (this == another) {
			//same ref
			return EQUAL;
		}
		if (another == null) {
			return NOT_EQUAL;
		}

		if (this.getFirstName() != null && this.getLastName() != null && another.getFirstName() != null
				&& another.getLastName() != null) {
			if (this.getFirstName().trim().compareToIgnoreCase(another.getFirstName().trim()) == 0
					&& this.getLastName().trim().compareToIgnoreCase(another.getLastName().trim()) == 0) {
				return EQUAL;
			}
			else {
				return NOT_EQUAL;
			}
		}
		return NOT_EQUAL;
	}

	@Override
	public int compareTo(Traveler another) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this == another) {
			//same ref
			return EQUAL;
		}
		if (another == null) {
			return BEFORE;
		}

		int diff = 0;

		// First name
		diff = Strings.compareTo(getFirstName(), another.getFirstName());
		if (diff != 0) {
			return diff;
		}

		// Middle name
		diff = Strings.compareTo(getMiddleName(), another.getMiddleName());
		if (diff != 0) {
			return diff;
		}

		// Last name
		diff = Strings.compareTo(getLastName(), another.getLastName());
		if (diff != 0) {
			return diff;
		}

		// Home address
		diff = compareJsonable(getHomeAddress(), another.getHomeAddress());
		if (diff != 0) {
			return diff;
		}

		// Phone number
		diff = Strings.compareTo(getPhoneNumber(), another.getPhoneNumber());
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

	public void setAge(int mAge) {
		this.mAge = mAge;
	}
}
