package com.expedia.bookings.data;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

/**
 * This class represents a traveler for booking
 * @author jdrotos
 *
 */
public class Traveler implements JSONable {

	private Long mTuid = 0L;

	//These all come from the api...
	private String mFirstName;
	private String mMiddleName;
	private String mLastName;
	private String mPhoneCountryCode;
	private String mPhoneNumber;
	private String mEmail;
	private Gender mGender;

	private Calendar mBirthDate;
	private String mRedressNumber;
	private String mPassportCountry;
	private SeatPreference mSeatPreference = SeatPreference.ANY;
	private AssistanceType mAssistance = AssistanceType.NONE;

	private boolean mSaveTravelerToExpediaAccount = false;

	public enum Gender {
		MALE, FEMALE
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

	}

	/***
	 * This constructor copies data from an existing BillingInfo object
	 * into this Traveler instance
	 * @param info
	 */
	public Traveler(BillingInfo info) {
		if (info == null) {
			return;
		}
		setFirstName(info.getFirstName());
		setLastName(info.getLastName());
		setPhoneCountryCode(info.getTelephoneCountryCode());
		setPhoneNumber(info.getTelephone());
		setEmail(info.getEmail());

	}

	/***
	 * This constructor copies data from an existing User object
	 * into this Traveler instance
	 * @param info
	 */
	public Traveler(User user) {
		if (user == null) {
			return;
		}

		setFirstName(user.getFirstName());
		setMiddleName(user.getMiddleName());
		setLastName(user.getLastName());
		setEmail(user.getEmail());

	}

	///////////////////////////
	// Getters

	public String getFirstName() {
		return mFirstName;
	}

	public String getMiddleName() {
		return mMiddleName;
	}

	public String getLastName() {
		return mLastName;
	}

	public String getPhoneCountryCode() {
		return mPhoneCountryCode;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public String getEmail() {
		return mEmail;
	}

	public Calendar getBirthDate() {
		return mBirthDate;
	}

	public Gender getGender() {
		return mGender;
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

	public boolean getSaveTravelerToExpediaAccount() {
		return mSaveTravelerToExpediaAccount;
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

	public Long getTuid() {
		return mTuid;
	}

	public boolean hasTuid() {
		return (mTuid != 0);
	}

	/***
	 * Does the traveler have non-blank first and last name values
	 * @return
	 */
	public boolean hasName() {
		return !TextUtils.isEmpty(getFirstName()) && !TextUtils.isEmpty(getLastName());

	}

	//////////////////////////
	// Setters

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public void setMiddleName(String middleName) {
		mMiddleName = middleName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public void setPhoneCountryCode(String code) {
		mPhoneCountryCode = code;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public void setBirthDate(Calendar cal) {
		mBirthDate = cal;
	}

	public void setGender(Gender gender) {
		mGender = gender;
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

	public void setTuid(Long tuid) {
		mTuid = tuid;
	}

	public void setSaveTravelerToExpediaAccount(boolean save) {
		mSaveTravelerToExpediaAccount = save;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("tuid", mTuid);

			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);

			//TODO:phone num and country code need to be integers
			obj.putOpt("phoneCountryCode", mPhoneCountryCode);
			obj.putOpt("phone", mPhoneNumber);

			obj.putOpt("email", mEmail);
			
			//TODO:"save" is not a valid key, this is not yet defined in the api
			obj.putOpt("save", mSaveTravelerToExpediaAccount);

			if (mGender != null) {
				obj.putOpt("gender", mGender.name());
			}

			//TODO:Calendar 
			//obj.putOpt("birthDate", mBirthDate);

			obj.putOpt("redressNumber", mRedressNumber);
			obj.putOpt("passportCountry", mPassportCountry);
			if (mSeatPreference != null) {
				obj.put("seatPreference", mSeatPreference.name());
			}
			if (mAssistance != null) {
				obj.put("assistance", mAssistance.name());
			}

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTuid = obj.optLong("tuid");

		mEmail = obj.optString("email", null);
		mFirstName = obj.optString("firstName", null);
		mMiddleName = obj.optString("middleName", null);
		mLastName = obj.optString("lastName", null);

		//TODO:phone num and country code need to be integers
		mPhoneCountryCode = obj.optString("phoneCountryCode");
		mPhoneNumber = obj.optString("phone");

		mSaveTravelerToExpediaAccount = obj.optBoolean("save");
		
		String genderStr = obj.optString("gender");
		if (!TextUtils.isEmpty(genderStr)) {
			mGender = Gender.valueOf(genderStr);
		}

		//TODO:Calender stuff for birthday...

		mRedressNumber = obj.optString("redressNumber");
		mPassportCountry = obj.optString("passportCountry");

		String seatPrefStr = obj.optString("seatPreference");
		if (!TextUtils.isEmpty(seatPrefStr)) {
			mSeatPreference = SeatPreference.valueOf(seatPrefStr);
		}
		String assistanceStr = obj.optString("assistance");
		if (!TextUtils.isEmpty(assistanceStr)) {
			mAssistance = AssistanceType.valueOf(assistanceStr);
		}

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
