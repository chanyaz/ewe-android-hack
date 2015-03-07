package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class LXCheckoutWidget extends CheckoutBasePresenter {

	public LXCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Inject
	LXState lxState;

	LXCheckoutSummaryWidget summaryWidget;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		summaryWidget = Ui.inflate(R.layout.lx_checkout_summary_widget, summaryContainer, false);
		summaryContainer.addView(summaryWidget);
		mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.lx_enter_contact_details));
	}

	@Subscribe
	public void onShowCheckout(Events.LXCreateTripSucceeded event) {
		bind(event.createTripResponse);
	}

	private void bind(LXCreateTripResponse createTripResponse) {
		summaryWidget.bind();
		paymentInfoCardView.setCreditCardRequired(true);
		slideWidget.resetSlider();

		String grandTotal = String.format(getResources().getString(R.string.lx_total_price_with_currency_TEMPLATE),
			lxState.offer.currencySymbol, LXUtils.getTotalAmount(lxState.selectedTickets));
		sliderTotalText.setText(getResources().getString(R.string.your_card_will_be_charged_TEMPLATE, grandTotal));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(View.GONE);
		// TODO Make this LX specific
		legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());
		isCheckoutComplete();
		show(new CheckoutDefault());
	}

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideProgress(float pixels, float total) {
	}

	@Override
	public void onSlideAllTheWay() {
		LXCheckoutParams checkoutParams = new LXCheckoutParams();
		//TODO - Use fluent interface to fill all required params
		Events.post(new Events.LXKickOffCheckoutCall(checkoutParams));
	}

	@Override
	public void onSlideAbort() {
	}
}
