package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.utils.Strings;

public class HotelOffersResponse {

	public String airAttachExpirationTimeSeconds;
	public String checkInDate;
	public String checkOutDate;
	public Boolean deskTopOverrideNumber;
	public String firstHotelOverview;
	public String hotelAddress;
	public List<HotelAmenities> hotelAmenities;
	public HotelText hotelAmenitiesText;
	public String hotelCity;
	public String hotelCountry;
	public Double hotelGuestRating;
	public String hotelId;
	public String hotelName;
	public List<HotelRoomResponse> hotelRoomResponse;

	public Double hotelStarRating;

	public Integer totalReviews;
	public Integer totalRecommendations;
	public String telesalesNumber;
	public Boolean isVipAccess;

	public List<Photos> photos;

	public static class HotelAmenities {
		public String id;
		public String description;
	}

	public static class HotelText {
		public String content;
		public String name;
	}

	public static class HotelRoomResponse {
		public List<BedTypes> bedTypes;
		public String cancellationPolicy;
		public Boolean hasFreeCancellation;
		public String freeCancellationWindowDate;
		public HotelRoomResponse payLaterOffer;
		public Boolean immediateChargeRequired;
		public Boolean nonRefundable;
		public String productKey;
		public Boolean rateChange;
		public String rateDescription;
		public RateInfo rateInfo;
		public String roomTypeDescription;
		public String roomLongDescription;
		public String roomThumbnailUrl;
		public List<ValueAdds> valueAdds;
		public String supplierType;

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
}
