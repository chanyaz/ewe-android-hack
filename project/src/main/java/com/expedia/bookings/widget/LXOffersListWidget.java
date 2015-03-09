package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.data.lx.Offer;

public class LXOffersListWidget extends LinearLayout {
	public LXOffersListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private LXOffersListAdapter adapter = new LXOffersListAdapter();
	private List<Offer> availableOffers;

	public void setOffers(List<Offer> offers, LocalDate dateSelected) {

		availableOffers = new ArrayList<>();
		for (Offer offer : offers) {
			if (offer.getAvailabilityInfoOfSelectedDate(dateSelected) != null) {
				availableOffers.add(offer);
			}
		}

		adapter.setOffers(availableOffers);

		this.removeAllViews();
		for (int position = 0; position < availableOffers.size(); position++) {
			View offerRow = adapter.getView(position, null, this);
			this.addView(offerRow);
		}
	}
}
