package com.expedia.bookings.data.hotels;

import java.util.List;

import com.expedia.bookings.data.Money;

public class HotelRate {
	public float maxNightlyRate;
	public float averageRate;
	public String taxStatusType;
	public float surchargeTotal;
	public float surchargeTotalForEntireStay;
	public float averageBaseRate;
	public float nightlyRateTotal;
	public float discountPercent;
	public float total;
	public String currencyCode;
	public String currencySymbol;
	public String discountMessage;
	public float priceToShowUsers;
	public float strikethroughPriceToShowUsers;
	public float totalMandatoryFees;
	public float totalPriceWithMandatoryFees;
	public String userPriceType;
	public String checkoutPriceType;
	public boolean airAttached;
	public String roomTypeCode;
	public String ratePlanCode;
	public boolean showResortFeeMessage;
	public boolean resortFeeInclusion;
	public List<PriceAdjustments> priceAdjustments;
	public List<SurchargesForEntireStay> surchargesForEntireStay;

	public static class PriceAdjustments {
		public String amount;
	}

	public static class SurchargesForEntireStay {
		public String type;
		public String amount;
	}

	public Money getPriceAdjustments() {
		Money totalAdjustments = new Money();
		totalAdjustments.setAmount("0");
		for (PriceAdjustments adj : priceAdjustments) {
			totalAdjustments.add(new Money(adj.amount, currencyCode));
		}
		return totalAdjustments;
	}

	public Money getExtraGuestFees() {
		Money surcharges = null;
		for (SurchargesForEntireStay charge : surchargesForEntireStay) {
			if (charge.type.equals("EXTRA")) {
				surcharges = new Money(charge.amount, currencyCode);
				break;
			}
		}
		return surcharges;
	}

	public String depositAmountToShowUsers;
}
