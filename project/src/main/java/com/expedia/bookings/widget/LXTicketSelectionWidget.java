package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXRedemptionType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;

public class LXTicketSelectionWidget extends LinearLayout {

	private boolean isGroundTransport;

	public LXTicketSelectionWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.ticket_selectors_container)
	LinearLayout ticketSelectorContainer;

	@InjectView(R.id.selected_ticket_summary)
	TextView ticketSummary;

	@InjectView(R.id.lx_book_now)
	Button bookNow;

	@InjectView(R.id.expanded_offer_title)
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

	@InjectView(R.id.price_summary_container)
	LinearLayout priceSummaryContainer;

	@InjectView(R.id.discount_percentage)
	TextView discountPercentageView;

	@InjectView(R.id.strike_through_price)
	TextView strikeThroughPrice;

	@InjectView(R.id.actual_price)
	TextView actualPrice;

	@Inject
	LXState lxState;

	private List<Ticket> selectedTickets = new ArrayList<>();

	private String offerId;

	private String discountType;


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
		Events.register(this);
	}

	public List<Ticket> getSelectedTickets() {
		return selectedTickets;
	}

	public void bind(Offer offer, boolean isGroundTransport) {
		this.offerId = offer.id;
		this.isGroundTransport = isGroundTransport;
		this.discountType = offer.discountType;

		title.setText(offer.title);

		if (Strings.isNotEmpty(offer.description)) {
			offerDescription.setVisibility(View.VISIBLE);
			offerDescription.bindData(StrUtils.stripHTMLTags(offer.description));
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
			offerDetails.get(index).setText(getContext().getString(R.string.free_cancellation));
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

		if (Constants.LX_AIR_MIP.equals(offer.discountType)) {
			if (Constants.MOD_PROMO_TYPE.equals(lxState.getPromoDiscountType())) {
				LXDataUtils.formatDiscountBadge(getContext(), discountPercentageView, R.color.member_only_tag_bg_color, R.color.member_pricing_text_color);
			}
			else {
				LXDataUtils.formatDiscountBadge(getContext(), discountPercentageView, R.color.air_attach_orange, R.color.white);
			}
		}
		else {
			LXDataUtils.formatDiscountBadge(getContext(), discountPercentageView, R.color.success_green, R.color.white);
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

			ticketPicker.bind(ticket, offerId, defaultCount, isGroundTransport);
		}
	}

	@Subscribe
	public void onTicketCountChanged(Events.LXTicketCountChanged event) {
		// Update only if the event was done by TicketPicker of belonging to this widget.
		if (Strings.isNotEmpty(offerId) && offerId.equals(event.offerId)) {
			updateTicketCountInSelectedTicketsFrom(event.ticket);

			ticketSummaryContainer.setVisibility(LXUtils.getTotalTicketCount(selectedTickets) > 0 ? VISIBLE : GONE);
			ticketSummary.setText(LXDataUtils.ticketsCountSummary(getContext(), selectedTickets));
			ticketSummary.setContentDescription(Phrase.from(getContext(), R.string.lx_ticket_selected_summary_TEMPLATE)
					.put("ticket_summary", LXDataUtils.ticketsCountSummary(getContext(), selectedTickets))
					.format()
					.toString());

			Money originalAmount = LXUtils.getTotalOriginalAmount(selectedTickets);
			Money totalAmount = LXUtils.getTotalAmount(selectedTickets);
			int discountPercentage = LXUtils.getDiscountPercentValue(totalAmount.getAmount(), originalAmount.getAmount());

			if (!originalAmount.getAmount().equals(BigDecimal.ZERO)) {
				strikeThroughPrice.setText(HtmlCompat.fromHtml(
						getContext().getString(R.string.strike_template,
								originalAmount.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)),
						null,
						new StrikethroughTagHandler()));
				strikeThroughPrice.setVisibility(VISIBLE);
			}
			else {
				strikeThroughPrice.setVisibility(GONE);
			}
			actualPrice.setText(totalAmount.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
			String priceSummaryContDescr;

			if (discountPercentage >= Constants.LX_MIN_DISCOUNT_PERCENTAGE) {
				discountPercentageView.setText(Phrase.from(getContext(), R.string.lx_discount_percentage_text_TEMPLATE)
						.put("discount", discountPercentage)
						.format());
				priceSummaryContDescr = Phrase.from(getContext(), R.string.activity_price_with_discount_cont_desc_TEMPLATE)
						.put("activity_price", totalAmount.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
						.put("original_price", originalAmount.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
						.put("discount", discountPercentage)
						.format()
						.toString();
				discountPercentageView.setVisibility(View.VISIBLE);
			}
			else {
				discountPercentageView.setVisibility(View.GONE);
				priceSummaryContDescr = Phrase.from(getContext(), R.string.activity_price_without_discount_cont_desc_TEMPLATE)
						.put("activity_price", totalAmount.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
						.format()
						.toString();
			}
			actualPrice.setContentDescription(priceSummaryContDescr);
			priceSummaryContainer.setVisibility(View.VISIBLE);
		}
	}

	private void updateTicketCountInSelectedTicketsFrom(Ticket updatedTicket) {
		for (Ticket ticket : selectedTickets) {
			if (ticket.code == updatedTicket.code && Strings.equals(ticket.restrictionText, updatedTicket.restrictionText)) {
				ticket.count = updatedTicket.count;
			}
		}
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		if (visibility == VISIBLE) {
			// Once the animateLayoutChanges are done, then only we can give focus to the view.
			AccessibilityUtil.delayedFocusToView(title, 500);
		}
		super.onVisibilityChanged(changedView, visibility);
	}
}
