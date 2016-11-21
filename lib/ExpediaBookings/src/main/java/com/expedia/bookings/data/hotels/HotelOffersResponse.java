package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.packages.PackageOffersResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.payment.LoyaltyInformation;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.Strings;

public class HotelOffersResponse extends BaseApiResponse {

	public String airAttachExpirationTimeSeconds;
	public String checkInDate;
	public String checkOutDate;
	public boolean deskTopOverrideNumber;
	public String firstHotelOverview;
	public String hotelAddress;
	public List<HotelAmenities> hotelAmenities;
	public String hotelCity;
	public String hotelStateProvince;
	public String hotelCountry;
	public double hotelGuestRating;
	public String hotelId;
	public String hotelName;
	public List<HotelRoomResponse> hotelRoomResponse;
	public List<HotelText> hotelOverviewText;
	public HotelText hotelAmenitiesText;
	public HotelText hotelPoliciesText;
	public HotelText hotelFeesText;
	public HotelText hotelMandatoryFeesText;

	public double latitude;
	public double longitude;
	public String locationId;

	public double hotelStarRating;

	public int totalReviews;
	public int totalRecommendations;
	public String telesalesNumber;
	public boolean isVipAccess;

	public List<Photos> photos;
	public HotelText hotelRenovationText;

	public boolean isPackage;
	public boolean doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo;

	public static class HotelAmenities {
		public String id;
		public String description;
	}

	public static class HotelText {
		public String content;
		public String name;
	}

	public static class HotelRoomResponse {
		@Nullable
		public List<BedTypes> bedTypes;
		public String cancellationPolicy;
		public String currentAllotment;
		public boolean depositRequired;
		public boolean hasFreeCancellation;
		public String freeCancellationWindowDate;
		public HotelRoomResponse payLaterOffer;
		public boolean immediateChargeRequired;
		public boolean nonRefundable;
		public String productKey;
		public boolean rateChange;
		public String rateDescription;
		public RateInfo rateInfo;
		public String roomTypeDescription;
		public String roomLongDescription;
		public String roomThumbnailUrl;
		public List<ValueAdds> valueAdds;
		public String supplierType;
		public boolean isDiscountRestrictedToCurrentSourceType;
		public boolean isSameDayDRR;
		public boolean isPayLater;
		public List<String> depositPolicy;
		public boolean isMemberDeal;
		public String ratePlanCode;
		public String roomTypeCode;
		public String promoDescription;

		public Money packageHotelDeltaPrice;
		public LoyaltyInformation packageLoyaltyInformation;

		public String depositPolicyAtIndex(int index) {
			String policy = "";
			if (depositPolicy != null && index < depositPolicy.size()) {
				policy = depositPolicy.get(index);
			}
			return policy;
		}

		public boolean isMerchant() {
			return Strings.equals(supplierType, "E") || Strings.equals(supplierType, "MERCHANT");
		}

		public String getFormattedBedNames() {
			ArrayList<String> bedNames = new ArrayList<String>();

			if (bedTypes != null) {
				for (BedTypes bed : bedTypes) {
					bedNames.add(bed.description);
				}
			}
			return Strings.joinWithoutEmpties(", ", bedNames);
		}

		public boolean isPackage() {
			return packageHotelDeltaPrice != null;
		}
	}

	public static class BedTypes {
		public String id;
		public String description;
	}

	public static class RateInfo {
		public HotelRate chargeableRateInfo;
		public String description;
	}

	public static class Photos {
		public String displayText;
		public String url;
	}

	public static class ValueAdds {
		public String description;
	}

	public static HotelOffersResponse convertToHotelOffersResponse(HotelOffersResponse hotelOffer, PackageOffersResponse packageOffer, PackageSearchParams searchParams) {
		hotelOffer.checkInDate = searchParams.getStartDate().toString();
		hotelOffer.checkOutDate = searchParams.getEndDate().toString();
		hotelOffer.hotelRoomResponse = new ArrayList<>();
		for (PackageOffersResponse.PackageHotelOffer packageHotelOffer : packageOffer.packageHotelOffers) {
			packageHotelOffer.hotelOffer.productKey = packageHotelOffer.packageProductId;
			if (packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo == null) {
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo = new HotelRate();
			}
			if (packageHotelOffer.priceDifferencePerNight != null) {
				packageHotelOffer.hotelOffer.packageHotelDeltaPrice = packageHotelOffer.priceDifferencePerNight;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.priceToShowUsers = packageHotelOffer.hotelOffer.packageHotelDeltaPrice.amount.floatValue();
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.currencyCode = packageHotelOffer.hotelOffer.packageHotelDeltaPrice.currencyCode;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 0;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.userPriceType = Constants.PACKAGE_HOTEL_DELTA_PRICE_TYPE;
			}
			if (packageHotelOffer.packagePricing.hotelPricing != null) {
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.showResortFeeMessage = true;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.totalMandatoryFees = packageHotelOffer.packagePricing.hotelPricing.mandatoryFees.feeTotal.amount.floatValue();
			}
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packagePricePerPerson = packageHotelOffer.pricePerPerson;
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packageSavings = packageHotelOffer.packagePricing.savings;
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packageTotalPrice = packageHotelOffer.packagePricing.packageTotal;
			packageHotelOffer.hotelOffer.hasFreeCancellation = packageHotelOffer.cancellationPolicy.hasFreeCancellation;
			if (packageHotelOffer.loyaltyInfo != null) {
				packageHotelOffer.hotelOffer.packageLoyaltyInformation = packageHotelOffer.loyaltyInfo;
			}

			hotelOffer.hotelRoomResponse.add(packageHotelOffer.hotelOffer);
		}
		hotelOffer.isPackage = true;
		return hotelOffer;
	}
}
