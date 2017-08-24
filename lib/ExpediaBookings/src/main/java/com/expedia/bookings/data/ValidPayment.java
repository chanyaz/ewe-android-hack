package com.expedia.bookings.data;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class ValidPayment {

	public String name;
	protected PaymentType mPaymentType;
	protected Money mFee;

	public void setPaymentType(PaymentType type) {
		mPaymentType = type;
	}

	public PaymentType getPaymentType() {
		return mPaymentType;
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
		if (payment.getPaymentType() == PaymentType.CARD_DISCOVER) {
			ValidPayment googlePayment = new ValidPayment();
			googlePayment.setPaymentType(PaymentType.WALLET_GOOGLE);
			googlePayment.setFee(payment.getFee());
			payments.add(googlePayment);
		}
	}

	public static boolean isPaymentTypeSupported(List<ValidPayment> payments, PaymentType paymentType) {
		if (paymentType != null && payments != null) {
			for (ValidPayment payment : payments) {
				if (payment.getPaymentType() == paymentType) {
					return true;
				}
			}
		}
		return false;
	}

	public static final Type gsonListTypeToken = new TypeToken<List<ValidPayment>>() {
	}.getType();

}
