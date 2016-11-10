package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.utils.CollectionUtils;

public class RailTripOffer extends BaseRailOffer {
	public List<RailTripProduct> railProductList;
	public List<RailTicketDeliveryOption> ticketDeliveryOptionList;

	@Override
	public List<? extends RailProduct> getRailProductList() {
		return railProductList;
	}

	@Nullable
	public RailLegOption getOutboundLegOption() {
		if (CollectionUtils.isNotEmpty(railProductList)) {
			return railProductList.get(0).getFirstLegOption();
		}
		return null;
	}

	@Nullable
	public RailLegOption getInboundLegOption() {
		// todo will change with open return
		if (CollectionUtils.isNotEmpty(railProductList) && isRoundTrip()) {
			if (isRoundTrip()) {
				return railProductList.get(1).getFirstLegOption();
			}
			else if (isOpenReturn()) {
				return railProductList.get(0).getSecondLegOption();
			}
		}
		return null;
	}

	public boolean isRoundTrip() {
		return railProductList.size() == 2;
	}

	public String colonSeparatedMarketingCarriers() {
		StringBuilder carrier = new StringBuilder("");
		for (RailTripProduct railProduct: railProductList) {
			for (RailLegOption railLegOption: railProduct.legOptionList) {
				carrier.append(railLegOption.aggregatedMarketingCarrier + ":");
			}
		}
		String carrierString = carrier.toString();
		return carrierString.substring(0, carrierString.length() - 1);
	}
}
