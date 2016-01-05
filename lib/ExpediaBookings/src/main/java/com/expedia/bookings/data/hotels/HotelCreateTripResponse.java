package com.expedia.bookings.data.hotels;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.utils.Strings;

public class HotelCreateTripResponse extends BaseApiResponse {

	public String tripId;
	public String userId;
	public HotelProductResponse originalHotelProductResponse;
	public HotelProductResponse newHotelProductResponse;
	public ExpediaRewards expediaRewards;
	public String tealeafTransactionId;
	public List<ValidPayment> validFormsOfPayment;
	public String guestUserPromoEmailOptInStatus;
	public Coupon coupon;
	public List<PointsDetails> pointsDetails;

	public static class ExpediaRewards {
		public int totalPointsToEarn;
		public boolean isActiveRewardsMember;
		public String rewardsMembershipTierName;
	}

	public static class Coupon {
		public String code;
	}

	public static class HotelProductResponse {
		public String checkInDate;
		public String checkOutDate;
		public String adultCount;
		public String numberOfNights;
		public String numberOfRooms;
		public String hotelId;
		public String hotelName;
		public String localizedHotelName;
		public String hotelAddress;
		public String hotelCity;
		public String hotelStateProvince;
		public String hotelCountry;
		public String regionId;
		public String hotelStarRating;
		public HotelOffersResponse.HotelRoomResponse hotelRoomResponse;
		public String supplierType;
		public List<HotelOffersResponse.HotelAmenities> accessibilityAmenities;
		public String largeThumbnailUrl;

		boolean isVipAccess;
		String tealeafTransactionId;

		public String getHotelName() {
			if (Strings.isEmpty(localizedHotelName)) {
				return hotelName;
			}
			return localizedHotelName;
		}
	}

	public PointsDetails getPointDetails(PointsProgramType pointsProgramType) {
		if (pointsDetails != null) {
			for (PointsDetails pointDetail : pointsDetails) {
				if (pointDetail.getProgramName() == pointsProgramType) {
					return pointDetail;
				}
			}
		}
		return null;
	}

	public boolean isExpediaRewardsRedeemable() {
		return ValidPayment.isPaymentTypeSupported(validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS);
	}

	public Money getTripTotal() {
		return newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getDisplayTotalPrice();
	}
}
