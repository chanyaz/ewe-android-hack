package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.utils.CollectionUtils;

public class RailOffer extends BaseRailOffer {
	public List<RailProduct> railProductList;

	@Nullable
	public RailLegOption outboundLeg; //set in code on the offer when the leg/offer combo is selected

	@Nullable
	public RailLegOption inboundLeg; //set in code on the offer when the leg/offer combo is selected

	public boolean containsLegOptionId(Integer legOptionId) {
		for (RailProduct product : railProductList) {
			if (product.legOptionIndexList.contains(legOptionId)) {
				return true;
			}
		}
		return false;
	}

	// creating a key by combining fare class, service class and total fare amount
	public String getUniqueIdentifier() {
		return railProductList.get(0).aggregatedCarrierFareClassDisplayName +
			railProductList.get(0).aggregatedCarrierServiceClassDisplayName + totalPrice.amount;
	}

	@Override
	public List<? extends RailProduct> getRailProductList() {
		return railProductList;
	}

	public int getOutboundLegOptionId() {
		if (CollectionUtils.isNotEmpty(railProductList) && CollectionUtils
			.isNotEmpty(railProductList.get(0).legOptionIndexList)) {
			return railProductList.get(0).legOptionIndexList.get(0);
		}
		return -1;
	}

	public int getInboundLegOptionId() {
		if (CollectionUtils.isNotEmpty(railProductList) && CollectionUtils
			.isNotEmpty(railProductList.get(0).legOptionIndexList)
			&& railProductList.get(0).legOptionIndexList.size() == 2) {
			return railProductList.get(0).legOptionIndexList.get(1);
		}
		return -1;
	}
}
