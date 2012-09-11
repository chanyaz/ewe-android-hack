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
public class Traveler implements JSONable {

	// Expedia
	private Long mTuid = 0L;
	private String mLoyaltyMembershipNumber;

	// General
	private String mFirstName;
	private String mMiddleName;
	private String mLastName;
	private Location mHomeAddress;
	private List<Phone> mPhoneNumbers = new ArrayList<Phone>();
	private String mEmail;

	// Hotels
	private boolean mIsSmokingPreferred;

	// Flights
	private Gender mGender;
	private Date mBirthDate;
	private String mRedressNumber;
	private String mPassportCountry;
	private SeatPreference mSeatPreference = SeatPreference.ANY;
	private AssistanceType mAssistance = AssistanceType.NONE;

	// Utility - not actually coming from the Expedia
	private boolean mSaveTravelerToExpediaAccount = false;

	public enum Gender {
		MALE, FEMALE, OTHER
	}

	public enum SeatPreference {
		ANY, WINDOW, AISLE
	}

	public enum AssistanceType {
		NONE,
		WHEELCHAIR,
		DEFIBRILLATOR,
		SUPER_LONG_ASSISTANCE_TYPE
	}

	public Traveler() {
		// Default constructor
	}

	public BillingInfo toBillingInfo() {
		BillingInfo b = new BillingInfo();
		b.setFirstName(mFirstName);
		b.setLastName(mLastName);

		Phone p = getPrimaryPhoneNumber();
		b.setTelephoneCountryCode(p.getCountryCode());
		b.setTelephone(p.getAreaCode() + p.getNumber());

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

	public String getPassportCountry() {
		return mPassportCountry;
	}

	public SeatPreference getSeatPreference() {
		return mSeatPreference;
	}

	public String getSeatPreferenceString(Context context) {
		SeatPreference pref = getSeatPreference();
		Resources res = context.getResources();
		String retStr = "";

		switch (pref) {
		case ANY:
			retStr = res.getString(R.string.any);
			break;
		case WINDOW:
			retStr = res.getString(R.string.window);
			break;
		case AISLE:
			retStr = res.getString(R.string.aisle);
			break;
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
		case WHEELCHAIR:
			retStr = res.getString(R.string.wheelchair);
			break;
		case DEFIBRILLATOR:
			retStr = res.getString(R.string.defibrillator);
			break;
		case SUPER_LONG_ASSISTANCE_TYPE:
			retStr = res.getString(R.string.super_long_assistance);
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

	public void setPassportCountry(String passportCountry) {
		mPassportCountry = passportCountry;
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
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			obj.putOpt("email", mEmail);

			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

			JSONUtils.putEnum(obj, "gender", mGender);
			JSONUtils.putJSONable(obj, "birthDate", mBirthDate);
			obj.putOpt("redressNumber", mRedressNumber);
			obj.putOpt("passportCountry", mPassportCountry);
			JSONUtils.putEnum(obj, "seatPreference", mSeatPreference);
			JSONUtils.putEnum(obj, "assistance", mAssistance);

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
		mHomeAddress = JSONUtils.getJSONable(obj, "homeAddress", Location.class);
		mPhoneNumbers = JSONUtils.getJSONableList(obj, "phoneNumbers", Phone.class);
		mEmail = obj.optString("email", null);

		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred");

		mGender = JSONUtils.getEnum(obj, "gender", Gender.class);
		mBirthDate = JSONUtils.getJSONable(obj, "birthDate", Date.class);
		mRedressNumber = obj.optString("redressNumber");
		mPassportCountry = obj.optString("passportCountry");
		mSeatPreference = JSONUtils.getEnum(obj, "seatPreference", SeatPreference.class);
		mAssistance = JSONUtils.getEnum(obj, "assistance", AssistanceType.class);

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
}
