package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class StoredCreditCard implements JSONable, Comparable<StoredCreditCard> {

	private PaymentType mType;
	private String mDescription;
	private String mRemoteId;
	private String mCardNumber;
	private String mNameOnCard;
	private boolean expired;
	private boolean mIsGoogleWallet;

	// (Tablet Checkout) When user is logged in, can this credit card be selected from the list of stored credit cards or disabled?
	private boolean mIsSelectable = true;

	public void setType(PaymentType type) {
		mType = type;
	}

	public PaymentType getType() {
		if (isGoogleWallet()) {
			return PaymentType.WALLET_GOOGLE;
		}

		return mType;
	}

	public void setId(String id) {
		mRemoteId = id;
	}

	public String getId() {
		return mRemoteId;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setCardNumber(String number) {
		mCardNumber = number;
	}

	public void setNameOnCard(String name) {
		mNameOnCard = name;
	}

	public String getNameOnCard() {
		return mNameOnCard;
	}


	public void setExpired(boolean isExpired) {
		expired = isExpired;
	}

	public boolean isExpired() {
		return expired;
	}

	/**
	 * TODO: this will always return null until the API gives us this information.
	 * @return
	 */
	public String getCardNumber() {
		return mCardNumber;
	}

	public void setIsGoogleWallet(boolean isGoogleWallet) {
		mIsGoogleWallet = isGoogleWallet;
	}

	public boolean isGoogleWallet() {
		return mIsGoogleWallet;
	}

	public boolean isSelectable() {
		return mIsSelectable;
	}

	public void setIsSelectable(boolean isSelectable) {
		mIsSelectable = isSelectable;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			JSONUtils.putEnum(obj, "creditCardType", mType);
			obj.putOpt("description", mDescription);
			obj.putOpt("paymentsInstrumentsId", mRemoteId);
			obj.putOpt("isGoogleWallet", mIsGoogleWallet);
			obj.putOpt("cardNumber", mCardNumber);
			obj.putOpt("isSelectable", mIsSelectable);
			obj.putOpt("nameOnCard", mNameOnCard);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert StoredCreditCard to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mType = JSONUtils.getEnum(obj, "creditCardType", PaymentType.class);
		mDescription = obj.optString("description", null);
		mRemoteId = obj.optString("paymentsInstrumentsId", null);
		mIsGoogleWallet = obj.optBoolean("isGoogleWallet");
		mCardNumber = obj.optString("cardNumber");
		mIsSelectable = obj.optBoolean("isSelectable");
		mNameOnCard = obj.optString("nameOnCard");
		return true;
	}

	private static final int BEFORE = -1;
	private static final int EQUAL = 0;

	@Override
	public int compareTo(StoredCreditCard another) {
		if (this == another) {
			//same ref
			return EQUAL;
		}
		if (another == null) {
			return BEFORE;
		}

		int diff = 0;

		// Compare card number
		diff = Strings.compareTo(getCardNumber(), another.getCardNumber());
		if (diff != 0) {
			return diff;
		}

		// Compare card type
		if (another.getType() != null && getType() != null) {
			diff = another.getType().compareTo(getType());
			if (diff != 0) {
				return diff;
			}
		}

		// Compare remoteID
		if (!TextUtils.isEmpty(another.getId()) && !TextUtils.isEmpty(getId())) {
			diff = Strings.compareTo(getId(), another.getId());
			if (diff != 0) {
				return diff;
			}
		}

		// Compare description
		if (!TextUtils.isEmpty(another.getDescription()) && !TextUtils.isEmpty(getDescription())) {
			diff = Strings.compareTo(getDescription(), another.getDescription());
			if (diff != 0) {
				return diff;
			}
		}

		if (!TextUtils.isEmpty(another.getNameOnCard()) && !TextUtils.isEmpty(getNameOnCard())) {
			diff = Strings.compareTo(getNameOnCard(), another.getNameOnCard());
			if (diff != 0) {
				return diff;
			}
		}

		return EQUAL;
	}
}
