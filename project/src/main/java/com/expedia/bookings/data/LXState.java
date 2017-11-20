package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXUtils;
import com.squareup.otto.Subscribe;

public class LXState {
	public LxSearchParams searchParams;
	public LXActivity activity;
	public Offer offer;
	private List<Ticket> selectedTickets;

	/**
	 * Original Price for Current LX Booking Workflow.
	 * Total computed from the Tickets selected by the user (in selectedTickets)
	 */
	private Money originalTotalPrice;

	/**
	 * Latest Price for the Current LX Booking Workflow.
	 * Normally equals the originalTotalPrice, but in case of a Price Change during CreateTrip or Checkout, this holds the New Price returned by the API Response
	 */
	private Money latestTotalPrice;
	private static final String MOD_PROMO_ID = "3";

	public LXState() {
		Events.register(this);
	}

	@Subscribe
	public void onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		this.searchParams = event.lxSearchParams;
	}

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		this.activity = event.lxActivity;
	}

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		activity.destination = event.activityDetails.destination;
		activity.location = event.activityDetails.location;
		activity.regionId = event.activityDetails.regionId;
		activity.freeCancellationMinHours = event.activityDetails.freeCancellationMinHours;
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		this.offer = event.offer;
		this.selectedTickets = event.selectedTickets;
		this.originalTotalPrice = LXUtils.getTotalAmount(selectedTickets);
		this.latestTotalPrice = LXUtils.getTotalAmount(selectedTickets);
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXOfferLevelCancellationPolicySupport)) {
			activity.freeCancellationMinHours = event.offer.freeCancellationMinHours;
		}
	}

	@Subscribe
	public void updateTotalPrice(Events.LXUpdateCheckoutSummaryAfterPriceChange event) {
		this.latestTotalPrice = event.lxCheckoutResponse.newTotalPrice;
	}

	@Subscribe
	public void onCreateTripSucceeded(Events.LXCreateTripSucceeded event) {
		if (event.createTripResponse.hasPriceChange()) {
			this.latestTotalPrice = event.createTripResponse.newTotalPrice;
		}
	}

	public LXCreateTripParams createTripParams(Context context) {

		String promotionId = "";
		boolean lxModTestEnabled = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidLXMOD);

		boolean modPricingEnabled = activity.modPricingEnabled(lxModTestEnabled);
		if (modPricingEnabled) {
			promotionId = MOD_PROMO_ID;
		}
		LXOfferSelected offerSelected = new LXOfferSelected(activity.id, this.offer, this.selectedTickets, activity.regionId, promotionId);

		List<LXOfferSelected> offersSelected = new ArrayList<>();
		offersSelected.add(offerSelected);

		return new LXCreateTripParams().tripName(activity.location).offersSelected(offersSelected);
	}

	/**
	 * Utility Methods around Selected Tickets, to eliminate its direct usage
	 * CAVEAT: The intention above is partially defeated because at the moment, the selectedTickets getter is being used in Cost Breakdown Dialog on Checkout
	 * Once the API Team exposes Updated Ticket Prices post Price Change, this Tech Debt needs to rectified and dealt with.
	 * @return
	 */
	public List<Ticket> selectedTickets() {
		return selectedTickets;
	}

	public Money originalTotalPrice() {
		return originalTotalPrice;
	}

	public Money latestTotalPrice() {
		return latestTotalPrice;
	}

	public String selectedTicketsCountSummary(Context context) {
		return LXDataUtils.ticketsCountSummary(context, selectedTickets);
	}

	public int selectedTicketsCount() {
		return LXUtils.getTotalTicketCount(selectedTickets);
	}

	public int selectedChildTicketsCount() {
		return LXUtils.getTicketTypeCount(selectedTickets, LXTicketType.Child) + LXUtils
			.getTicketTypeCount(selectedTickets, LXTicketType.Infant);
	}
}
