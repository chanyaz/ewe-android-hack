package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

public class LXState {
	public LXSearchParams searchParams;
	public LXActivity activity;
	public Offer offer;
	// This contains the details to be sent in create trip post body.
	public LXOfferSelected offerSelected;

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
		activity.location = event.activityDetails.location;
		activity.currencyCode = event.activityDetails.currencyCode;
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		this.offer = event.offer;
		this.offerSelected = event.offerSelected;
	}

	public LXCreateTripParams createTripParams() {

		// TODO : Need to check if regionID & allDayActivity is required.
		List<LXOfferSelected> offersSelected = new ArrayList<>();
		offerSelected.activityId = activity.id;
		offersSelected.add(offerSelected);

		return new LXCreateTripParams().tripName(activity.location)
			.offersSelected(offersSelected);
	}

}
