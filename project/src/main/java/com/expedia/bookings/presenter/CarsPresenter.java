package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.CarSearchParamsWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

public class CarsPresenter extends Presenter {

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.widget_car_params)
	CarSearchParamsWidget widgetCarParams;

	@InjectView(R.id.cars_results_presenter)
	CarsResultsPresenter carsResultsPresenter;

	@InjectView(R.id.car_checkout_presenter)
	CarCheckoutPresenter checkoutWidget;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		Log.d("carspresenterstuff", (new Object()).getClass().getName());
		Log.d("carspresenterstuff", (new String()).getClass().getName());
		Log.d("carspresenterstuff", (Object.class.getName()));
		Log.d("carspresenterstuff", (String.class.getName()));
		show(widgetCarParams);
		widgetCarParams.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(carsResultsPresenter);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(checkoutWidget);
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		show(checkoutWidget, true);
	}

	@Subscribe
	public void onShowSearch(Events.CarsGoToSearch event) {
		show(widgetCarParams, true);
	}
}
