package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.utils.FeatureUtilKt;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.json.JSONable;

public abstract class TripBucketItem implements JSONable {

	public abstract LineOfBusiness getLineOfBusiness();

	private List<ValidPayment> mValidPayments = new ArrayList<>();

	protected void addValidPaymentsV2(List<? extends ValidFormOfPayment> payments) {
		if (payments != null) {
			for (ValidFormOfPayment payment : payments) {
				addValidPayment(ValidFormOfPaymentUtils.createValidPaymentFromValidFormOfPayment(payment));
			}
		}
	}

	void addValidPayments(List<ValidPayment> payments) {
		if (payments != null) {
			for (ValidPayment payment : payments) {
				addValidPayment(payment);
			}
		}
	}

	private void addValidPayment(ValidPayment payment) {
		if (payment != null) {
			mValidPayments.add(payment);
			addGoogleWalletIfSupported(payment);
		}
	}

	private void addGoogleWalletIfSupported(ValidPayment payment) {
		if (payment.getPaymentType() == PaymentType.CARD_DISCOVER) {
			ValidPayment googlePayment = new ValidPayment();
			googlePayment.setPaymentType(PaymentType.WALLET_GOOGLE);
			googlePayment.setFee(payment.getFee());
			mValidPayments.add(googlePayment);
		}
	}


	/**
	 * Is the supplied card type valid for this FlightTrip?
	 *
	 * @param paymentType
	 * @return true if this FlightTrip supports the card type, false otherswise.
	 */
	public boolean isPaymentTypeSupported(PaymentType paymentType, Context context) {
		if (FeatureUtilKt.isAllowUnknownCardTypesEnabled(context) && paymentType == PaymentType.CARD_UNKNOWN) {
			return true;
		}
		return ValidPayment.isPaymentTypeSupported(mValidPayments, paymentType);
	}

	public Money getPaymentFee(PaymentType paymentType) {
		if (paymentType != null && mValidPayments != null) {
			for (ValidPayment payment : mValidPayments) {
				if (payment.getPaymentType() == paymentType) {
					return payment.getFee();
				}
			}
		}
		return null;
	}

	public boolean hasPaymentFee(PaymentType paymentType) {
		Money paymentFee = getPaymentFee(paymentType);
		return paymentFee != null && !paymentFee.isZero();
	}

	//////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		GsonUtil.putListForJsonable(obj, "validPayments", mValidPayments);
		return obj;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mValidPayments = GsonUtil.getListForJsonable(obj, "validPayments", ValidPayment.gsonListTypeToken);
		return true;
	}

}
