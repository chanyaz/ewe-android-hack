package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXTicketSelected;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXCheckoutSummaryWidget extends RelativeLayout {

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
		lxGroupText.setText(offerGroupText(lxState.offerSelected));
		lxOfferDate.setText(lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		lxOfferLocation.setText(lxState.activity.location);

		String grandTotal = String.format(getResources().getString(R.string.lx_total_price_with_currency_TEMPLATE),
			lxState.offer.currencySymbol, lxState.offerSelected.amount);
		tripTotalText.setText(grandTotal);

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.ic_action_bar_checkmark_white);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.lx_checkmark_color), PorterDuff.Mode.SRC_IN);
		freeCancellationText.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
	}

	private String offerGroupText(LXOfferSelected offerSelected) {
		List<String> ageCategories = new ArrayList<String>();
		if (offerSelected != null) {
			for (LXTicketSelected ticketSelected : offerSelected.tickets) {
				//TODO - Need to localize
				ageCategories.add(String.format("%d %s", ticketSelected.count, ticketSelected.code));
			}
			return Strings.joinWithoutEmpties(", ", ageCategories);
		}

		return "";
	}
}
