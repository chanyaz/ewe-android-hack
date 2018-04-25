package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.squareup.phrase.Phrase;

import java.math.BigDecimal;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXTicketPicker extends LinearLayout {

	@InjectView(R.id.traveler_type)
	TextView travelerTypeView;

	@InjectView(R.id.original_price)
	TextView originalPriceView;

	@InjectView(R.id.actual_price)
	TextView actualPriceView;

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
		if (ticket.prices != null) {
			ticket.count = LXDataUtils.incrementTicketCountForVolumePricing(ticket.count, ticket.prices);
		}
		else {
			ticket.count++;
		}
		announceForAccessibility(Phrase.from(getContext(), R.string.lx_ticket_added_announce_accessibility_TEMPLATE)
				.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
				.format());
		bind(ticket, offerId, ticket.count, isGroundTransport);
	}

	@OnClick(R.id.ticket_remove)
	public void onRemoveTicket() {
		trackAddOrRemove("Remove.");
		if (ticket.prices != null) {
			ticket.count = LXDataUtils.decrementTicketCountForVolumePricing(ticket.count, ticket.prices);
		}
		else {
			ticket.count--;
		}
		announceForAccessibility(Phrase.from(getContext(), R.string.lx_ticket_removed_announce_accessibility_TEMPLATE)
				.put("traveler", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
				.format());
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
		HashMap<String, Money> moneyMap = LXDataUtils.getPriceMoneyMap(ticket, defaultCount);
		Money perTicketPrice = moneyMap.get("perTicketPrice");
		Money perTicketOriginalPrice = moneyMap.get("perTicketOriginalPrice");
		String priceContentDescr;
		if (perTicketPrice == null) {
			perTicketPrice = ticket.prices.get(0).money;
			perTicketOriginalPrice = ticket.prices.get(0).originalPriceMoney;
			defaultCount = ticket.prices.get(0).travellerNum;
		}

		if (Strings.isNotEmpty(ticket.restrictionText)) {
			ticketDetailsText = Phrase.from(this, R.string.ticket_details_new_TEMPLATE)
					.put("traveler_type", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
					.put("restriction_text", ticket.restrictionText)
					.format()
					.toString();
		}
		else {
			ticketDetailsText = Phrase.from(this, R.string.ticket_details_no_restriction_new_TEMPLATE)
					.put("traveler_type", LXDataUtils.ticketDisplayName(getContext(), ticket.code))
					.format()
					.toString();
		}
		travelerTypeView.setText(ticketDetailsText);
		if (!perTicketOriginalPrice.getAmount().equals(BigDecimal.ZERO)) {
			originalPriceView.setText(HtmlCompat.fromHtml(
					getContext().getString(R.string.strike_template,
							perTicketOriginalPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)),
					null,
					new StrikethroughTagHandler()));
			originalPriceView.setVisibility(View.VISIBLE);
			priceContentDescr = Phrase.from(getContext(), R.string.lx_total_price_description_TEMPLATE)
					.put("original_price", perTicketOriginalPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
					.put("activity_price", perTicketPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
					.format()
					.toString();
		}
		else {
			originalPriceView.setVisibility(View.GONE);
			priceContentDescr = Phrase.from(getContext(), R.string.activity_price_without_discount_cont_desc_TEMPLATE)
					.put("activity_price", perTicketPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
					.format()
					.toString();
		}
		actualPriceView.setText(perTicketPrice.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
		actualPriceView.setContentDescription(priceContentDescr);
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
			ticketAdd.setEnabled(true);
			setViewDisableProperty(ticketRemove);
		}
		else if (ticket.prices != null && ticket.count == ticket.prices.get(ticket.prices.size() - 1).travellerNum) {
			ticketRemove.setEnabled(true);
			setViewDisableProperty(ticketAdd);
		}
		else if (ticket.count == MAX_TICKET_COUNT) {
			ticketRemove.setEnabled(true);
			setViewDisableProperty(ticketAdd);
		}
		else {
			ticketAdd.setEnabled(true);
			ticketRemove.setEnabled(true);
		}
		Events.post(new Events.LXTicketCountChanged(ticket, offerId));
	}

	private void setViewDisableProperty(ImageButton imageButton) {
		imageButton.setEnabled(false);
		imageButton.setColorFilter(disabledTicketSelectorColor,
			PorterDuff.Mode.SRC_IN);
	}
}
