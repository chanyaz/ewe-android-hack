package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarConfirmationWidget extends LinearLayout {

	public CarConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.confirmation_text)
	TextView confirmationText;

	@OnClick(R.id.cars_back_to_search)
	public void goBackToSearch() {
		Events.post(new Events.CarsGoToSearch());
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		bind(event.checkoutResponse);
	}

	public void bind(CarCheckoutResponse response) {
		String text;
		if (response.hasErrors()) {
			text = response.printErrors();
		}
		else {
			text = getResources().getString(R.string.successful_checkout_TEMPLATE, response.newTrip.itineraryNumber);
		}
		confirmationText.setText(text);
	}
}
