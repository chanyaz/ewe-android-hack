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

	public static Type gsonListTypeToken = new TypeToken<List<ValidPayment>>() {
	}.getType();

}
