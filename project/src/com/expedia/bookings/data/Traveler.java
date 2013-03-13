package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.UserPreference.Category;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * This class represents a traveler for booking
 */
public class Traveler implements JSONable, Comparable<Traveler> {

	// Expedia
	private Long mTuid = 0L;
	private String mLoyaltyMembershipNumber;

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
	private Gender mGender;
	private Date mBirthDate;
	private String mRedressNumber;
	private List<String> mPassportCountries;
	private SeatPreference mSeatPreference = SeatPreference.WINDOW;
	private AssistanceType mAssistance = AssistanceType.NONE;

	// Activities
	private boolean mIsRedeemer;

	// Utility - not actually coming from the Expedia
	private boolean mSaveTravelerToExpediaAccount = false;

	public enum Gender {
		MALE, FEMALE, OTHER
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

	public BillingInfo toBillingInfo() {
		BillingInfo b = new BillingInfo();
		b.setFirstName(mFirstName);
		b.setLastName(mLastName);

		Phone p = getPrimaryPhoneNumber();
		if (p != null) {
			b.setTelephoneCountryCode(p.getCountryCode());
			b.setTelephone(p.getAreaCode() + p.getNumber());
		}

		b.setEmail(mEmail);
		b.setLocation(mHomeAddress);

		// TODO: CC info

		return b;
	}

	//////////////////////////////////////////////////////////////////////////
	// Getters

	public Long getTuid() {
		return mTuid;
	}

	public boolean hasTuid() {
		return (mTuid != 0);
	}

	public void resetTuid() {
		mTuid = 0L;
	}

	public String getLoyaltyMembershipNumber() {
		if (TextUtils.isEmpty(mLoyaltyMembershipNumber)) {
			return null;
		}
		return mLoyaltyMembershipNumber;
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

	public Date getBirthDate() {
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
		return mAssistance;
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

	/***
	 * Does the traveler have non-blank first and last name values
	 * @return
	 */
	public boolean hasName() {
		return !TextUtils.isEmpty(getFirstName()) && !TextUtils.isEmpty(getLastName());
	}

	public boolean getSaveTravelerToExpediaAccount() {
		return mSaveTravelerToExpediaAccount;
	}

	public boolean getIsRedeemer() {
		return mIsRedeemer;
	}

	//////////////////////////////////////////////////////////////////////////
	// Setters

	public void setTuid(Long tuid) {
		mTuid = tuid;
	}

	public void setLoyaltyMembershipNumber(String loyaltyMembershipNumber) {
		mLoyaltyMembershipNumber = loyaltyMembershipNumber;
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

	public void setBirthDate(Date date) {
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

	public void setPhoneAreaCode(String areaCode) {
		getOrCreatePrimaryPhoneNumber().setAreaCode(areaCode);
	}

	public String getPhoneAreaCode() {
		return getOrCreatePrimaryPhoneNumber().getAreaCode();
	}

	public void setPhoneCountryCode(String phoneCountryCode) {
		getOrCreatePrimaryPhoneNumber().setCountryCode(phoneCountryCode);
	}

	public String getPhoneCountryCode() {
		return getOrCreatePrimaryPhoneNumber().getCountryCode();
	}

	// Quick way to get birth date in milliseconds (good for formatting)
	public long getBirthDateInMillis() {
		return mBirthDate.getCalendar().getTimeInMillis();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("tuid", mTuid);
			obj.putOpt("loyaltyMembershipNumber", mLoyaltyMembershipNumber);

			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);
			obj.putOpt("fullName", mFullName);
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			obj.putOpt("email", mEmail);

			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

			JSONUtils.putEnum(obj, "gender", mGender);
			JSONUtils.putJSONable(obj, "birthDate", mBirthDate);
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

			obj.putOpt("isRedeemer", mIsRedeemer);

			obj.putOpt("saveToExpediaAccount", mSaveTravelerToExpediaAccount);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException("Could not convert Traveler to JSON", e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTuid = obj.optLong("tuid");
		mLoyaltyMembershipNumber = obj.optString("loyaltyMembershipNumber", null);

		mFirstName = obj.optString("firstName", null);
		mMiddleName = obj.optString("middleName", null);
		mLastName = obj.optString("lastName", null);
		mFullName = obj.optString("fullName", null);
		mHomeAddress = JSONUtils.getJSONable(obj, "homeAddress", Location.class);
		mPhoneNumbers = JSONUtils.getJSONableList(obj, "phoneNumbers", Phone.class);
		mEmail = obj.optString("email", null);

		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred");

		mGender = JSONUtils.getEnum(obj, "gender", Gender.class);
		mBirthDate = JSONUtils.getJSONable(obj, "birthDate", Date.class);
		mRedressNumber = obj.optString("redressNumber");
		mPassportCountries = JSONUtils.getStringList(obj, "passportCountries");
		mSeatPreference = JSONUtils.getEnum(obj, "seatPreference", SeatPreference.class);
		if (mSeatPreference.equals(SeatPreference.ANY) || mSeatPreference.equals(SeatPreference.UNASSIGNED)) {
			//We only want to support window and AISLE with window being the default
			mSeatPreference = SeatPreference.WINDOW;
		}
		mAssistance = JSONUtils.getEnum(obj, "assistance", AssistanceType.class);

		mIsRedeemer = obj.optBoolean("isRedeemer");

		mSaveTravelerToExpediaAccount = obj.optBoolean("saveToExpediaAccount");

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

		if ((getFirstName() == null && another.getFirstName() != null)
				|| (getFirstName() != null && getFirstName().compareTo(another.getFirstName()) != 0)) {
			return getFirstName() == null ? BEFORE : getFirstName().compareTo(another.getFirstName());
		}

		if ((getMiddleName() == null && another.getMiddleName() != null)
				|| (getMiddleName() != null && getMiddleName().compareTo(another.getMiddleName()) != 0)) {
			return getMiddleName() == null ? BEFORE : getMiddleName().compareTo(another.getMiddleName());
		}

		if ((getLastName() == null && another.getLastName() != null)
				|| (getLastName() != null && getLastName().compareTo(another.getLastName()) != 0)) {
			return getLastName() == null ? BEFORE : getLastName().compareTo(another.getLastName());
		}

		if ((getHomeAddress() == null && another.getHomeAddress() != null)
				|| (getHomeAddress() != null && getHomeAddress().toJson().toString()
						.compareTo(another.getHomeAddress().toJson().toString()) != 0)) {
			return getHomeAddress() == null ? BEFORE : getHomeAddress().toJson().toString()
					.compareTo(another.getHomeAddress().toJson().toString());
		}

		if ((getPhoneNumber() == null && another.getPhoneNumber() != null)
				|| (getPhoneNumber() != null && getPhoneNumber().compareTo(another.getPhoneNumber()) != 0)) {
			return getPhoneNumber() == null ? BEFORE : getPhoneNumber().compareTo(another.getPhoneNumber());
		}
		if ((getPhoneCountryCode() == null && another.getPhoneCountryCode() != null)
				|| (getPhoneCountryCode() != null && getPhoneCountryCode().compareTo(another.getPhoneCountryCode()) != 0)) {
			return getPhoneCountryCode() == null ? BEFORE : getPhoneCountryCode().compareTo(
					another.getPhoneCountryCode());
		}
		if ((getEmail() == null && another.getEmail() != null)
				|| (getEmail() != null && getEmail().compareTo(another.getEmail()) != 0)) {
			return getEmail() == null ? BEFORE : getEmail().compareTo(another.getEmail());
		}

		if (isSmokingPreferred() != another.isSmokingPreferred()) {
			return BEFORE;
		}

		if ((getGender() == null && another.getGender() != null)
				|| (getGender() != null && getGender().compareTo(another.getGender()) != 0)) {
			return getGender() == null ? BEFORE : getGender().compareTo(another.getGender());
		}

		if ((getBirthDate() == null && another.getBirthDate() != null)
				|| (getBirthDate() != null && getBirthDate().toJson().toString()
						.compareTo(another.getBirthDate().toJson().toString()) != 0)) {
			return getBirthDate() == null ? BEFORE : getBirthDate().toJson().toString()
					.compareTo(another.getBirthDate().toJson().toString());
		}
		if ((getRedressNumber() == null && another.getRedressNumber() != null)
				|| (getRedressNumber() != null && getRedressNumber().compareTo(another.getRedressNumber()) != 0)) {
			return getRedressNumber() == null ? BEFORE : getRedressNumber().compareTo(another.getRedressNumber());
		}

		if (hasPassportCountry() != another.hasPassportCountry()) {
			return BEFORE;
		}
		else if (hasPassportCountry() && !mPassportCountries.equals(another.mPassportCountries)) {
			//Compare list length
			int mySize = mPassportCountries.size();
			if (mySize != another.mPassportCountries.size()) {
				return mySize < another.mPassportCountries.size() ? BEFORE : AFTER;
			}

			// Compare each item
			for (int a = 0; a < mySize; a++) {
				int compared = mPassportCountries.get(a).compareTo(another.mPassportCountries.get(a));
				if (compared != 0) {
					return compared;
				}
			}
		}

		if ((getSeatPreference() == null && another.getSeatPreference() != null)
				|| (getSeatPreference() != null && getSeatPreference().compareTo(another.getSeatPreference()) != 0)) {
			return getSeatPreference() == null ? BEFORE : getSeatPreference().compareTo(another.getSeatPreference());
		}

		if ((getAssistance() == null && another.getAssistance() != null)
				|| (getAssistance() != null && getAssistance().compareTo(another.getAssistance()) != 0)) {
			return getAssistance() == null ? BEFORE : getAssistance().compareTo(another.getAssistance());
		}

		return EQUAL;
	}
}
