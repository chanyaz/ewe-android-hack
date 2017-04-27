package com.expedia.bookings.data.hotels;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.payment.LoyaltyInformation;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.Strings;

public class HotelRate {
	public float maxNightlyRate;
	public float averageRate;
	public String taxStatusType;
	public float surchargeTotal;
	public float surchargeTotalForEntireStay;
	public float surchargesWithoutPropertyFeeForEntireStay;
	public float averageBaseRate;
	public float nightlyRateTotal;
	public float discountPercent;
	public float total;
	public String currencyCode;
	public String currencySymbol;
	public String discountMessage;
	public float priceToShowUsers;
	public float strikethroughPriceToShowUsers;
	public float dailyMandatoryFee;
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
	public LoyaltyInformation loyaltyInfo;

	public Money packagePricePerPerson;
	public Money packageTotalPrice;
	public Money packageSavings;

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

	public Money getPropertyServiceFees() {
		Money surcharges = null;
		if (surchargesForEntireStay != null) {
			for (SurchargesForEntireStay charge : surchargesForEntireStay) {
				if (charge.type.equals("FEE_SERVICE")) {
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

	public Money getDisplayMoney(boolean strikeThrough, boolean shouldFallbackToZeroIfNegative) {
		// In case of shop with points, price to show users can be negative. We need to show 0 in such cases.
		float price = shouldFallbackToZeroIfNegative ? getPriceToShowUsersFallbackToZeroIfNegative() : priceToShowUsers;
		Money money = strikeThrough ? new Money(Float.toString(strikethroughPriceToShowUsers), currencyCode)
			: new Money(Float.toString(price), currencyCode);
		return money;
	}

	public float getPriceToShowUsersFallbackToZeroIfNegative() {
		return (priceToShowUsers < 0) ? 0 : priceToShowUsers;
	}

	public boolean isDiscountPercentNotZero() {
		return discountPercent != 0;
	}
}
