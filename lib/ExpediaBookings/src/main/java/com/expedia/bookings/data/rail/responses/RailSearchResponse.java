package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.utils.CollectionUtils;

public class RailSearchResponse {

	public List<RailPassenger> passengerList;
	public List<RailLeg> legList;
	public List<RailOffer> offerList;

	public List<RailOffer> findOffersForLegOption(RailLegOption legOption) {
		List<RailOffer> offers = new ArrayList<>();

		for (RailOffer offer : offerList) {
			if (offer.containsLegOptionId(legOption.legOptionIndex)) {
				offer.outboundLeg = legOption;
				offers.add(offer);
			}
		}

		return offers;
	}

	public static class RailLeg {
		public Integer legIndex;
		public Integer legBoundOrder;
		public RailStation departureStation;
		public RailStation arrivalStation;
		public List<RailLegOption> legOptionList;
		public Money cheapestPrice;

		@Nullable
		public Money cheapestInboundPrice; //Set in code when showing outbound legs
	}

	public static class RailOffer extends BaseRailOffer {
		public List<RailProduct> railProductList;

		@Nullable
		public RailLegOption outboundLeg; //set in code on the offer when the leg/offer combo is selected

		@Nullable
		public RailLegOption inboundLeg; //set in code on the offer when the leg/offer combo is selected

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

		public boolean hasRailCardApplied() {
			boolean railCardApplied = false;
			if (CollectionUtils.isNotEmpty(railProductList)) {
				List<RailCard> fareQualifierList = railProductList.get(0).fareQualifierList;
				railCardApplied = CollectionUtils.isNotEmpty(fareQualifierList);
			}
			return railCardApplied;
		}

		public boolean isOpenReturn() {
			boolean openReturn = false;
			if (CollectionUtils.isNotEmpty(railProductList)) {
				openReturn = railProductList.get(0).openReturn;
			}
			return openReturn;
		}

		public String getUniqueIdentifier() {
			return railProductList.get(0).aggregatedCarrierFareClassDisplayName +
				railProductList.get(0).aggregatedCarrierServiceClassDisplayName + totalPrice.amount;
		}
	}
}
