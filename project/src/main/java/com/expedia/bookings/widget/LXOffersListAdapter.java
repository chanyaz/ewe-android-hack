package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.AvailabilityInfo;
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
	private LocalDate dateSelected;

	public void setOffers(List<Offer> availableOffers, LocalDate dateSelected) {
		this.offers = availableOffers;
		this.dateSelected = dateSelected;
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
		viewHolder.bind(offer, dateSelected);
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

		public void bind(Offer offer, LocalDate dateSelected) {
			this.offerId = offer.id;
			AvailabilityInfo availabilityInfoForSelectedDate = offer
				.getAvailabilityInfoOnDate(dateSelected);

			List<String> priceSummaries = new ArrayList<String>();
			ticketSelectionWidget.setOfferId(offerId);
			ticketSelectionWidget.setOfferTitle(offer.title);
			ticketSelectionWidget.setCurrencySymbol(offer.currencySymbol);

			if (availabilityInfoForSelectedDate != null) {
				for (Ticket ticket : availabilityInfoForSelectedDate.tickets) {
					priceSummaries.add(String.format("%s %s", ticket.price, ticket.name));
				}
				String priceSummaryText = Strings.joinWithoutEmpties(", ", priceSummaries);

				priceSummary.setText(priceSummaryText);
				ticketSelectionWidget.buildTicketPickers(availabilityInfoForSelectedDate);
			}
			else {
				selectTickets.setEnabled(false);
			}

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
