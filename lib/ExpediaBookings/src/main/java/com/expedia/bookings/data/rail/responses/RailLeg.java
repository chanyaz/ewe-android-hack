package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;

public class RailLeg {
	public final Integer legBoundOrder;
	public RailStation departureStation;
	public RailStation arrivalStation;
	public final List<RailLegOption> legOptionList;
	public Money cheapestPrice;

	@Nullable
	public Money cheapestInboundPrice; //Set in code when showing outbound legs

	List<RailLegOption> filterLegOptions(Set<Integer> legOptionIds) {
		List<RailLegOption> legOptions = new ArrayList<>();
		for (RailLegOption legOption : legOptionList) {
			if (legOptionIds.contains(legOption.legOptionIndex)) {
				legOptions.add(legOption);
			}
		}

		return legOptions;
	}
}
