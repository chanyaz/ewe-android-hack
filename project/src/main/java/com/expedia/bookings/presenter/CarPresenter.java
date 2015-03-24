package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

public class CarPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;

	public CarPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.widget_car_params)
	CarSearchPresenter carSearchPresenter;

	@InjectView(R.id.car_results_presenter)
	CarResultsPresenter carResultsPresenter;

	@InjectView(R.id.car_checkout_presenter)
	CarCheckoutPresenter carCheckoutPresenter;

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

	private Transition resultsToCheckout = new VisibilityTransition(this, CarResultsPresenter.class,
		CarCheckoutPresenter.class);
	private Transition checkoutToSearch = new VisibilityTransition(this, CarCheckoutPresenter.class,
		CarSearchPresenter.class) {
		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			carSearchPresenter.animationFinalize(forward);
		}
	};

	private Transition showParamsOverlay = new Transition(CarResultsPresenter.class,
		ParamsOverlayState.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carResultsPresenter.animationStart(forward);
			carSearchPresenter.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, forward);
			carSearchPresenter.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(forward ? VISIBLE : GONE);
			carResultsPresenter.animationFinalize(forward);
			carSearchPresenter.animationFinalize(forward);
		}
	};

	private Transition searchToResults = new Transition(CarSearchPresenter.class,
		CarResultsPresenter.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carResultsPresenter.animationStart(!forward);
			carSearchPresenter.animationStart(!forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, !forward);
			carSearchPresenter.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			carResultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			carSearchPresenter.setVisibility(forward ? GONE : VISIBLE);
			carResultsPresenter.animationFinalize(!forward);
			carSearchPresenter.animationFinalize(!forward);
		}
	};

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(carResultsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(carCheckoutPresenter);
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		show(carCheckoutPresenter, FLAG_CLEAR_BACKSTACK);
	}

	@Subscribe
	public void onShowParamsOverlay(Events.CarsGoToOverlay event) {
		show(new ParamsOverlayState());
		OmnitureTracking.trackAppCarSearchBox(getContext());
	}

	@Subscribe
	public void onShowSearchWidget(Events.CarsGoToSearch event) {
		show(carSearchPresenter, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}
}
