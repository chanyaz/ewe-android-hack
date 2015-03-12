package com.expedia.bookings.widget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
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

	private Map<Ticket, Integer> tickets = new LinkedHashMap<>();

	private String offerId;
	private String offerTitle;
	private String currencySymbol;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	public Map<Ticket, Integer> getTickets() {
		return tickets;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public void setOfferTitle(String offerTitle) {
		this.offerTitle = offerTitle;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public void buildTicketPickers(AvailabilityInfo availabilityInfo) {

		title.setText(offerTitle);

		for (Ticket ticket : availabilityInfo.tickets) {
			LXTicketPicker ticketPicker = Ui.inflate(R.layout.lx_ticket_picker, ticketSelectorContainer, false);
			ticketSelectorContainer.addView(ticketPicker);

			ticketPicker.bind(ticket, offerId);

			// Initialize all ticket types with 0 count.
			tickets.put(ticket, 0);
		}
	}

	@Subscribe
	public void onTicketCountChanged(Events.LXTicketCountChanged event) {
		// Update only if the event was done by TicketPicker of belonging to this widget.
		if (Strings.isNotEmpty(offerId) && offerId.equals(event.offerId)) {
			tickets.put(event.ticket, event.count);
			updateTicketSelection();
		}
	}

	private void updateTicketSelection() {
		BigDecimal total = BigDecimal.ZERO;
		List<String> ticketsSummaryList = new ArrayList<>();
		String ticketSummaryTemplate = getResources().getString(R.string.ticket_summary_type_count_TEMPLATE);

		for (Map.Entry<Ticket, Integer> ticketAndCount : tickets.entrySet()) {
			int ticketCount = ticketAndCount.getValue();
			Ticket ticket = ticketAndCount.getKey();
			ticketsSummaryList.add(String.format(ticketSummaryTemplate, ticketCount, getResources().getString(LXDataUtils.LX_TICKET_TYPE_NAME_MAP.get(ticket.code))));
			total = total.add(ticket.amount.multiply(BigDecimal.valueOf(ticketCount)));
		}

		ticketSummary.setText(Strings.joinWithoutEmpties(", ", ticketsSummaryList));
		bookNow.setText(String.format(getResources().getString(R.string.offer_book_now_TEMPLATE), currencySymbol, total));
	}
}
