package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.utils.DateUtils;

public class RailSearchResponse {

	public List<RailPassenger> passengerList;
	public List<RailLeg> legList;
	public List<RailOffer> offerList;

	public RailOffer findOfferForLeg(@NotNull LegOption it) {
		for (RailOffer offer : offerList) {
			if (offer.containsLegOptionId(it.legOptionIndex)) {
				return offer;
			}
		}
		return null;
	}

	public List<RailOffer> findOffersForLegOption(Integer optionId) {
		List<RailOffer> offers = new ArrayList<>();

		for (RailOffer offer : offerList) {
			if (offer.containsLegOptionId(optionId)) {
				offers.add(offer);
			}
		}

		return offers;
	}

	public static class RailLeg {
		public String legIndex;
		public RailStation departureStation;
		public RailStation arrivalStation;
		public List<LegOption> legOptionList;
	}

	public static class RailDateTime {
		public String raw;
		public Long epochSeconds;
		public String localized;
		public String localizedShortDate;

		public DateTime toDateTime() {
			return DateTime.parse(raw);
		}
	}

	public static class RailMoney {
		public String amount;
		public String currencyCode;
		public String formattedPrice;
		public String formattedWholePrice;
	}

	public static class LegOption {
		public Integer legOptionIndex;
		public RailStation departureStation;
		public RailStation arrivalStation;
		public RailDateTime departureDateTime;
		public RailDateTime arrivalDateTime;
		public String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]
		public String aggregatedMarketingCarrier;
		public String aggregatedOperatingCarrier;
		public RailMoney bestPrice;
		public List<RailSegment> travelSegmentList;


		public int durationMinutes() {
			return DateUtils.parseDurationMinutes(duration);
		}

		public String allOperators() {
			boolean first = true;
			String result = "";
			for (RailSegment segment : travelSegmentList) {
				if (!first) {
					result += ", ";
				}
				result += segment.marketingCarrier;
				first = false;
			}

			return result;
		}

		@NotNull
		public DateTime getDepartureDateTime() {
			return departureDateTime.toDateTime();
		}

		@NotNull
		public DateTime getArrivalDateTime() {
			return arrivalDateTime.toDateTime();
		}

		public int changesCount() {
			return travelSegmentList.size() - 1;
		}
	}

	public static class RailSegment {
		public String travelMode;
		public RailStation departureStation;
		public RailStation arrivalStation;
		public RailDateTime departureDateTime;
		public RailDateTime arrivalDateTime;
		public String marketingCarrier; //"Virgin"
		public String operatingCarrier; //"Virgin"
		public String duration;
		public RailTravelMedium travelMedium;
		public String travelSegmentIndex;

		public boolean isTransfer() {
			return !"Train".equals(travelMode);
		}

		public static class RailTravelMedium {
			public String travelMediumCode; //"ICY"
			public String travelMediumName; //"Inter-City"
		}

		public int durationMinutes() {
			return DateUtils.parseDurationMinutes(duration);
		}

		@NotNull
		public DateTime getDepartureDateTime() {
			return departureDateTime.toDateTime();
		}

		@NotNull
		public DateTime getArrivalDateTime() {
			return arrivalDateTime.toDateTime();
		}
	}

	public static class RailOffer {
		public RailMoney totalPrice;
		public List<RailProduct> railProductList;
		public String railOfferToken;
		//TODO Add price breakdown

		@Nullable
		public LegOption outboundLeg; //set in code on the offer when the leg/offer combo is selected

		public boolean containsLegOptionId(Integer legOptionId) {
			for (RailProduct product : railProductList) {
				for (Integer legId : product.legOptionIndexList) {
					if (legOptionId.equals(legId)) {
						return true;
					}
				}
			}
			return false;
		}

		public static class RailProduct {
			public RailMoney totalPrice;
			public List<Integer> legOptionIndexList;
			public List<FareBreakdown> fareBreakdownList;
			public String serviceClassCode;
			public String fareClass;
			public boolean refundable;
			public boolean exchangeable;
			public String railProductToken;
			public String aggregatedCarrierServiceClassDisplayName;
			public String aggregatedCarrierFareClassDisplayName;
			public String aggregatedFareDescription;

			public static class FareBreakdown {
				public List<PassengerReference> passengerFareList;

				public static class PassengerReference {
					public Integer passengerIndex;
					public String passengerTypeCode;
					public List<FareCode> passengerSegmentFareList;

					public static class FareCode {
						public String fareCode;
						public String serviceClassCategory;
						public String carrierServiceClassDisplayName;
						public String fareDescription;
						public String fareClassCategory;
						public String carrierFareClassDisplayName;
						public Integer travelSegmentIndex;
					}
				}
			}
		}
	}
}
