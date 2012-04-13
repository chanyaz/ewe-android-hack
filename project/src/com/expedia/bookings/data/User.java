package com.expedia.bookings.data;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class User implements JSONable {

	private String mEmail;

	private String mFirstName;
	private String mMiddleName;
	private String mLastName;

	private ArrayList<Phone> mPhoneNumbers;
	private Address mHomeAddress;
	private ArrayList<StoredCreditCard> mStoredCreditCards;

	private String mLoyaltyMembershipNumber;
	private boolean mIsSmokingPreferred;

	public void setEmail(String email) {
		mEmail = email;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public String getLastName() {
		return mLastName;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("email", mEmail);
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "storedCreditCards", mStoredCreditCards);

			obj.putOpt("loyaltyMembershipNumber", mLoyaltyMembershipNumber);
			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

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

		JSONArray phoneNumbers = obj.optJSONArray("phoneNumbers");
		if (phoneNumbers != null) {
			mPhoneNumbers = new ArrayList<Phone>();
			for (int i = 0; i < phoneNumbers.length(); i++) {
				try {
					Phone p = new Phone(phoneNumbers.getJSONObject(i));
					mPhoneNumbers.add(p);
				}
				catch (JSONException e) {
					Log.e("Could not get phone number at i="+i+":", e);
				}
			}
		}

		JSONArray creditCards = obj.optJSONArray("storedCreditCards");
		if (creditCards != null) {
			mStoredCreditCards = new ArrayList<StoredCreditCard>();
			for (int i = 0; i < creditCards.length(); i++) {
				try {
					StoredCreditCard c = new StoredCreditCard(creditCards.getJSONObject(i));
					mStoredCreditCards.add(c);
				}
				catch (JSONException e) {
					Log.e("Could not get stored credit card at i="+i+":", e);
				}
			}
		}

		JSONObject addr = obj.optJSONObject("homeAddress");
		if (addr != null) {
			mHomeAddress = new Address(addr);
		}
		// TODO - frequent guest memberships
		mLoyaltyMembershipNumber = obj.optString("loyaltyMembershipNumber", null);
		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred", false);
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
