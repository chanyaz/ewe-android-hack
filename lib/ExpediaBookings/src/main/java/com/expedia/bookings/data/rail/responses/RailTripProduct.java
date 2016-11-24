package com.expedia.bookings.data.rail.responses;

import java.util.List;

import com.expedia.bookings.utils.CollectionUtils;

public class RailTripProduct extends RailProduct {
	public List<RailLegOption> legOptionList;

	public RailLegOption getFirstLegOption() {
		if (CollectionUtils.isNotEmpty(legOptionList)) {
			return legOptionList.get(0);
		}
		return null;
	}

	public RailLegOption getSecondLegOption() {
		if (CollectionUtils.isNotEmpty(legOptionList) && legOptionList.size() == 2) {
			return legOptionList.get(1);
		}
		return null;
	}
}
