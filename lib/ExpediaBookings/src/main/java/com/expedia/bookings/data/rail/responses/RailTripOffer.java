package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Strings;

public class RailTripOffer extends BaseRailOffer {
	public List<RailTripProduct> railProductList;
	public final List<RailTicketDeliveryOption> ticketDeliveryOptionList;

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
		if (CollectionUtils.isNotEmpty(railProductList)) {
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

	public String colonSeparatedSegmentOperatingCarriers() {
		RailLegOption outboundLegOption = getOutboundLegOption();
		RailLegOption inboundLegOption = getInboundLegOption();

		StringBuilder carrier = new StringBuilder("");
		for (RailSegment railSegment : outboundLegOption.travelSegmentList) {
			if (Strings.isNotEmpty(railSegment.operatingCarrier)) {
				carrier.append(railSegment.operatingCarrier).append(":");
			}
		}

		if (inboundLegOption != null) {
			for (RailSegment railSegment : inboundLegOption.travelSegmentList) {
				if (Strings.isNotEmpty(railSegment.operatingCarrier)) {
					carrier.append(railSegment.operatingCarrier).append(":");
				}
			}
		}

		String carrierString = carrier.toString();
		if (!Strings.isEmpty(carrierString)) {
			return carrierString.substring(0, carrierString.length() - 1);
		}
		return carrierString;
	}
}
