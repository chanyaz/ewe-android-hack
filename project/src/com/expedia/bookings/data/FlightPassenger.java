package com.expedia.bookings.data;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

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

	//This is some stuff in the design that maybe won't exist
	private String mRedressNumber;
	private String mPassportCountry;

	public enum Gender {
		MALE, FEMALE
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
