package com.expedia.bookings.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.LXDataUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.subjects.PublishSubject;

public class LXOffersListAdapter extends BaseAdapter {

	//List of Offers for an Activity
	private List<Offer> offers = new ArrayList<>();
	PublishSubject<Offer> offerClickedSubject = PublishSubject.create();
	private boolean isGroundTransport;
	private String activityId;
	private String promoDiscountType;
	private String activityDiscountType;

	public void setOffers(List<Offer> offers, boolean isGroundTransport, String activityId, String promoDiscountType, String activityDiscountType) {
		this.offers = offers;
		this.activityId = activityId;
		this.promoDiscountType = promoDiscountType;
		this.activityDiscountType = activityDiscountType;
		// If there is only one offer, expand it.
		if (CollectionUtils.isNotEmpty(offers) && offers.size() == 1 ) {
			offers.get(0).isToggled = true;
			offerClickedSubject.onNext(offers.get(0));
			this.isGroundTransport = isGroundTransport;
			OmnitureTracking.trackLinkLXSelectTicket(isGroundTransport);
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
		viewHolder.bind(offer, isGroundTransport, activityId, promoDiscountType, position, activityDiscountType);
		return convertView;
	}

	protected View initializeViewHolder(ViewGroup parent) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_lx_offer_row, parent, false);
		ViewHolder viewHolder = new ViewHolder(convertView);
		convertView.setTag(viewHolder);

		return convertView;
	}

	public class ViewHolder implements View.OnClickListener {

		private Offer offer;

		private View itemView;
		private boolean isGroundTransport;
		private String activityId;
		private String promoDiscountType;
		private int position;
		private String activityDiscountType;

		public ViewHolder(View itemView) {
			this.itemView = itemView;
			ButterKnife.inject(this, itemView);
			Events.register(this);
			itemView.setOnClickListener(this);
		}

		@InjectView(R.id.offer_title)
		TextView offerTitle;

		@InjectView(R.id.select_tickets)
		Button selectTickets;

		@InjectView(R.id.offer_row)
		View offerRow;

		@InjectView(R.id.offer_tickets_picker)
		LXTicketSelectionWidget ticketSelectionWidget;

		@InjectView(R.id.lx_book_now)
		Button bookNow;

		@InjectView(R.id.activity_price_summary_container)
		LinearLayout priceSummaryContainer;

		@OnClick(R.id.select_tickets)
		public void offerExpanded() {
			offerClickedSubject.onNext(offer);
		}

		@OnClick(R.id.lx_book_now)
		public void offerBooked() {
			String promoType = promoDiscountType;
			if (promoDiscountType != null && Constants.LX_AIR_MIP.equals(offer.discountType)) {
				if (!Constants.MOD_PROMO_TYPE.equals(promoDiscountType)) {
					promoType = "MIP";
				}
			}
			else {
				promoType = "NONE";
			}
			OmnitureTracking.trackLXOfferClicked(activityId, offer.id, promoType, position + 1, isGroundTransport);
			Events.post(new Events.LXOfferBooked(offer, ticketSelectionWidget.getSelectedTickets()));
		}

		public void bind(final Offer offer, boolean isGroundTransport, String activityId, String promoDiscountType, int position, String activityDiscountType) {
			this.offer = offer;
			this.isGroundTransport = isGroundTransport;
			this.activityId = activityId;
			this.promoDiscountType = promoDiscountType;
			this.position = position;
			this.activityDiscountType = activityDiscountType;

			FontCache.setTypeface(selectTickets, FontCache.Font.ROBOTO_REGULAR);
			FontCache.setTypeface(bookNow, FontCache.Font.ROBOTO_REGULAR);
			ticketSelectionWidget.bind(offer, isGroundTransport);
			for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
				LXDataUtils.addPriceSummaryRow(itemView.getContext(), priceSummaryContainer, ticket);
				priceSummaryContainer.setVisibility(View.VISIBLE);
			}
			ticketSelectionWidget.buildTicketPickers(offer.availabilityInfoOfSelectedDate);

			offerTitle.setText(offer.title);

			updateState(offer.isToggled);
			offerClickedSubject.subscribe(this::onOfferClicked);
		}

		private void onOfferClicked(Offer clickedOffer) {
			if (this.offer.id.equals(clickedOffer.id)) {
				if (!offer.isToggled) {
					//  Track Link to track Ticket Selected.
					OmnitureTracking.trackLinkLXSelectTicket(isGroundTransport);
				}
				offer.isToggled = true;
				offerRow.setVisibility(View.GONE);
				if (Constants.LX_AIR_MIP.equals(activityDiscountType) && !Constants.LX_AIR_MIP.equals(offer.discountType)) {
					OmnitureTracking.trackLXProductForNonMipMod(activityId, false);
				}
				ticketSelectionWidget.offerDescription.minimizeDescription();
				ticketSelectionWidget.setVisibility(View.VISIBLE);
			}
			else {
				offer.isToggled = false;
				offerRow.setVisibility(View.VISIBLE);
				ticketSelectionWidget.setVisibility(View.GONE);
			}
			itemView.setClickable(!offer.isToggled);
		}

		private void updateState(boolean isToggled) {
			offerRow.setVisibility(isToggled ? View.GONE : View.VISIBLE);
			ticketSelectionWidget.setVisibility(isToggled ? View.VISIBLE : View.GONE);
			itemView.setClickable(!offer.isToggled);
		}

		@Override
		public void onClick(View v) {
			offerClickedSubject.onNext(offer);
		}
	}
}
