package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXTicketSelectionWidget extends CardView {

	public LXTicketSelectionWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.ticket_selectors_container)
	LinearLayout ticketSelectorContainer;

	@InjectView(R.id.selected_ticket_summary)
	TextView ticketSummary;

	@InjectView(R.id.lx_book_now)
	Button bookNow;

	@InjectView(R.id.offer_title)
	TextView title;

	private Map<LXTicketType, Ticket> ticketsMap = new LinkedHashMap<>();

	private String offerId;
	private String offerTitle;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	public List<Ticket> getSelectedTickets() {
		List<Ticket> selectedTickets = new LinkedList<>();
		for (Ticket ticket : ticketsMap.values()) {
			if (ticket.count > 0) {
				selectedTickets.add(ticket);
			}
		}
		return selectedTickets;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}

	public void buildTicketPickers(AvailabilityInfo availabilityInfo) {

		title.setText(offerTitle);

		for (Ticket ticket : availabilityInfo.tickets) {
			LXTicketPicker ticketPicker = Ui.inflate(R.layout.lx_ticket_picker, ticketSelectorContainer, false);
			ticketSelectorContainer.addView(ticketPicker);

			ticketPicker.bind(ticket, offerId);

			// Initialize all ticket types with 0 count.
			ticketsMap.put(ticket.code, ticket);
		}
	}

	@Subscribe
	public void onTicketCountChanged(Events.LXTicketCountChanged event) {
		// Update only if the event was done by TicketPicker of belonging to this widget.
		if (Strings.isNotEmpty(offerId) && offerId.equals(event.offerId)) {
			ticketsMap.put(event.ticket.code, event.ticket);

			ticketSummary.setText(Strings.joinWithoutEmpties(", ", getTicketSummaries()));
			bookNow.setText(String.format(getResources().getString(R.string.offer_book_now_TEMPLATE),
				LXUtils.getTotalAmount(new ArrayList<>(ticketsMap.values())).getFormattedMoney()));
		}
	}

	private List<String> getTicketSummaries() {
		List<String> ticketsSummaries = new ArrayList<>();

		for (Ticket ticket : ticketsMap.values()) {
			ticketsSummaries.add(LXDataUtils.getTicketCountSummary(getContext(), ticket.code, ticket.count));
		}

		return ticketsSummaries;
	}
}
