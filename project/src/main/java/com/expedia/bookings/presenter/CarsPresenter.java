package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.CarSearchPresenter;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

public class CarsPresenter extends Presenter {

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.widget_car_params)
	CarSearchPresenter carSearchPresenter;

	@InjectView(R.id.cars_results_presenter)
	CarsResultsPresenter carsResultsPresenter;

	@InjectView(R.id.car_checkout_presenter)
	CarCheckoutPresenter checkoutWidget;

	private static class ParamsOverlayState {
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		addTransition(searchToResults);
		addTransition(resultsToCheckout);
		addTransition(checkoutToSearch);
		addTransition(showParamsOverlay);
		show(carSearchPresenter);
		carSearchPresenter.setVisibility(VISIBLE);
	}

	private Transition resultsToCheckout = new VisibilityTransition(this, CarsResultsPresenter.class,
		CarCheckoutPresenter.class);
	private Transition checkoutToSearch = new VisibilityTransition(this, CarCheckoutPresenter.class,
		CarSearchPresenter.class);

	private Transition showParamsOverlay = new Transition(CarsResultsPresenter.class,
		ParamsOverlayState.class, new DecelerateInterpolator(), 700) {
		@Override
		public void startTransition(boolean forward) {
			carsResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carsResultsPresenter.animationStart(forward);
			carSearchPresenter.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carsResultsPresenter.animationUpdate(f, forward);
			carSearchPresenter.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			carsResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(forward ? VISIBLE : GONE);
			carsResultsPresenter.animationFinalize(forward);
			carSearchPresenter.animationFinalize(forward);
		}
	};

	private Transition searchToResults = new Transition(CarSearchPresenter.class,
		CarsResultsPresenter.class, new DecelerateInterpolator(), 700) {
		@Override
		public void startTransition(boolean forward) {
			carsResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carsResultsPresenter.animationStart(!forward);
			carSearchPresenter.animationStart(!forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carsResultsPresenter.animationUpdate(f, !forward);
			carSearchPresenter.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			carsResultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			carSearchPresenter.setVisibility(forward ? GONE : VISIBLE);
			carsResultsPresenter.animationFinalize(!forward);
			carSearchPresenter.animationFinalize(!forward);
		}
	};

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(carsResultsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(checkoutWidget);
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		show(checkoutWidget, FLAG_CLEAR_BACKSTACK);
	}

	@Subscribe
	public void onShowParamsOverlay(Events.CarsGoToOverlay event) {
		show(new ParamsOverlayState());
	}

	@Subscribe
	public void onShowSearchWidget(Events.CarsGoToSearch event) {
		show(carSearchPresenter, FLAG_CLEAR_BACKSTACK);
	}
}
