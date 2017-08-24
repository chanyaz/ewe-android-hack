package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.payment.LoyaltyInformation;

public class HotelSearchResponse extends BaseApiResponse {
	public String pageViewBeaconPixelUrl = "";
	public List<Hotel> hotelList = new ArrayList<>();
	public List<Neighborhood> allNeighborhoodsInSearchRegion = new ArrayList<>();
	public Map<String, AmenityOptions> amenityFilterOptions = new HashMap<>();
	public String searchRegionCity = "";
	public String searchRegionId = "";
	public List<PriceOption> priceOptions = new ArrayList<>();
	public boolean hasLoyaltyInformation;

	public transient Map<String, Neighborhood> neighborhoodsMap = new HashMap<>();
	public transient HotelRate.UserPriceType userPriceType = HotelRate.UserPriceType.UNKNOWN;
	public transient boolean isFilteredResponse;
	public transient boolean isPinnedSearch;

	public static class Neighborhood {
		public final String name;
		public final String id;

		public transient List<Hotel> hotels = new ArrayList<>();
		public transient int score;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Neighborhood that = (Neighborhood) o;

			if (name != null ? !name.equals(that.name) : that.name != null) {
				return false;
			}
			return id != null ? id.equals(that.id) : that.id == null;

		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (id != null ? id.hashCode() : 0);
			return result;
		}
	}

	public static class AmenityOptions {
	}

	public static class PriceOption {
		public Integer minPrice;
		public Integer maxPrice;
	}

	public static HotelSearchResponse convertPackageToSearchResponse(BundleSearchResponse packageSearchResponse) {
		HotelSearchResponse response = new HotelSearchResponse();
		response.hotelList = packageSearchResponse.getHotels();
		response.userPriceType = HotelRate.UserPriceType.PACKAGES;
		return response;
	}

	public void setHasLoyaltyInformation() {
		for (Hotel hotel : hotelList) {
			HotelRate lowRateInfo = hotel.lowRateInfo;
			if (lowRateInfo != null) {
				LoyaltyInformation loyaltyInformation = lowRateInfo.loyaltyInfo;
				if (loyaltyInformation != null && loyaltyInformation.isBurnApplied()) {
					hasLoyaltyInformation = true;
					return;
				}
			}
		}
		hasLoyaltyInformation = false;
	}
}
