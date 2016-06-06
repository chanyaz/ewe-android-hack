package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.RailPassenger;

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
		public List<SearchLegOption> legOptionList;
	}

	public static class RailOffer {
		public Money totalPrice;
		public List<RailSearchProduct> railProductList;
		public String railOfferToken;
		//TODO Add price breakdown

		@Nullable
		public LegOption outboundLeg; //set in code on the offer when the leg/offer combo is selected

		public boolean containsLegOptionId(Integer legOptionId) {
			for (RailSearchProduct product : railProductList) {
				for (Integer legId : product.legOptionIndexList) {
					if (legOptionId.equals(legId)) {
						return true;
					}
				}
			}
			return false;
		}

		public static class RailSearchProduct {
			public Money totalPrice;
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
