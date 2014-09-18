package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class ValidPayment implements JSONable {

	private CreditCardType mCreditCardType;
	private Money mFee;

	public void setCreditCardType(CreditCardType type) {
		mCreditCardType = type;
	}

	public CreditCardType getCreditCardType() {
		return mCreditCardType;
	}

	public Money getFee() {
		return mFee;
	}

	public void setFee(Money fee) {
		mFee = fee;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject json = new JSONObject();
			JSONUtils.putEnum(json, "name", mCreditCardType);
			JSONUtils.putJSONable(json, "fee", mFee);
			return json;
		}
		catch (JSONException e) {
			Log.e("Could not convert ValidPayment object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCreditCardType = JSONUtils.getEnum(obj, "name", CreditCardType.class);
		mFee = JSONUtils.getJSONable(obj, "fee", Money.class);
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility methods

	public static void addValidPayment(List<ValidPayment> payments, ValidPayment payment) {
		if (payments == null) {
			throw new IllegalArgumentException("payments can not be null");
		}
		if (payment == null) {
			throw new IllegalArgumentException("payment can not be null");
		}

		payments.add(payment);

		// #1363: Duplicate Mastercard valid payment types as a Google Wallet card as well,
		// since Google Wallet uses Mastercard on the back end.
		// #3014: Google Wallet now uses Discover cards
		if (payment.getCreditCardType() == CreditCardType.DISCOVER) {
			ValidPayment googlePayment = new ValidPayment();
			googlePayment.setCreditCardType(CreditCardType.GOOGLE_WALLET);
			googlePayment.setFee(payment.getFee());
			payments.add(googlePayment);
		}
	}

	public static boolean isCardTypeSupported(List<ValidPayment> payments, CreditCardType creditCardType) {
		if (creditCardType != null && payments != null) {
			for (ValidPayment payment : payments) {
				if (payment.getCreditCardType() == creditCardType) {
					return true;
				}
			}
		}
		return false;
	}
}
