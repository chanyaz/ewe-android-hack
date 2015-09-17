package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.Offer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXOffersListWidget extends android.widget.LinearLayout {
	public LXOffersListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

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
		offersListInitialMaxCount = this.getResources().getInteger(R.integer.lx_offers_list_initial_size);
	}

	public void setOffers(List<Offer> offers, LocalDate dateSelected) {

		availableOffers = new ArrayList<>();
		for (Offer offer : offers) {
			if (offer.updateAvailabilityInfoOfSelectedDate(dateSelected) != null) {
				availableOffers.add(offer);
			}
		}

		adapter.setOffers(availableOffers);

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
	}

	@OnClick((R.id.show_more_widget))
	public void onShowMoreClicked() {
		for (int position = offersListInitialMaxCount; position < availableOffers.size(); position++) {
			View offerRow = adapter.getView(position, null, this);
			offerContainer.addView(offerRow);
		}
		showMoreContainer.setVisibility(GONE);
	}
}
