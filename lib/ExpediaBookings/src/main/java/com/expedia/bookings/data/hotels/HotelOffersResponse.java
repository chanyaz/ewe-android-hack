package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.multiitem.Amenity;
import com.expedia.bookings.data.multiitem.HotelOffer;
import com.expedia.bookings.data.multiitem.MultiItemOffer;
import com.expedia.bookings.data.packages.PackageOffersResponse;
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
	public String packageTelesalesNumber;
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
		@Nullable public String roomThumbnailUrl;
		@Nullable public List<String> roomThumbnailUrlArray;
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

		public String getRoomTypeDescriptionWithoutDetail() {
			if (roomTypeDescription == null) {
				return "";
			}
			String detailSeparator = " - ";
			String detailString = roomTypeDescription;
			int separatorIndex = detailString.indexOf(detailSeparator);
			if (separatorIndex != -1) {
				return detailString.substring(0, separatorIndex);
			}
			return roomTypeDescription;
		}

		public String getRoomTypeDescriptionDetail() {
			if (roomTypeDescription == null) {
				return "";
			}
			String detailSeparator = " - ";
			String detailString = roomTypeDescription;
			int separatorIndex = detailString.indexOf(detailSeparator);
			if (separatorIndex != -1) {
				return detailString.substring(separatorIndex + detailSeparator.length(), detailString.length());
			}
			return "";
		}

		public String roomGroupingKey() {
			if (roomTypeCode != null) {
				return roomTypeCode;
			}
			return productKey;
		}
	}

	public static class BedTypes {
		public String id;
		public String description;
	}

	public static List<BedTypes> convertMultiItemBedType(List<Amenity> amenities) {
		List<BedTypes> allBedTypes = new ArrayList<>();
		for (Amenity amenity : amenities) {
			BedTypes bedTypes = new BedTypes();
			bedTypes.id = String.valueOf(amenity.getId());
			bedTypes.description = amenity.getName();
			allBedTypes.add(bedTypes);
		}
		return allBedTypes;
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
		public String id;
		public String description;
	}

	public static HotelOffersResponse convertToHotelOffersResponse(HotelOffersResponse hotelOffer,
		List<HotelRoomResponse> hotelRoomResponse, String checkInDate, String checkOutDate) {
		hotelOffer.checkInDate = checkInDate;
		hotelOffer.checkOutDate = checkOutDate;
		hotelOffer.hotelRoomResponse = hotelRoomResponse;
		hotelOffer.isPackage = true;
		return hotelOffer;
	}

	public static List<HotelRoomResponse> convertPSSHotelRoomResponse(PackageOffersResponse packageOffer) {
		List<HotelRoomResponse> hotelRoomResponse = new ArrayList<>();
		for (PackageOffersResponse.PackageHotelOffer packageHotelOffer : packageOffer.packageHotelOffers) {
			packageHotelOffer.hotelOffer.productKey = packageHotelOffer.packageProductId;
			if (packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo == null) {
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo = new HotelRate();
			}
			if (packageHotelOffer.priceDifferencePerNight != null) {
				packageHotelOffer.hotelOffer.packageHotelDeltaPrice = packageHotelOffer.priceDifferencePerNight;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.priceToShowUsers = packageHotelOffer.hotelOffer.packageHotelDeltaPrice.amount
					.floatValue();
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.currencyCode = packageHotelOffer.hotelOffer.packageHotelDeltaPrice.currencyCode;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 0;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.userPriceType = Constants.PACKAGE_HOTEL_DELTA_PRICE_TYPE;
			}
			if (packageHotelOffer.packagePricing.hotelPricing != null) {
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.showResortFeeMessage = true;
				packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.totalMandatoryFees = packageHotelOffer.packagePricing.hotelPricing.mandatoryFees.feeTotal.amount
					.floatValue();
			}
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packagePricePerPerson = packageHotelOffer.pricePerPerson;
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packageSavings = packageHotelOffer.packagePricing.savings;
			packageHotelOffer.hotelOffer.rateInfo.chargeableRateInfo.packageTotalPrice = packageHotelOffer.packagePricing.packageTotal;
			packageHotelOffer.hotelOffer.hasFreeCancellation = packageHotelOffer.cancellationPolicy.hasFreeCancellation;
			if (packageHotelOffer.loyaltyInfo != null) {
				packageHotelOffer.hotelOffer.packageLoyaltyInformation = packageHotelOffer.loyaltyInfo;
			}
			hotelRoomResponse.add(packageHotelOffer.hotelOffer);
		}
		return hotelRoomResponse;
	}

	public static HotelRoomResponse convertMidHotelRoomResponse(HotelOffer roomOffer, MultiItemOffer room) {
		HotelRoomResponse hotelRoomResponse = new HotelOffersResponse.HotelRoomResponse();
		hotelRoomResponse.rateInfo = new RateInfo();
		hotelRoomResponse.rateInfo.chargeableRateInfo = new HotelRate();

		hotelRoomResponse.productKey = null; //won't be available
		hotelRoomResponse.packageHotelDeltaPrice = room.getPrice().deltaPricePerPerson();
		hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = room.getPrice().deltaPricePerPerson()
			.getAmount().floatValue();
		hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode = room.getPrice().deltaPricePerPerson()
			.getCurrency();
		hotelRoomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 0;
		hotelRoomResponse.rateInfo.chargeableRateInfo.userPriceType = Constants.PACKAGE_HOTEL_DELTA_PRICE_TYPE;
		hotelRoomResponse.rateInfo.chargeableRateInfo.discountPercent = room.getPrice().getSavings().getAmount()
			.floatValue();
		hotelRoomResponse.rateInfo.chargeableRateInfo.total = room.getPrice().getTotalPrice().getAmount().floatValue();
		hotelRoomResponse.rateInfo.chargeableRateInfo.airAttached = false;

		hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode = room.getPrice().getTotalPrice().getCurrency();
		hotelRoomResponse.rateInfo.chargeableRateInfo.showResortFeeMessage = true;

		if (roomOffer.getMandatoryFees() != null) {
			hotelRoomResponse.rateInfo.chargeableRateInfo.mandatoryDisplayType = roomOffer.getMandatoryFees()
				.getDisplayType();
			switch (roomOffer.getMandatoryFees().getDisplayType()) {
			case TOTAL:
				hotelRoomResponse.rateInfo.chargeableRateInfo.totalMandatoryFees = roomOffer.getMandatoryFees()
					.getTotalMandatoryFeesSupplyCurrency().getAmount().floatValue();
				hotelRoomResponse.rateInfo.chargeableRateInfo.localCurrency = roomOffer.getMandatoryFees().
					getTotalMandatoryFeesSupplyCurrency().getCurrency();
				break;
			case DAILY:
				hotelRoomResponse.rateInfo.chargeableRateInfo.totalMandatoryFees = roomOffer.getMandatoryFees()
					.getDailyResortFeePOSCurrency().getAmount().floatValue();
				break;
			case NONE:
				hotelRoomResponse.rateInfo.chargeableRateInfo.showResortFeeMessage = false;
				break;
			}
		}
		hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = room.getPrice().pricePerPerson();
		hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = room.getPrice().packageSavings();
		hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = room.getPrice().packageTotalPrice();

		hotelRoomResponse.isPayLater = false;
		hotelRoomResponse.hasFreeCancellation = room.getCancellationPolicy().getFreeCancellationAvailable();
		hotelRoomResponse.packageLoyaltyInformation = room.getLoyaltyInfo();
		hotelRoomResponse.bedTypes = convertMultiItemBedType(roomOffer.getBedTypes());
		hotelRoomResponse.currentAllotment = String.valueOf(roomOffer.getRoomsLeft());
		hotelRoomResponse.isSameDayDRR = roomOffer.getSameDayDRR();
		hotelRoomResponse.isDiscountRestrictedToCurrentSourceType = roomOffer.getSourceTypeRestricted();
		hotelRoomResponse.isMemberDeal = roomOffer.getMemberDeal();
		if (roomOffer.getPromotion() != null) {
			hotelRoomResponse.promoDescription = roomOffer.getPromotion().getDescription();
		}
		hotelRoomResponse.ratePlanCode = roomOffer.getRatePlanCode();
		hotelRoomResponse.roomTypeCode = roomOffer.getRoomTypeCode();
		hotelRoomResponse.roomLongDescription = roomOffer.getRoomLongDescription();
		hotelRoomResponse.roomThumbnailUrl = roomOffer.getThumbnailUrl();
		hotelRoomResponse.roomTypeDescription = roomOffer.getRoomRatePlanDescription();
		hotelRoomResponse.supplierType = roomOffer.getInventoryType();
		hotelRoomResponse.packageLoyaltyInformation = room.getLoyaltyInfo();

		return hotelRoomResponse;
	}
}
