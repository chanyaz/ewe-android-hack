package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXOffersListAdapter extends BaseAdapter {
	//List of Offers for an Activity
	private List<Offer> offers = new ArrayList<>();

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
		for (int i = 0; i < offers.size(); i++) {
			Offer offer = offers.get(i);
			offer.isToggled = i == 0;
		}
	}

	@Override
	public int getCount() {
		return offers.size();
	}

	@Override
	public Offer getItem(int position) {
		return offers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Offer offer = getItem(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = initializeViewHolder(parent);
		}

		viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.bind(offer);
		return convertView;
	}

	protected View initializeViewHolder(ViewGroup parent) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_lx_offer_row, parent, false);
		ViewHolder viewHolder = new ViewHolder(convertView);
		convertView.setTag(viewHolder);

		return convertView;
	}

	public static class ViewHolder implements View.OnClickListener {

		private Offer offer;

		private View itemView;

		public ViewHolder(View itemView) {
			this.itemView = itemView;
			ButterKnife.inject(this, itemView);
			Events.register(this);
			itemView.setOnClickListener(this);
		}

		@InjectView(R.id.offer_title)
		TextView offerTitle;

		@InjectView(R.id.price_summary)
		TextView priceSummary;

		@InjectView(R.id.select_tickets)
		Button selectTickets;

		@InjectView(R.id.offer_row)
		View offerRow;

		@InjectView(R.id.offer_tickets_picker)
		LXTicketSelectionWidget ticketSelectionWidget;

		@InjectView(R.id.lx_book_now)
		Button bookNow;

		@OnClick(R.id.select_tickets)
		public void offerExpanded() {
			Events.post(new Events.LXOfferExpanded(offer));
		}

		@OnClick(R.id.lx_book_now)
		public void offerBooked() {
			Events.post(new Events.LXOfferBooked(offer, ticketSelectionWidget.getSelectedTickets()));
		}

		public void bind(final Offer offer) {
			this.offer = offer;

			FontCache.setTypeface(selectTickets, FontCache.Font.ROBOTO_REGULAR);
			FontCache.setTypeface(bookNow, FontCache.Font.ROBOTO_REGULAR);

			List<String> priceSummaries = new ArrayList<String>();
			ticketSelectionWidget.setOfferId(offer.id);
			ticketSelectionWidget.setOfferTitle(offer.title);
			ticketSelectionWidget.setFreeCancellation(offer.freeCancellation);

			for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
				priceSummaries.add(String.format("%s %s",
					ticket.money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL),
					LXDataUtils.ticketDisplayName(itemView.getContext(), ticket.code)));
			}
			String priceSummaryText = Strings.joinWithoutEmpties(", ", priceSummaries);
			priceSummary.setText(priceSummaryText);
			ticketSelectionWidget.buildTicketPickers(offer.availabilityInfoOfSelectedDate);

			offerTitle.setText(offer.title);
			updateState(offer.isToggled);
		}

		@Subscribe
		public void onOfferExpanded(Events.LXOfferExpanded event) {
			if (this.offer.id.equals(event.offer.id)) {
				if (!offer.isToggled) {
					//  Track Link to track Ticket Selected.
					OmnitureTracking.trackLinkLXSelectTicket(itemView.getContext());
				}
				offer.isToggled = true;
				offerRow.setVisibility(View.GONE);
				ticketSelectionWidget.setVisibility(View.VISIBLE);
			}
			else {
				offer.isToggled = false;
				offerRow.setVisibility(View.VISIBLE);
				ticketSelectionWidget.setVisibility(View.GONE);
			}
			itemView.setClickable(!offer.isToggled);
		}
		public void updateState(boolean isToggled) {
			offerRow.setVisibility(isToggled ? View.GONE : View.VISIBLE);
			ticketSelectionWidget.setVisibility(isToggled ? View.VISIBLE : View.GONE);
			itemView.setClickable(!offer.isToggled);
		}

		@Override
		public void onClick(View v) {
			Events.post(new Events.LXOfferExpanded(offer));
		}
	}
}
