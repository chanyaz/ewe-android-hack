package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXOffersListWidget extends android.widget.LinearLayout {
	private boolean isGroundTransport;
	private String activityId;
	private String promoDiscountType;
	private String activityDiscountType;

	public LXOffersListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Inject
	LXState lxState;

	@InjectView(R.id.offer_show_more_container)
	android.widget.LinearLayout showMoreContainer;

	@InjectView(R.id.offers_container)
	android.widget.LinearLayout offerContainer;

	@InjectView(R.id.show_more_widget)
	ShowMoreWithCountWidget showMoreWithCountWidget;

	private LXOffersListAdapter adapter = new LXOffersListAdapter();
	private List<Offer> availableOffers;

	private int offersListInitialMaxCount;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
		offersListInitialMaxCount = this.getResources().getInteger(R.integer.lx_offers_list_initial_size);
		adapter.offerClickedSubject.subscribe(this::trackLXDetails);
	}

	private void trackLXDetails(Offer offer) {
		LocalDate availabilityDate = ApiDateUtils
			.yyyyMMddHHmmssToLocalDate(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		String lowestTicketAmount = offer.availabilityInfoOfSelectedDate.getLowestTicket().money.getAmount()
			.toString();

		for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
			if (ticket.code == LXTicketType.Adult) {
				lowestTicketAmount = ticket.money.getAmount().toString();
				break;
			}
		}
		if (lxState.activity != null) {
			AdTracker.trackLXDetails(lxState.activity.id, lxState.activity.destination, availabilityDate,
				lxState.activity.regionId, lxState.activity.price.currencyCode, lowestTicketAmount);
		}
	}

	public void setOffers(List<Offer> offers, LocalDate dateSelected) {

		availableOffers = new ArrayList<>();
		for (Offer offer : offers) {
			if (offer.updateAvailabilityInfoOfSelectedDate(dateSelected) != null) {
				availableOffers.add(offer);
			}
		}
		adapter.setOffers(sortTicketByPriorityAndOfferByPrice(availableOffers), isGroundTransport, activityId, promoDiscountType, activityDiscountType);

		offerContainer.removeAllViews();

		for (int position = 0; position < Math.min(offersListInitialMaxCount, availableOffers.size()); position++) {
			View offerRow = adapter.getView(position, null, this);
			offerContainer.addView(offerRow);
		}
		setShowMore();
	}

	private void setShowMore() {
		if (availableOffers.size() > offersListInitialMaxCount) {
			showMoreContainer.setVisibility(VISIBLE);
			showMoreWithCountWidget.setCount(String.valueOf(availableOffers.size() - offersListInitialMaxCount));
		}
		else {
			showMoreContainer.setVisibility(GONE);
		}
	}

	@OnClick((R.id.offer_show_more_container))
	public void onShowMoreClicked() {
		for (int position = offersListInitialMaxCount; position < availableOffers.size(); position++) {
			View offerRow = adapter.getView(position, null, this);
			offerContainer.addView(offerRow);
		}
		showMoreContainer.setVisibility(GONE);
	}

	public LinearLayout getOfferContainer() {
		return offerContainer;
	}

	public List<Offer> sortTicketByPriorityAndOfferByPrice(List<Offer> availableOffers) {
		Collections.sort(availableOffers, new Comparator<Offer>() {
			@Override
			public int compare(Offer lhs, Offer rhs) {
				Collections.sort(lhs.availabilityInfoOfSelectedDate.tickets);
				Collections.sort(rhs.availabilityInfoOfSelectedDate.tickets);
				return lhs.availabilityInfoOfSelectedDate.tickets.get(0).money
						.compareTo(rhs.availabilityInfoOfSelectedDate.tickets.get(0).money);
			}
		});
		return availableOffers;
	}

	public void setIsFromGroundTransport(boolean isGroundTransport) {
		this.isGroundTransport = isGroundTransport;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public void setPromoDiscountType(String promoDiscountType) {
		this.promoDiscountType = promoDiscountType;
	}

	public void setActivityDiscountType(String activityDiscountType) {
		this.activityDiscountType = activityDiscountType;
	}
}
