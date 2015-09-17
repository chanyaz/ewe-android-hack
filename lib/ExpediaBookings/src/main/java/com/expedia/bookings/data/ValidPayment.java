package com.expedia.bookings.data;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class ValidPayment {

	public String name;
	protected CreditCardType mCreditCardType;
	protected Money mFee;

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

	public static Type gsonListTypeToken = new TypeToken<List<ValidPayment>>() {
	}.getType();

}
