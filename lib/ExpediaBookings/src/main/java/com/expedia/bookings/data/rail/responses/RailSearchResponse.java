package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.utils.Strings;

public class RailSearchResponse {
	private final static int OUTBOUND_BOUND_ORDER = 1;
	private final static int INBOUND_BOUND_ORDER = 2;

	public final List<RailPassenger> passengerList;
	public final List<RailLeg> legList;
	public final List<RailOffer> offerList;
	public final RailResponseStatus responseStatus;

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

	public boolean hasWarnings() {
		return responseStatus != null && !responseStatus.warningList.isEmpty();
	}

	public boolean hasChildrenAreFreeWarning() {
		return hasWarnings() && responseStatus.getWarningByCode("WARN0001") != null;
	}

	@Nullable
	public RailLeg getInboundLeg() {
		return findLegWithBoundOrder(INBOUND_BOUND_ORDER);
	}

	@Nullable
	public RailLeg getOutboundLeg() {
		return findLegWithBoundOrder(OUTBOUND_BOUND_ORDER);
	}

	public boolean hasInbound() {
		return legList.size() == 2;
	}

	/**
	 * Returns matching return leg options for a given offer:
	 * If the offer is an open return, returns only the leg options that can be combined with that offer
	 * (have open return fares present); otherwise it will return all inbound leg options.
	 */
	@NotNull
	public List<RailLegOption> getInboundLegOptionsForOffer(@NotNull RailOffer offer) {
		RailLeg inboundLeg = getInboundLeg();

		if (inboundLeg == null) {
			return new ArrayList<>();
		}

		if (!offer.isOpenReturn()) {
			return inboundLeg.legOptionList;
		}

		Set<Integer> inboundLegOptionIds = findOpenReturnInboundLegOptionIdsFor(offer);
		return inboundLeg.filterLegOptions(inboundLegOptionIds);
	}

	public boolean hasError() {
		boolean hasError = false;
		if (!responseStatus.status.equals(RailsApiStatusCodes.STATUS_SUCCESS)) {
			hasError = true;
		}
		else if (Strings.isNotEmpty(responseStatus.statusCategory) && responseStatus.statusCategory
			.equals(RailsApiStatusCodes.STATUS_CATEGORY_NO_PRODUCT)) {
			hasError = true;
		}
		return hasError;
	}

	/**
	 * For one-way and outbound
	 * Filters duplicate open return offers based on uniqueIdentifier
	 */
	public List<RailOffer> filterOutboundOffers(List<RailOffer> offerList) {
		ArrayList<String> fareServiceKeys = new ArrayList<>();
		ArrayList<RailOffer> filteredList = new ArrayList<>();

		for (RailOffer offer : offerList) {
			if (!offer.isOpenReturn()) {
				filteredList.add(offer);
			}
			else {
				String currentKey = offer.getUniqueIdentifier();
				if (!fareServiceKeys.contains(currentKey)) {
					fareServiceKeys.add(currentKey);
					filteredList.add(offer);
				}
			}
		}
		return filteredList;
	}

	/**
	 * Returns a list of filtered inbound offers based on
	 * If outbound selected offer is -
	 * openReturn - show only the matching open return offers on inbound
	 * non openReturn - show only non open return offers on inbound
	 */
	public List<RailOffer> filterInboundOffers(List<RailOffer> offerList, RailOffer outboundOffer) {
		if (outboundOffer.isOpenReturn()) {
			return filterMatchingInboundOpenReturnOffer(offerList, outboundOffer);
		}
		return filterOutInboundOpenReturnOffers(offerList, outboundOffer);
	}

	/**
	 * Returns a list of non open return offers
	 */
	private List<RailOffer> filterOutInboundOpenReturnOffers(List<RailOffer> offerList, RailOffer outboundOffer) {
		List<RailOffer> filteredList = new ArrayList<>();
		for (RailOffer offer : offerList) {
			if (!offer.isOpenReturn()) {
				filteredList.add(offer);
			}
		}
		return filteredList;
	}

	/**
	 * Returns a list of matching inbound open return offers for selected outbound offer
	 */
	private List<RailOffer> filterMatchingInboundOpenReturnOffer(List<RailOffer> offerList, RailOffer outboundOffer) {
		List<RailOffer> filteredList = new ArrayList<>();
		for (RailOffer offer : offerList) {
			if (isMatchingInboundOpenReturnOffer(offer, outboundOffer)) {
				filteredList.add(offer);
				break;
			}
		}
		return filteredList;
	}

	/**
	 * Returns true if current railOffer matches selected outboundOffer based on leg option indices and uniqueIdentifier
	 * false otherwise
	 */
	private boolean isMatchingInboundOpenReturnOffer(RailOffer railOffer, RailOffer outboundOffer) {
		boolean valid = railOffer.isOpenReturn()
			&& railOffer.containsLegOptionId(outboundOffer.getOutboundLegOptionId());
		return (valid && outboundOffer.getUniqueIdentifier().equals(railOffer.getUniqueIdentifier()));
	}

	@Nullable
	private RailLeg findLegWithBoundOrder(Integer legBoundOrder) {
		for (RailLeg railLeg : legList) {
			if (railLeg.legBoundOrder.equals(legBoundOrder)) {
				return railLeg;
			}
		}
		return null;
	}

	private Set<Integer> findOpenReturnInboundLegOptionIdsFor(RailOffer outboundOffer) {
		int outboundIndex = outboundOffer.getOutboundLegOptionId();
		String offerUniqueId = outboundOffer.getUniqueIdentifier();

		Set<Integer> inboundIds = new HashSet<>();
		for (RailOffer offer : offerList) {
			if (offer.isOpenReturn() && offer.containsLegOptionId(outboundIndex) && offer.getUniqueIdentifier()
				.equals(offerUniqueId)) {
				int inboundId = offer.getInboundLegOptionId();
				if (inboundId >= 0) {
					inboundIds.add(inboundId);
				}
			}
		}
		return inboundIds;
	}
}
