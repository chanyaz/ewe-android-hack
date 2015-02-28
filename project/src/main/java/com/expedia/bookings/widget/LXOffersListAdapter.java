package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.Strings;

import butterknife.ButterKnife;
import butterknife.InjectView;

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

		View itemView;

		public ViewHolder(View itemView) {
			this.itemView = itemView;
			ButterKnife.inject(this, itemView);
		}

		@InjectView(R.id.offer_title)
		TextView offerTitle;

		@InjectView(R.id.price_summary)
		TextView priceSummary;

		@InjectView(R.id.select_tickets)
		Button selectTickets;

		public void bind(Offer offer, LocalDate dateSelected) {
			AvailabilityInfo availabilityInfoForSelectedDate = offer
				.getAvailabilityInfoOnDate(dateSelected);

			List<String> priceSummaries = new ArrayList<String>();

			if (availabilityInfoForSelectedDate != null) {
				for (Ticket ticket : availabilityInfoForSelectedDate.tickets) {
					priceSummaries.add(String.format("%s %s", ticket.price, ticket.name));
				}
				String priceSummaryText = Strings.joinWithoutEmpties(", ", priceSummaries);

				priceSummary.setText(priceSummaryText);
			}

			offerTitle.setText(offer.title);
		}
	}
}
