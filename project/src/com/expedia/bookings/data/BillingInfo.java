package com.expedia.bookings.data;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.FileCipher;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class BillingInfo implements JSONable {

	private static final String SAVED_INFO_FILENAME = "billing.dat";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	private static final String PASSWORD = "7eGeDr4jaD6jut9aha3hAyupAC6ZE9a";

	private String mFirstName;
	private String mLastName;
	private String mTelephone;
	private String mEmail;
	private Location mLocation;
	private String mBrandName;
	private String mBrandCode;
	private String mNumber;
	private String mSecurityCode;
	private Calendar mExpirationDate;

	private boolean mIsSavedInfo;

	public BillingInfo() {
		mIsSavedInfo = false;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String firstName) {
		this.mFirstName = firstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String lastName) {
		this.mLastName = lastName;
	}

	public String getTelephone() {
		return mTelephone;
	}

	public void setTelephone(String telephone) {
		this.mTelephone = telephone;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		this.mLocation = location;
	}

	public String getBrandName() {
		return mBrandName;
	}

	public void setBrandName(String brandName) {
		this.mBrandName = brandName;
	}

	public String getBrandCode() {
		return mBrandCode;
	}

	public void setBrandCode(String brandCode) {
		this.mBrandCode = brandCode;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		this.mNumber = number;
	}

	public String getSecurityCode() {
		return mSecurityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.mSecurityCode = securityCode;
	}

	public Calendar getExpirationDate() {
		return mExpirationDate;
	}

	public void setExpirationDate(Calendar expirationDate) {
		this.mExpirationDate = expirationDate;
	}

	public boolean isSavedInfo() {
		return mIsSavedInfo;
	}

	public void setIsSavedInfo(boolean isSavedInfo) {
		this.mIsSavedInfo = isSavedInfo;
	}

	public boolean save(Context context) {
		Log.d("Saving user's billing info.");

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);

		if (!fileCipher.isInitialized()) {
			return false;
		}

		JSONObject data = toJson();

		// Remove sensitive data
		data.remove("brandName");
		data.remove("brandCode");
		data.remove("number");
		data.remove("securityCode");

		return fileCipher.saveSecureData(context.getFileStreamPath(SAVED_INFO_FILENAME), data.toString());
	}

	public boolean load(Context context) {
		Log.d("Loading saved billing info.");

		// Check that the saved billing info file exists
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (!f.exists()) {
			return false;
		}

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);
		if (!fileCipher.isInitialized()) {
			return false;
		}

		String results = fileCipher.loadSecureData(f);
		if (results == null || results.length() == 0) {
			return false;
		}

		try {
			fromJson(new JSONObject(results));
			return true;
		}
		catch (JSONException e) {
			Log.e("Could not restore saved billing info.", e);
			return false;
		}
	}

	// Returns true if the file does not exist by the end of the method;
	// If it didn't exist at the beginning, it doesn't matter.
	public boolean delete(Context context) {
		Log.i("Deleting saved billing info.");

		// Reset internal fields
		mFirstName = null;
		mLastName = null;
		mTelephone = null;
		mEmail = null;
		mLocation = null;
		mBrandName = null;
		mBrandCode = null;
		mNumber = null;
		mSecurityCode = null;
		mExpirationDate = null;

		// Check that the saved billing info file exists before trying to delete
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (!f.exists()) {
			return true;
		}

		return f.delete();
	}

	public static boolean hasSavedBillingInfo(Context context) {
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		return f.exists();
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("lastName", mLastName);
			obj.putOpt("telephone", mTelephone);
			obj.putOpt("email", mEmail);
			JSONUtils.putJSONable(obj, "location", mLocation);
			obj.putOpt("brandName", mBrandName);
			obj.putOpt("brandCode", mBrandCode);
			obj.putOpt("number", mNumber);
			obj.putOpt("securityCode", mSecurityCode);

			if (mExpirationDate != null) {
				obj.putOpt("expMonth", mExpirationDate.get(Calendar.MONTH));
				obj.putOpt("expYear", mExpirationDate.get(Calendar.YEAR));
			}

			obj.put("isSavedInfo", mIsSavedInfo);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert BillingInfo object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mFirstName = obj.optString("firstName", null);
		mLastName = obj.optString("lastName", null);
		mTelephone = obj.optString("telephone", null);
		mEmail = obj.optString("email", null);
		mLocation = (Location) JSONUtils.getJSONable(obj, "location", Location.class);
		mBrandName = obj.optString("brandName", null);
		mBrandCode = obj.optString("brandCode", null);
		mNumber = obj.optString("number", null);
		mSecurityCode = obj.optString("securityCode", null);

		if (obj.has("expMonth") && obj.has("expYear")) {
			int expMonth = obj.optInt("expMonth");
			int expYear = obj.optInt("expYear");
			mExpirationDate = new GregorianCalendar(expYear, expMonth, 1);
		}

		mIsSavedInfo = obj.optBoolean("isSavedInfo", false);

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

	// **WARNING: USE FOR TESTING PURPOSES ONLY**
	public void fillWithTestData() throws JSONException {
		String data = "{\"expMonth\":11,\"lastName\":\"Test\",\"expYear\":2018,\"brandCode\":\"VI\",\"location\":{\"countryCode\":\"US\",\"streetAddress\":[\"travelnow\"],\"stateCode\":\"MN\",\"longitude\":0,\"latitude\":0,\"postalCode\":\"55408\",\"city\":\"Minneapolis\"},\"email\":\"dan@mobiata.com\",\"number\":\"4005550000000019\",\"isSavedInfo\":false,\"firstName\":\"Test\",\"telephone\":\"6122345680\"}";
		JSONObject obj = new JSONObject(data);
		fromJson(obj);
	}
}
