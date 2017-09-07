package com.expedia.bookings.data;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class BillingInfo implements JSONable, Comparable<BillingInfo> {

	private String mFirstName;
	private String mLastName;
	private String mNameOnCard;
	private String mTelephoneCountryCode;
	private String mTelephone;
	private String mEmail;
	private Location mLocation;
	private String mBrandName;
	private String mBrandCode;
	private String mNumber;
	private String mSecurityCode;
	private LocalDate mExpirationDate;
	private StoredCreditCard mStoredCard;
	private boolean mSaveCardToExpediaAccount = false;
	private boolean mIsTempCard = false;

	public BillingInfo() {
		// default
	}

	//Copy constructor
	public BillingInfo(BillingInfo base) {
		if (base != null) {
			mFirstName = base.getFirstName();
			mLastName = base.getLastName();
			mNameOnCard = base.getNameOnCard();
			mTelephoneCountryCode = base.getTelephoneCountryCode();
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
			if (base.hasStoredCard()) {
				mStoredCard = new StoredCreditCard();
				mStoredCard.fromJson(base.getStoredCard().toJson());
			}
			mSaveCardToExpediaAccount = base.getSaveCardToExpediaAccount();
			mIsTempCard = base.mIsTempCard;
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

	public void setNumberAndDetectType(String number, Context context) {
		setNumber(number);

		PaymentType type = CurrencyUtils.detectCreditCardBrand(getNumber(), context);
		setBrandCode(type.getCode());
		setBrandName(type.name());
	}

	public String getSecurityCode() {
		return mSecurityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.mSecurityCode = securityCode;
	}

	public LocalDate getExpirationDate() {
		return mExpirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
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

	public boolean isTempCard() {
		return mIsTempCard;
	}

	public void setIsTempCard(boolean isTempCard) {
		mIsTempCard = isTempCard;
	}

	public boolean isUsingGoogleWallet() {
		return mStoredCard != null && mStoredCard.isGoogleWallet();
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
	 * @return the PaymentType this billingInfo encapsulates (or null if it cannot be determined)
	 */
	public PaymentType getPaymentType(Context context) {
		PaymentType selectedPaymentType = null;
		StoredCreditCard scc = getStoredCard();

		if (scc != null) {
			selectedPaymentType = scc.getType();
		}
		else {
			String number = getNumber();
			selectedPaymentType = CurrencyUtils.detectCreditCardBrand(number, context);
		}
		return selectedPaymentType;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("lastName", mLastName);
			obj.putOpt("nameOnCard", mNameOnCard);
			obj.putOpt("telephoneCountryCode", mTelephoneCountryCode);
			obj.putOpt("telephone", mTelephone);
			obj.putOpt("email", mEmail);
			JSONUtils.putJSONable(obj, "location", mLocation);
			obj.putOpt("brandName", mBrandName);
			obj.putOpt("brandCode", mBrandCode);
			obj.putOpt("number", mNumber);
			obj.putOpt("securityCode", mSecurityCode);
			obj.putOpt("storeCreditCardInUserProfile", mSaveCardToExpediaAccount);

			if (mExpirationDate != null) {
				obj.putOpt("expirationDate", mExpirationDate.toString());
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
		if (mTelephoneCountryCode != null) {
			// Blow away the telephone number in case we are upgrading and now ask the user for the country code
			// This handles cases where they put the country code in the phone number to begin with
			mTelephone = obj.optString("telephone", null);
		}
		mEmail = obj.optString("email", null);
		mLocation = JSONUtils.getJSONable(obj, "location", Location.class);
		mBrandName = obj.optString("brandName", null);
		mBrandCode = obj.optString("brandCode", null);
		mNumber = obj.optString("number", null);
		mSecurityCode = obj.optString("securityCode", null);
		mSaveCardToExpediaAccount = obj.optBoolean("storeCreditCardInUserProfile");

		if (obj.has("expMonth") && obj.has("expYear")) {
			int expMonth = obj.optInt("expMonth");
			int expYear = obj.optInt("expYear");
			mExpirationDate = new LocalDate(expYear, expMonth + 1, 1);
		}
		else if (obj.has("expirationDate")) {
			mExpirationDate = LocalDate.parse(obj.optString("expirationDate"));
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

	public boolean isCreditCardDataEnteredManually() {
		if (getLocation() == null) {
			return false;
		}
		//Checkout the major fields, if any of them have data, then we know some data has been manually entered
		return !Strings.isEmpty(getLocation().getStreetAddressString()) || !Strings.isEmpty(getLocation().getCity())
			|| !Strings.isEmpty(getLocation().getPostalCode()) || !Strings.isEmpty(getLocation().getStateCode())
			|| !Strings.isEmpty(getNameOnCard()) || !Strings.isEmpty(getNumber());
	}

}
