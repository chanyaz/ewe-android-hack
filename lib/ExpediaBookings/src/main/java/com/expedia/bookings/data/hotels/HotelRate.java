package com.expedia.bookings.data.hotels;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.Strings;

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
	public List<NightlyRatesPerRoom> nightlyRatesPerRoom;
	public String depositAmountToShowUsers;
	public String depositAmount;

	public transient float packagePricePerPerson;

	// The types of display rates
	public enum UserPriceType {
		RATE_FOR_WHOLE_STAY_WITH_TAXES,
		PER_NIGHT_RATE_NO_TAXES,
		PACKAGES,
		UNKNOWN;

		public static UserPriceType toEnum(String value) {
			if ("RateForWholeStayWithTaxes".equals(value)) {
				return UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES;
			}
			else if ("PerNightRateNoTaxes".equals(value)) {
				return UserPriceType.PER_NIGHT_RATE_NO_TAXES;
			}
			else if (Constants.PACKAGE_HOTEL_DELTA_PRICE_TYPE.equals(value)) {
				return UserPriceType.PACKAGES;
			}

			return UserPriceType.UNKNOWN;
		}
	}

	public UserPriceType getUserPriceType() {
		return UserPriceType.toEnum(userPriceType);
	}

	public static class NightlyRatesPerRoom {
		public boolean promo;
		public String baseRate;
		public String rate;
	}

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
		if (surchargesForEntireStay != null) {
			for (SurchargesForEntireStay charge : surchargesForEntireStay) {
				if (charge.type.equals("EXTRA")) {
					surcharges = new Money(charge.amount, currencyCode);
					break;
				}
			}
		}
		return surcharges;
	}

	public Money getDisplayTotalPrice() {
		if (Strings.equals(checkoutPriceType, "totalPriceWithMandatoryFees")) {
			return new Money(new BigDecimal(totalPriceWithMandatoryFees), currencyCode);
		}
		else {
			return new Money(new BigDecimal(total), currencyCode);
		}
	}

	public boolean isDiscountTenPercentOrBetter() {
		return discountPercent <= -10;
	}
}
