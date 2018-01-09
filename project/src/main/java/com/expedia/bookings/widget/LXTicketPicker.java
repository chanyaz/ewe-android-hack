package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXTicketPicker extends LinearLayout {

	@InjectView(R.id.ticket_details)
	TextView ticketDetails;

	@InjectView(R.id.ticket_count)
	TextView ticketCount;

	@InjectView(R.id.ticket_add)
	ImageButton ticketAdd;

	@InjectView(R.id.ticket_remove)
	ImageButton ticketRemove;

	private Ticket ticket;
	private String offerId;

	private static final int MIN_TICKET_COUNT = 0;
	private static final int MAX_TICKET_COUNT = 8;

	private int enabledTicketSelectorColor;
	private int disabledTicketSelectorColor;
	private boolean isGroundTransport;

	@OnClick(R.id.ticket_add)
	public void onAddTicket() {
		trackAddOrRemove("Add.");
		ticket.count++;
		announceForAccessibility(Phrase.from(getContext(), R.string.lx_ticket_added_announce_accessibility_TEMPLATE)
				.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
				.format());
		setTicketCount();
		bind(ticket, offerId, ticket.count, isGroundTransport);
	}

	@OnClick(R.id.ticket_remove)
	public void onRemoveTicket() {
		trackAddOrRemove("Remove.");
		ticket.count--;
		announceForAccessibility(Phrase.from(getContext(), R.string.lx_ticket_removed_announce_accessibility_TEMPLATE)
				.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
				.format());
		setTicketCount();
		bind(ticket, offerId, ticket.count, isGroundTransport);
	}

	public void trackAddOrRemove(String type) {
		//  Track Link to track Add/Remove Ticket.
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(ticket.code.toString());
		OmnitureTracking.trackLinkLXAddRemoveTicket(sb.toString(), isGroundTransport);
	}

	public LXTicketPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		enabledTicketSelectorColor = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
		disabledTicketSelectorColor = Ui.obtainThemeColor(getContext(), R.attr.skin_ticketSelectorDisabledColor);
	}

	public void bind(Ticket ticket, String offerId, int defaultCount, boolean isGroundTransport) {
		this.ticket = ticket;
		this.offerId = offerId;
		this.isGroundTransport = isGroundTransport;

		String ticketDetailsText = null;
		Money perTicketPrice = null;
		if (ticket.prices == null) {
			perTicketPrice = ticket.money;
		}
		else if (defaultCount == 0) {
			perTicketPrice = ticket.prices.get(0).money;
		}
		else {
			for (Ticket.LxTicketPrices price : ticket.prices) {
				if (defaultCount == price.travellerNum) {
					perTicketPrice = price.money;
				}
			}
		}
		if (Strings.isNotEmpty(ticket.restrictionText)) {
			ticketDetailsText = String
					.format(getResources().getString(R.string.ticket_details_template), perTicketPrice.getFormattedMoney(
							Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL),
							LXDataUtils.ticketDisplayName(getContext(), ticket.code), ticket.restrictionText);
		}
		else {
			ticketDetailsText = String
				.format(getResources().getString(R.string.ticket_details_no_restriction_TEMPLATE),
						perTicketPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL),
					LXDataUtils.ticketDisplayName(getContext(), ticket.code));
		}
		ticketDetails.setText(ticketDetailsText);
		ticket.count = defaultCount;

		ticketAdd.setContentDescription(Phrase.from(this, R.string.lx_add_ticket_button_cont_desc_TEMPLATE)
			.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
			.format());
		ticketRemove.setContentDescription(Phrase.from(this, R.string.lx_remove_ticket_button_cont_desc_TEMPLATE)
			.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
			.format());

		setTicketCount();
	}

	private void setTicketCount() {
		ticketCount.setText(String.valueOf(ticket.count));
		// Enable or disable add and remove option for ticket
		ticketRemove.setColorFilter(enabledTicketSelectorColor, PorterDuff.Mode.SRC_IN);
		ticketAdd.setColorFilter(enabledTicketSelectorColor, PorterDuff.Mode.SRC_IN);
		if (ticket.count == MIN_TICKET_COUNT) {
			ticketRemove.setEnabled(false);
			ticketRemove.setColorFilter(disabledTicketSelectorColor,
				PorterDuff.Mode.SRC_IN);
		}
		else if (ticket.prices != null && ticket.count == ticket.prices.size()) {
			ticketAdd.setEnabled(false);
			ticketAdd.setColorFilter(disabledTicketSelectorColor, PorterDuff.Mode.SRC_IN);
		}
		else if (ticket.count == MAX_TICKET_COUNT) {
			ticketAdd.setEnabled(false);
			ticketAdd.setColorFilter(disabledTicketSelectorColor, PorterDuff.Mode.SRC_IN);
		}
		else {
			ticketAdd.setEnabled(true);
			ticketRemove.setEnabled(true);
		}
		Events.post(new Events.LXTicketCountChanged(ticket, offerId));
	}
}
