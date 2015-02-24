package com.expedia.bookings.widget;

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

	public void setOffers(List<Offer> offers, LocalDate dateSelected) {
		adapter.setOffers(offers, dateSelected);

		this.removeAllViews();
		for (int position = 0; position < offers.size(); position++) {
			View offerRow = adapter.getView(position, null, this);
			this.addView(offerRow);
		}
	}
}
