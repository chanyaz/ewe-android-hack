package com.expedia.bookings.presenter.car;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.LeftToRightTransition;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.CarConfirmationWidget;
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

	@InjectView(R.id.confirmation)
	CarConfirmationWidget confirmation;

	private float searchStartingAlpha = 0f;

	private static class ParamsOverlayState {
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		addTransition(searchToResults);
		addTransition(resultsToCheckout);
		addTransition(checkoutToSearch);
		addTransition(showParamsOverlay);
		addTransition(checkoutToConfirmation);
		show(carSearchPresenter);
		carSearchPresenter.setVisibility(VISIBLE);
		carResultsPresenter.setVisibility(INVISIBLE);
	}

	private Transition checkoutToConfirmation = new LeftToRightTransition(this, CarCheckoutPresenter.class, CarConfirmationWidget.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			confirmation.postDelayed(new Runnable() {
				@Override
				public void run() {
					confirmation.setFocusOnToolbarForAccessibility();
				}
			}, 200);
		}
	};

	private Transition resultsToCheckout = new LeftToRightTransition(this, CarResultsPresenter.class, CarCheckoutPresenter.class);

	private Transition checkoutToSearch = new VisibilityTransition(this, CarCheckoutPresenter.class, CarSearchPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			carSearchPresenter.animationFinalize(forward);
			carSearchPresenter.reset();
		}
	};

	private Transition showParamsOverlay = new Transition(CarResultsPresenter.class,
		ParamsOverlayState.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			if (forward) {
				searchStartingAlpha = carResultsPresenter.animationStart();
			}
			else {
				carResultsPresenter.animationStart();
			}
			carSearchPresenter.animationStart(forward, searchStartingAlpha);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, forward);
			carSearchPresenter.animationUpdate(f, forward, searchStartingAlpha);
		}

		@Override
		public void endTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(forward ? VISIBLE : GONE);
			carResultsPresenter.animationFinalize();
			carSearchPresenter.animationFinalize(forward);
		}
	};

	private Transition searchToResults = new Transition(CarSearchPresenter.class,
		CarResultsPresenter.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carResultsPresenter.animationStart();
			carSearchPresenter.animationStart(!forward, 1f);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, !forward);
			carSearchPresenter.animationUpdate(f, !forward, 1f);
		}

		@Override
		public void endTransition(boolean forward) {
			carResultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			carSearchPresenter.setVisibility(forward ? GONE : VISIBLE);
			if (forward) {
				// making sure tool tip is hidden in case of deep link
				// the reason we are adding delay is we are showing tool tip with 50 ms delay
				// TODO Update CalendarPicker.hideToolTip() in such way it cancels 50 ms "show" animation if we are trying to hide it
				new Handler().postDelayed(new Runnable() {
					public void run() {
						carSearchPresenter.calendarContainer.hideToolTip();
					}
				}, 300);

			}
			carResultsPresenter.animationFinalize();
			carSearchPresenter.animationFinalize(!forward);
		}
	};

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(carResultsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		Db.getTripBucket().clearCars();
		show(carCheckoutPresenter);
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		show(confirmation, FLAG_CLEAR_BACKSTACK);
	}

	@Subscribe
	public void onShowParamsOverlay(Events.CarsGoToOverlay event) {
		show(new ParamsOverlayState());
		OmnitureTracking.trackAppCarSearchBox();
	}

	@Subscribe
	public void onShowSearchWidget(Events.CarsGoToSearch event) {
		carSearchPresenter.calendarContainer.hideToolTip();
		show(carSearchPresenter, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}
}
