package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.utils.LXFormatter;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXCheckoutSummaryWidget extends android.widget.LinearLayout {

	public LXCheckoutSummaryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.lx_offer_title_text)
	TextView lxOfferTitleText;

	@InjectView(R.id.lx_group_text)
	TextView lxGroupText;

	@InjectView(R.id.lx_offer_date)
	TextView lxOfferDate;

	@InjectView(R.id.lx_offer_location)
	TextView lxOfferLocation;

	@InjectView(R.id.free_cancellation_text)
	TextView freeCancellationText;

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
	}

	public void bind() {
		lxOfferTitleText.setText(lxState.offer.title);
		lxGroupText.setText(LXFormatter.selectedTicketsSummaryText(getContext(), lxState.selectedTickets));
		lxOfferDate.setText(lxState.offer.availabilityInfoOfSelectedDate.availabilities.displayDate);
		lxOfferLocation.setText(lxState.activity.location);

		String totalMoney = LXUtils.getTotalAmount(lxState.selectedTickets).getFormattedMoney();
		tripTotalText.setText(totalMoney);

		freeCancellationText.setVisibility(lxState.offer.freeCancellation ? VISIBLE : GONE);
	}
}
