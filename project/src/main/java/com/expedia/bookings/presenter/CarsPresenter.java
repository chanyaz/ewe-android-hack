package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
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
		addTransition(paramsToResults);
		addTransition(resultsToCheckout);
		addTransition(checkoutToSearch);
		addTransition(resultsToSearch);
		show(widgetCarParams);
		widgetCarParams.setVisibility(View.VISIBLE);
	}

	private Transition paramsToResults = new VisibilityTransition(this, CarSearchParamsWidget.class.getName(), CarsResultsPresenter.class.getName());
	private Transition resultsToCheckout = new VisibilityTransition(this, CarsResultsPresenter.class.getName(), CarCheckoutPresenter.class.getName());
	private Transition checkoutToSearch = new VisibilityTransition(this, CarCheckoutPresenter.class.getName(), CarSearchParamsWidget.class.getName());
	private Transition resultsToSearch = new AddTransition(this, CarsResultsPresenter.class.getName(), CarSearchParamsWidget.class.getName());

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
		// TODO: don't hide the other views, show search params widget over them with an alpha
		show(widgetCarParams, true);
	}
}
