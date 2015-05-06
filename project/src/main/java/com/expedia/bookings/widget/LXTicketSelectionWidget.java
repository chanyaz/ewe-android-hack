package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXTicketSelectionWidget extends LinearLayout {


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
	com.expedia.bookings.widget.TextView title;

	@InjectView(R.id.free_cancellation)
	TextView freeCancellationText;

	@InjectView(R.id.ticket_summary_container)
	LinearLayout ticketSummaryContainer;

	private List<Ticket> selectedTickets = new ArrayList<>();

	private String offerId;
	private String offerTitle;
	private boolean freeCancellation;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	public List<Ticket> getSelectedTickets() {
		return selectedTickets;
	}

	public void setFreeCancellation(boolean freeCancellation) {
		this.freeCancellation = freeCancellation;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}

	public void buildTicketPickers(AvailabilityInfo availabilityInfo) {

		title.setText(offerTitle);

		freeCancellationText.setVisibility(freeCancellation ? VISIBLE : GONE);
		int index = 0;
		for (Ticket ticket : availabilityInfo.tickets) {
			LXTicketPicker ticketPicker = Ui.inflate(R.layout.lx_ticket_picker, ticketSelectorContainer, false);
			ticketSelectorContainer.addView(ticketPicker);

			// Set default count of first ticket in offer.
			int defaultCount = 0;
			if (index == 0) {
				defaultCount = getResources().getInteger(R.integer.lx_offer_ticket_default_count);
				index++;
			}
			selectedTickets.add(ticket);

			ticketPicker.bind(ticket, offerId, defaultCount);
		}
	}

	@Subscribe
	public void onTicketCountChanged(Events.LXTicketCountChanged event) {
		// Update only if the event was done by TicketPicker of belonging to this widget.
		if (Strings.isNotEmpty(offerId) && offerId.equals(event.offerId)) {
			updateTicketCountInSelectedTicketsFrom(event.ticket);

			ticketSummaryContainer.setVisibility(LXUtils.getTotalTicketCount(selectedTickets) > 0 ? VISIBLE : GONE);
			ticketSummary.setText(LXDataUtils.ticketsCountSummary(getContext(), selectedTickets));
			bookNow.setText(String.format(getResources().getString(R.string.offer_book_now_TEMPLATE),
				LXUtils.getTotalAmount(selectedTickets).getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)));
		}
	}

	private void updateTicketCountInSelectedTicketsFrom(Ticket updatedTicket) {
		for (Ticket ticket : selectedTickets) {
			if (ticket.code == updatedTicket.code && Strings.equals(ticket.restrictionText, updatedTicket.restrictionText)) {
				ticket.count = updatedTicket.count;
			}
		}
	}
}
