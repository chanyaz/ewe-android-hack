package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Offer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.subjects.PublishSubject;

public class LXOffersListWidget extends android.widget.LinearLayout {
	private boolean isGroundTransport;

	public LXOffersListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.offer_show_more_container)
	android.widget.LinearLayout showMoreContainer;

	@InjectView(R.id.offers_container)
	android.widget.LinearLayout offerContainer;

	@InjectView(R.id.show_more_widget)
	ShowMoreWithCountWidget showMoreWithCountWidget;

	private PublishSubject<Offer> lxOfferSubject = PublishSubject.create();
	private LXOffersListAdapter adapter = new LXOffersListAdapter();
	private List<Offer> availableOffers;

	private int offersListInitialMaxCount;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		offersListInitialMaxCount = this.getResources().getInteger(R.integer.lx_offers_list_initial_size);
	}

	public void setOffers(List<Offer> offers, LocalDate dateSelected) {

		availableOffers = new ArrayList<>();
		for (Offer offer : offers) {
			if (offer.updateAvailabilityInfoOfSelectedDate(dateSelected) != null) {
				availableOffers.add(offer);
			}
		}
		adapter.setOffers(sortTicketByPriorityAndOfferByPrice(availableOffers), lxOfferSubject, isGroundTransport);

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

	public PublishSubject<Offer> getOfferPublishSubject() {
		return lxOfferSubject;
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
}
