package com.expedia.bookings.data;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;
import com.squareup.otto.Subscribe;

public class LXState {
	public LXSearchParams searchParams;
	public LXActivity activity;
	public Offer offer;
	public Map<Ticket, Integer> selectedTickets;

	public BigDecimal amount;

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
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		this.offer = event.offer;
		Map<Ticket, Integer> tickets = new LinkedHashMap<>();
		for (Map.Entry<Ticket, Integer> entry : event.tickets.entrySet()) {
			if (entry.getValue() > 0) {
				tickets.put(entry.getKey(), entry.getValue());
			}
		}
		this.selectedTickets = tickets;
		this.amount = LXUtils.getTotalAmount(tickets);
	}

	public Map<String, Object> createTripParams() throws
		UnsupportedEncodingException {
		Map<String, Object> params = new HashMap<>();
		String offerPrefix = "items[0].";
		String activityId = activity.id;
		String offerId = offer.id;
		org.joda.time.DateTime activityDate = DateUtils
			.yyyyMMddHHmmssToDateTime(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		String location = activity.location;
		Set<Map.Entry<Ticket, Integer>> entries = selectedTickets.entrySet();

		params.put("tripName", URLEncoder.encode(location, "utf-8"));
		params.put(URLEncoder.encode(offerPrefix + "activityId", "utf-8"), activityId);
		params.put(URLEncoder.encode(offerPrefix + "activityItemId", "utf-8"), offerId);
		params.put(URLEncoder.encode(offerPrefix + "activityDate", "utf-8"), DateUtils.toYYYYMMTddhhmmss(activityDate));
		params.put(URLEncoder.encode(offerPrefix + "amount", "utf-8"), amount);

		int ticketIndex = 0;
		for (Map.Entry<Ticket, Integer> entry : entries) {
			Ticket ticket = entry.getKey();
			LXTicketType code = ticket.code;
			String ticketId = ticket.ticketId;
			String ticketPrefix = offerPrefix + "tickets[" + ticketIndex + "].";
			params.put(URLEncoder.encode(ticketPrefix + "count", "utf-8"), entry.getValue());
			//TODO - ensure the correct serialized string is going in
			params.put(URLEncoder.encode(ticketPrefix + "code", "utf-8"), code);
			params.put(URLEncoder.encode(ticketPrefix + "ticketId", "utf-8"), ticketId);
			ticketIndex++;
		}
		return params;
	}
}
