package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.utils.FeatureUtilKt;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public abstract class TripBucketItem implements JSONable {

	public abstract LineOfBusiness getLineOfBusiness();

	private TripBucketItemState mState;

	private boolean mHasPriceChanged;

	private List<ValidPayment> mValidPayments;

	// Boolean check to indicate if this bucket item is actively being viewed/selected.
	private boolean mIsSelected;

	boolean mIsMerEmailOptIn;
	boolean mIsMerEmailOptInShownOnce;

	public boolean hasPriceChanged() {
		return mHasPriceChanged;
	}

	public void setHasPriceChanged(boolean priceChanged) {
		this.mHasPriceChanged = priceChanged;
	}

	public TripBucketItemState getState() {
		if (mState == null) {
			return TripBucketItemState.DEFAULT;
		}
		else {
			return mState;
		}
	}

	public void setState(TripBucketItemState state) {
		if (state == TripBucketItemState.EXPANDED
			|| state == TripBucketItemState.SHOWING_CHECKOUT_BUTTON
			|| state == TripBucketItemState.SHOWING_PRICE_CHANGE
			|| state == TripBucketItemState.CONFIRMATION) {
			throw new RuntimeException("Cannot set this state to the data model: " + state.name());
		}
		mState = state;
	}

	public boolean isSelected() {
		return mIsSelected;
	}

	public void setSelected(boolean isSelected) {
		this.mIsSelected = isSelected;
	}

	public boolean canBePurchased() {
		return mState != TripBucketItemState.PURCHASED && mState != TripBucketItemState.BOOKING_UNAVAILABLE && mState != TripBucketItemState.EXPIRED;
	}

	public boolean hasBeenPurchased() {
		return mState == TripBucketItemState.PURCHASED;
	}

	public boolean isMerEmailOptIn() {
		return mIsMerEmailOptIn;
	}

	public void setIsMerEmailOptIn(boolean optIn) {
		this.mIsMerEmailOptIn = optIn;
	}

	public boolean isMerEmailOptInShownOnce() {
		return mIsMerEmailOptInShownOnce;
	}

	public void setIsMerEmailOptInShownOnce(boolean shown) {
		this.mIsMerEmailOptInShownOnce = shown;
	}

	public void addValidPaymentsV2(List<? extends ValidFormOfPayment> payments) {
		if (mValidPayments == null) {
			mValidPayments = new ArrayList<>();
		}

		if (payments != null) {
			for (ValidFormOfPayment payment : payments) {
				ValidPayment oldPayment = ValidFormOfPaymentUtils.createFromValidFormOfPayment(payment);
				ValidPayment.addValidPayment(mValidPayments, oldPayment);
			}
		}
	}

	public void addValidPayments(List<ValidPayment> payments) {
		if (mValidPayments == null) {
			mValidPayments = new ArrayList<>();
		}

		if (payments != null) {
			for (ValidPayment payment : payments) {
				ValidPayment.addValidPayment(mValidPayments, payment);
			}
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

	/**
	 * This method calculates the card fee based upon looking at the ValidPayments (and their associated fees) and also
	 * the selected card from the given BillingInfo.
	 *
	 * @param billingInfo
	 * @return cardFee as Money or null if no card fee
	 */
	public Money getPaymentFee(BillingInfo billingInfo, Context context) {
		if (billingInfo == null || billingInfo.getPaymentType(context) == null) {
			return null;
		}
		return getPaymentFee(billingInfo.getPaymentType(context));
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
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putEnum(obj, "state", mState);
			obj.put("isSelected", mIsSelected);
			obj.put("hasPriceChanged", mHasPriceChanged);
			obj.put("isMerEmailOptIn", mIsMerEmailOptIn);

			GsonUtil.putListForJsonable(obj, "validPayments", mValidPayments);

			return obj;
		}
		catch (JSONException e) {
			Log.e("TripBucketItem toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mState = JSONUtils.getEnum(obj, "state", TripBucketItemState.class);
		mIsSelected = obj.optBoolean("isSelected");
		mIsMerEmailOptIn = obj.optBoolean("isMerEmailOptIn");
		mHasPriceChanged = obj.optBoolean("hasPriceChanged");
		mValidPayments = GsonUtil.getListForJsonable(obj, "validPayments", ValidPayment.gsonListTypeToken);
		return true;
	}

}
