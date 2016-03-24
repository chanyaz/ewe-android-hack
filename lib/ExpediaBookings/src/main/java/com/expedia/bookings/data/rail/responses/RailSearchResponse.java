package com.expedia.bookings.data.rail.responses;

import java.math.BigDecimal;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.Passengers;

public class RailSearchResponse {

	public void initialize() {
		for (RailLeg leg : railSearchResult.legList) {
			for (LegOption option : leg.legOptions) {
				option.setCheapestOffer(findCheapestOfferContainingLegOption(option.legOptionId));
			}
		}
	}

	private RailOffer findCheapestOfferContainingLegOption(String legOptionId) {
		float amount = Float.MAX_VALUE;
		RailOffer cheapestOffer = null;
		for (RailOffer offer : railSearchResult.railOfferList) {
			if (offer.getTotalPrice().getAmount().floatValue() < amount && offer.containsLegOptionId(legOptionId)) {
				cheapestOffer = offer;
				amount = offer.getTotalPrice().getAmount().floatValue();
			}
		}
		return cheapestOffer;
	}

	public RailSearchResult railSearchResult;

	public static class RailSearchResult {
		public List<Passengers> passengers;
		public List<RailLeg> legList;
		public List<RailOffer> railOfferList;

		public RailOffer findOfferForLeg(@NotNull LegOption it) {
			for (RailOffer offer : railOfferList) {
				if (offer.containsLegOptionId(it.legOptionId)) {
					return offer;
				}
			}
			return null;
		}
	}

	public static class RailLeg {
		public String legId;
		public RailStation departureStationDetails;
		public RailStation arrivalStationDetails;
		public List<LegOption> legOptions;
	}

	public static class LegOption {
		public RailStation departureStationDetails;
		public RailStation arrivalStationDetails;
		public String departureDateTime;
		public String arrivalDateTime;
		public int durationInMinutes;
		public String legOptionId;
		public List<RailSegment> segmentList;
		private RailOffer cheapestOffer;

		public String formattedPrice() {
			if (cheapestOffer != null) {
				return cheapestOffer.getTotalPrice().getFormattedMoney();
			}
			throw new RuntimeException("Price should not be null!");
		}

		public String allOperators() {
			boolean first = true;
			String result = "";
			for (RailSegment segment : segmentList) {
				if (!first) {
					result += ", ";
				}
				result += segment.supplier.operatingCarrier;
				first = false;
			}

			return result;
		}

		public void setCheapestOffer(RailOffer cheapestOffer) {
			this.cheapestOffer = cheapestOffer;
		}

		@NotNull
		public DateTime getDepartureDateTime() {
			return DateTime.parse(departureDateTime);
		}

		@NotNull
		public DateTime getArrivalDateTime() {
			return DateTime.parse(arrivalDateTime);
		}

		public int changesCount() {
			return segmentList.size() - 1;
		}
	}

	public static class RailSegment {
		public String travelMode;
		public RailStation departureStationDetails;
		public RailStation arrivalStationDetails;
		public String departureDateTime;
		public String arrivalDateTime;
		public String serviceName; //"Virgin"
		public int durationInMinutes;
		public RailEquipment equipment;
		public RailSupplier supplier;
		public String segmentId;

		public boolean isTransfer() {
			return !"Train".equals(travelMode);
		}

		public static class RailEquipment {
			public String code; //"ICY"
			public String name; //"Inter-City"
		}

		public static class RailSupplier {
			public String operatingCarrier; //"Virgin"
		}

		public int durationHours() {
			return durationInMinutes / 60;
		}

		public int durationMinutes() {
			return durationInMinutes % 60;
		}

		@NotNull
		public DateTime getDepartureDateTime() {
			return DateTime.parse(departureDateTime);
		}

		@NotNull
		public DateTime getArrivalDateTime() {
			return DateTime.parse(arrivalDateTime);
		}
	}

	public static class RailOffer {
		private RailMoney totalPrice;
		public List<RailProduct> railProductList;

		@Nullable
		public LegOption outboundLeg; //set in code on the offer when the leg/offer combo is selected

		public Money getTotalPrice() {
			return totalPrice.toMoney();
		}

		public boolean containsLegOptionId(String legOptionId) {
			for (RailProduct product : railProductList) {
				for (String legOption : product.legOptionReferences) {
					if (legOptionId.equalsIgnoreCase(legOption)) {
						return true;
					}
				}
			}
			return false;
		}

		public static class RailProduct {
			public RailMoney totalPrice;
			public List<String> legOptionReferences;
			public List<FareBreakUp> fareBreakUpList;
			public String serviceClassCode;
			public String fareClass;
			public boolean refundable;
			public boolean exchangeable;
			public String railProductToken;

			public static class FareBreakUp {
				public List<PassengerReference> passengerReferences;

				public static class PassengerReference {
					public List<FareCode> fareCodes;

					public static class FareCode {
						public String travelSegmentIdRef;
						public String serviceClassCode;
						public String carrierCabinClass;
						public String fareClass;
					}
				}
			}
		}
	}

	private static class RailMoney {
		private BigDecimal value;
		private String currency;

		public Money toMoney() {
			return new Money(value, currency);
		}
	}
}

