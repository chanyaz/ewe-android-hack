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

	@OnClick(R.id.ticket_add)
	public void onAddTicket() {
		trackAddOrRemove("Add.");
		ticket.count++;
		setTicketCount();
	}

	@OnClick(R.id.ticket_remove)
	public void onRemoveTicket() {
		trackAddOrRemove("Remove.");
		ticket.count--;
		setTicketCount();
	}

	public void trackAddOrRemove(String type) {
		//  Track Link to track Add/Remove Ticket.
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(ticket.code.toString());
		OmnitureTracking.trackLinkLXAddRemoveTicket(sb.toString());
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

	public void bind(Ticket ticket, String offerId, int defaultCount) {
		this.ticket = ticket;
		this.offerId = offerId;
		String ticketDetailsText = null;
		if (Strings.isNotEmpty(ticket.restrictionText)) {
			ticketDetailsText = String
				.format(getResources().getString(R.string.ticket_details_template), ticket.money.getFormattedMoney(
						Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL),
					LXDataUtils.ticketDisplayName(getContext(), ticket.code), ticket.restrictionText);
		}
		else {
			ticketDetailsText = String
				.format(getResources().getString(R.string.ticket_details_no_restriction_TEMPLATE),
					ticket.money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL),
					LXDataUtils.ticketDisplayName(getContext(), ticket.code));
		}
		ticketDetails.setText(ticketDetailsText);
		ticket.count = defaultCount;

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
