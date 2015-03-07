package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXTicketPicker extends LinearLayout {

	@InjectView(R.id.ticket_details)
	TextView ticketDetails;

	@InjectView(R.id.ticket_count)
	TextView ticketCount;

	@InjectView(R.id.ticket_add)
	Button ticketAdd;

	@InjectView(R.id.ticket_remove)
	Button ticketRemove;

	private Ticket ticket;
	private int count;
	private String offerId;

	private static final int MIN_TICKET_COUNT = 0;
	private static final int MAX_TICKET_COUNT = 8;

	@OnClick(R.id.ticket_add)
	public void onAddTicket() {
		count++;
		setTicketCount();
	}

	@OnClick(R.id.ticket_remove)
	public void onRemoveTicket() {
		count--;
		setTicketCount();
	}

	public LXTicketPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bind(Ticket ticket, String offerId) {
		this.ticket = ticket;
		this.offerId = offerId;
		String ticketDetailsText = String
			.format(getResources().getString(R.string.ticket_details_template), ticket.price,
				ticket.code, ticket.restrictionText);
		ticketDetails.setText(ticketDetailsText);
		setTicketCount();
	}

	private void setTicketCount() {
		ticketCount.setText(String.valueOf(count));
		// Enable or disable add and remove option for ticket
		if (count == MIN_TICKET_COUNT) {
			ticketRemove.setEnabled(false);
		}
		else if (count == MAX_TICKET_COUNT) {
			ticketAdd.setEnabled(false);
		}
		else {
			ticketAdd.setEnabled(true);
			ticketRemove.setEnabled(true);
		}
		Events.post(new Events.LXTicketCountChanged(ticket, count, offerId));
	}
}
