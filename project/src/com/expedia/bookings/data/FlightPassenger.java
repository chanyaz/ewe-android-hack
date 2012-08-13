package com.expedia.bookings.data;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

/**
 * This class represents a flight passenger (traveler) for flight booking
 * @author jdrotos
 *
 */
public class FlightPassenger implements JSONable {

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
	private SeatPreference mSeatPreference = SeatPreference.NONE;
	private AssistanceType mAssistance = AssistanceType.NONE;
	private MealType mMealPreference = MealType.NONE;

	public enum Gender {
		MALE, FEMALE
	}

	public enum SeatPreference {
		NONE, WINDOW, AISLE
	}

	public enum AssistanceType {
		NONE,
		WHEELCHAIR,
		DEFIBRILLATOR
	}

	public enum MealType {
		NONE,
		VEGITARIAN,
		BOOZE
	}

	public FlightPassenger() {
	}

	/***
	 * This constructor copies data from an existing BillingInfo object
	 * into this FlightPassenger instance
	 * @param info
	 */
	public FlightPassenger(BillingInfo info) {
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
	 * into this FlightPassenger instance
	 * @param info
	 */
	public FlightPassenger(User user) {
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
		case NONE:
			retStr = res.getString(R.string.none);
			break;
		default:
			retStr = res.getString(R.string.none);
		}

		return retStr;
	}

	public MealType getMealPreference() {
		return mMealPreference;
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

	public void setMealPreference(MealType preference) {
		mMealPreference = preference;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);

			//TODO:phone num and country code need to be integers
			obj.putOpt("phoneCountryCode", mPhoneCountryCode);
			obj.putOpt("phone", mPhoneNumber);

			obj.putOpt("email", mEmail);
			obj.putOpt("gender", mGender.name());

			//TODO:Calendar 
			//obj.putOpt("birthDate", mBirthDate);

			obj.putOpt("redressNumber", mRedressNumber);
			obj.putOpt("passportCountry", mPassportCountry);
			obj.put("seatPreference", mSeatPreference.name());
			obj.put("assistance", mAssistance.name());
			obj.put("mealPreference", mMealPreference.name());

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mEmail = obj.optString("email", null);
		mFirstName = obj.optString("firstName", null);
		mMiddleName = obj.optString("middleName", null);
		mLastName = obj.optString("lastName", null);

		//TODO:phone num and country code need to be integers
		mPhoneCountryCode = obj.optString("phoneCountryCode");
		mPhoneNumber = obj.optString("phone");

		mGender = Gender.valueOf(obj.optString("gender"));

		//TODO:Calender stuff for birthday...

		mRedressNumber = obj.optString("redressNumber");
		mPassportCountry = obj.optString("passportCountry");

		mSeatPreference = SeatPreference.valueOf(obj.optString("seatPreference"));
		mAssistance = AssistanceType.valueOf(obj.optString("assistance"));
		mMealPreference = MealType.valueOf(obj.optString("mealPreference"));

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
