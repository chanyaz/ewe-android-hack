package com.expedia.bookings.data;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.FileCipher;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class BillingInfo implements JSONable, Comparable<BillingInfo> {

	private static final String SAVED_INFO_FILENAME = "billing.dat";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	private static final String PASSWORD = "7eGeDr4jaD6jut9aha3hAyupAC6ZE9a";

	private String mFirstName;
	private String mLastName;
	private String mNameOnCard;
	private String mTelephoneCountryCode;
	private String mTelephoneCountry;
	private String mTelephone;
	private String mEmail;
	private Location mLocation;
	private String mBrandName;
	private String mBrandCode;
	private String mNumber;
	private String mSecurityCode;
	private Calendar mExpirationDate;
	private StoredCreditCard mStoredCard;
	private boolean mSaveCardToExpediaAccount = false;

	private boolean mExistsOnDisk = false;

	public BillingInfo() {
	}

	//Copy constructor
	public BillingInfo(BillingInfo base) {
		if (base != null) {
			mFirstName = base.getFirstName();
			mLastName = base.getLastName();
			mNameOnCard = base.getNameOnCard();
			mTelephoneCountryCode = base.getTelephoneCountryCode();
			mTelephoneCountry = base.getTelephoneCountry();
			mTelephone = base.getTelephone();
			mEmail = base.getEmail();
			Location loc = new Location();
			if (base.getLocation() != null) {
				loc.fromJson(base.getLocation().toJson());
			}
			mLocation = loc;
			mBrandName = base.getBrandName();
			mBrandCode = base.getBrandCode();
			mNumber = base.getNumber();
			mSecurityCode = base.getSecurityCode();
			mExpirationDate = base.getExpirationDate();
			if (base.getStoredCard() != null) {
				mStoredCard = new StoredCreditCard();
				mStoredCard.fromJson(base.getStoredCard().toJson());
			}
			mSaveCardToExpediaAccount = base.getSaveCardToExpediaAccount();
		}
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

	public String getTelephoneCountryCode() {
		return mTelephoneCountryCode;
	}

	public void setTelephoneCountryCode(String telephoneCountryCode) {
		this.mTelephoneCountryCode = telephoneCountryCode;
	}

	public String getTelephoneCountry() {
		return this.mTelephoneCountry;
	}

	public void setTelephoneCountry(String telephoneCountry) {
		this.mTelephoneCountry = telephoneCountry;
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

	public String getNameOnCard() {
		return mNameOnCard;
	}

	public void setNameOnCard(String name) {
		mNameOnCard = name;
	}

	public void setStoredCard(StoredCreditCard card) {
		mStoredCard = card;
	}

	public StoredCreditCard getStoredCard() {
		return mStoredCard;
	}

	public boolean hasStoredCard() {
		return mStoredCard != null;
	}

	public void setSaveCardToExpediaAccount(boolean save) {
		mSaveCardToExpediaAccount = save;
	}

	public boolean getSaveCardToExpediaAccount() {
		return mSaveCardToExpediaAccount;
	}

	/**
	 * Return the type for the currently active creditcard.
	 * 
	 * If we have a stored credit card, we return the type of that.
	 * If we have a CC number we determine the type from that.
	 * 
	 * @return the CreditCardType this billingInfo encapsulates (or null if it cannot be determined)
	 */
	public CreditCardType getCardType() {
		CreditCardType selectedCardType = null;
		StoredCreditCard scc = getStoredCard();

		if (scc != null) {
			selectedCardType = scc.getType();
		}
		else {
			String number = getNumber();
			if (!TextUtils.isEmpty(number)) {
				selectedCardType = CurrencyUtils.detectCreditCardBrand(number);
			}
		}
		return selectedCardType;
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
		data.remove("number");
		data.remove("securityCode");

		// If we're booking using Google Wallet, we'll have a lot of data just from
		// Wallet but we don't want to save any of it
		StoredCreditCard scc = getStoredCard();
		if (scc != null && scc.isGoogleWallet()) {
			data.remove("nameOnCard");
			data.remove("email");
			data.remove("location");
			data.remove("expMonth");
			data.remove("expYear");
		}

		mExistsOnDisk = true;

		return fileCipher.saveSecureData(context.getFileStreamPath(SAVED_INFO_FILENAME), data.toString());
	}

	public boolean load(Context context) {
		Log.d("Loading saved billing info.");

		// Check that the saved billing info file exists
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (!f.exists()) {
			mExistsOnDisk = false;
			return false;
		}

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);
		if (!fileCipher.isInitialized()) {
			mExistsOnDisk = false;
			return false;
		}

		String results = fileCipher.loadSecureData(f);
		if (results == null || results.length() == 0) {
			mExistsOnDisk = false;
			return false;
		}

		try {
			fromJson(new JSONObject(results));
			mExistsOnDisk = true;
			return true;
		}
		catch (JSONException e) {
			Log.e("Could not restore saved billing info.", e);
			mExistsOnDisk = false;
			return false;
		}
	}

	public boolean doesExistOnDisk() {
		return mExistsOnDisk;
	}

	// Returns true if the file does not exist by the end of the method;
	// If it didn't exist at the beginning, it doesn't matter.
	public boolean delete(Context context) {
		Log.i("Deleting saved billing info.");

		// Reset internal fields
		mFirstName = null;
		mLastName = null;
		mNameOnCard = null;
		mTelephoneCountryCode = null;
		mTelephone = null;
		mEmail = null;
		mLocation = null;
		mBrandName = null;
		mBrandCode = null;
		mNumber = null;
		mSecurityCode = null;
		mExpirationDate = null;
		mExistsOnDisk = false;
		mSaveCardToExpediaAccount = false;

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
			obj.putOpt("nameOnCard", mNameOnCard);
			obj.putOpt("telephoneCountryCode", mTelephoneCountryCode);
			obj.putOpt("telephoneCountry", mTelephoneCountry);
			obj.putOpt("telephone", mTelephone);
			obj.putOpt("email", mEmail);
			JSONUtils.putJSONable(obj, "location", mLocation);
			obj.putOpt("brandName", mBrandName);
			obj.putOpt("brandCode", mBrandCode);
			obj.putOpt("number", mNumber);
			obj.putOpt("securityCode", mSecurityCode);
			obj.putOpt("storeCreditCardInUserProfile", mSaveCardToExpediaAccount);

			if (mExpirationDate != null) {
				obj.putOpt("expMonth", mExpirationDate.get(Calendar.MONTH));
				obj.putOpt("expYear", mExpirationDate.get(Calendar.YEAR));
			}

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
		mNameOnCard = obj.optString("nameOnCard", null);
		mTelephoneCountryCode = obj.optString("telephoneCountryCode", null);
		mTelephoneCountry = obj.optString("telephoneCountry", null);
		if (mTelephoneCountryCode != null) {
			// Blow away the telephone number in case we are upgrading and now ask the user for the country code
			// This handles cases where they put the country code in the phone number to begin with
			mTelephone = obj.optString("telephone", null);
		}
		mEmail = obj.optString("email", null);
		mLocation = (Location) JSONUtils.getJSONable(obj, "location", Location.class);
		mBrandName = obj.optString("brandName", null);
		mBrandCode = obj.optString("brandCode", null);
		mNumber = obj.optString("number", null);
		mSecurityCode = obj.optString("securityCode", null);
		mSaveCardToExpediaAccount = obj.optBoolean("storeCreditCardInUserProfile");

		if (obj.has("expMonth") && obj.has("expYear")) {
			int expMonth = obj.optInt("expMonth");
			int expYear = obj.optInt("expYear");
			mExpirationDate = new GregorianCalendar(expYear, expMonth, 1);
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

	/**
	 * Use the toJson() method for both objects and compare the strings.
	 */
	@Override
	public int compareTo(BillingInfo another) {
		//TODO: We should really improve this compareto method, but it currently meets our needs with very little code
		if (this == another) {
			return 0;
		}
		if (another == null) {
			return -1;
		}
		return toJson().toString().compareTo(another.toJson().toString());

	}
}
