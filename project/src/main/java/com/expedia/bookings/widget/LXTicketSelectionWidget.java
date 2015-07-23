package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXRedemptionType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;

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

	@InjectView(R.id.ticket_summary_container)
	LinearLayout ticketSummaryContainer;

	@InjectView(R.id.offer_passengers)
	com.expedia.bookings.widget.TextView offerPassengers;

	@InjectView(R.id.offer_bags)
	com.expedia.bookings.widget.TextView offerBags;

	@InjectViews({ R.id.offer_detail1, R.id.offer_detail2, R.id.offer_detail3 })
	List<com.expedia.bookings.widget.TextView> offerDetails;

	@InjectView(R.id.offer_description)
	LXOfferDescription offerDescription;

	private List<Ticket> selectedTickets = new ArrayList<>();

	private String offerId;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	public List<Ticket> getSelectedTickets() {
		return selectedTickets;
	}

	public void bind(Offer offer) {
		this.offerId = offer.id;
		title.setText(offer.title);

		if (Strings.isNotEmpty(offer.description)) {
			offerDescription.setVisibility(View.VISIBLE);
			offerDescription.bindData(offer.description);
		}
		else {
			offerDescription.setVisibility(View.GONE);
		}

		int index = 0;
		if (Strings.isNotEmpty(offer.duration)) {
			offerDetails.get(index).setText(offer.duration);
			offerDetails.get(index).setVisibility(View.VISIBLE);
			Drawable durationDrawable = getResources().getDrawable(R.drawable.duration).mutate();
			durationDrawable
				.setColorFilter(getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_lxPrimaryColor)),
					PorterDuff.Mode.SRC_IN);
			offerDetails.get(index).setCompoundDrawablesWithIntrinsicBounds(durationDrawable, null, null, null);
			index++;
		}

		if (offer.freeCancellation) {
			offerDetails.get(index).setText(getContext().getString(R.string.lx_free_cancellation));
			offerDetails.get(index).setVisibility(View.VISIBLE);
			Drawable freeCancellationDrawable = getResources().getDrawable(R.drawable.checkmark).mutate();
			freeCancellationDrawable.setColorFilter(getResources().getColor(
				Ui.obtainThemeResID(getContext(), R.attr.skin_lxPrimaryColor)), PorterDuff.Mode.SRC_IN);
			offerDetails.get(index).setCompoundDrawablesWithIntrinsicBounds(freeCancellationDrawable, null, null, null);
			index++;
		}

		if (offer.redemptionType != null) {
			String redemptionText =
				offer.redemptionType.equals(LXRedemptionType.PRINT) ? getResources().getString(
					R.string.lx_print_voucher_offer) : getResources()
					.getString(R.string.lx_voucherless_offer);
			offerDetails.get(index).setText(redemptionText);
			offerDetails.get(index).setVisibility(View.VISIBLE);
			Drawable redemptionDrawable = getResources().getDrawable(R.drawable.printed_receipt).mutate();
			redemptionDrawable
				.setColorFilter(getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_lxPrimaryColor)),
					PorterDuff.Mode.SRC_IN);
			offerDetails.get(index).setCompoundDrawablesWithIntrinsicBounds(redemptionDrawable, null, null, null);
			index++;
		}

		// Reset other offer details section
		for (int i = index; i < offerDetails.size(); i++) {
			offerDetails.get(i).setText("");
			offerDetails.get(i).setVisibility(View.GONE);
		}

		if (offer.isGroundTransport) {
			if (Strings.isNotEmpty(offer.passengers)) {
				offerPassengers.setText(getContext().getString(R.string.lx_ground_transport_passengers_text,
					offer.passengers));
				offerPassengers.setVisibility(View.VISIBLE);
			}
			if (Strings.isNotEmpty(offer.bags)) {
				offerBags.setText(getContext().getString(R.string.lx_ground_transport_bags_text, offer.bags));
				offerBags.setVisibility(View.VISIBLE);
			}
		}

	}

	public void buildTicketPickers(AvailabilityInfo availabilityInfo) {

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
