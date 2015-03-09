package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXOffersListAdapter extends BaseAdapter {
	private List<Offer> offers = new ArrayList<>();

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
		notifyDataSetChanged();
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

	public static class ViewHolder {

		private String offerId;

		public ViewHolder(View itemView) {
			ButterKnife.inject(this, itemView);
			Events.register(this);
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

		@OnClick(R.id.select_tickets)
		public void offerSelected() {
			Events.post(new Events.LXOfferExpanded(offerId));
		}

		public void bind(Offer offer) {
			this.offerId = offer.id;

			List<String> priceSummaries = new ArrayList<String>();
			ticketSelectionWidget.setOfferId(offerId);
			ticketSelectionWidget.setOfferTitle(offer.title);
			ticketSelectionWidget.setCurrencySymbol(offer.currencySymbol);

			for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
				priceSummaries.add(String.format("%s %s", ticket.price, ticket.name));
			}
			String priceSummaryText = Strings.joinWithoutEmpties(", ", priceSummaries);

			priceSummary.setText(priceSummaryText);
			ticketSelectionWidget.buildTicketPickers(offer.availabilityInfoOfSelectedDate);


			offerTitle.setText(offer.title);
		}

		@Subscribe
		public void onOfferExpanded(Events.LXOfferExpanded event) {
			if (offerId.equals(event.offerId)) {
				offerRow.setVisibility(View.GONE);
				ticketSelectionWidget.setVisibility(View.VISIBLE);
			}
			else {
				offerRow.setVisibility(View.VISIBLE);
				ticketSelectionWidget.setVisibility(View.GONE);
			}
		}
	}
}
