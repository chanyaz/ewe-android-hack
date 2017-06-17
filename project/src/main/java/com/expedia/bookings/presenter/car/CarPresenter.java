package com.expedia.bookings.presenter.car;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.TransitionElement;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.LeftToRightTransition;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.CarConfirmationWidget;
import com.expedia.vm.cars.CarSearchViewModel;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;

public class CarPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;

	public CarPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//@InjectView(R.id.widget_car_params)
	public CarSearchPresenter carSearchPresenter;

	//@InjectView(R.id.car_results_presenter)
	CarResultsPresenter carResultsPresenter;

	//@InjectView(R.id.car_checkout_presenter)
	CarCheckoutPresenter carCheckoutPresenter;

	//@InjectView(R.id.confirmation)
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
		addDefaultTransition(defaultSearchTransition);
		carSearchPresenter.setSearchViewModel(new CarSearchViewModel(getContext()));
		show(carSearchPresenter);
		carResultsPresenter.setVisibility(INVISIBLE);
		carSearchPresenter.getSearchViewModel().getSearchParamsObservable().subscribe(carSearchParamsObserver);
		carResultsPresenter.locationDescriptionSubject.subscribe(carSearchPresenter.getSearchViewModel().getFormattedOriginObservable());
	}

	private Observer<CarSearchParam> carSearchParamsObserver = new Observer<CarSearchParam>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(CarSearchParam params) {
			Events.post(new Events.CarsNewSearchParams(params));
		}
	};

	TransitionElement searchBackgroundColor = new TransitionElement(ContextCompat.getColor(getContext(), R.color.search_anim_background), Color.TRANSPARENT);
	ArgbEvaluator searchArgbEvaluator = new ArgbEvaluator();

	private DefaultTransition defaultSearchTransition = new Presenter.DefaultTransition(CarSearchPresenter.class.getName()) {
		@Override
		public void endTransition(boolean forward) {
			carSearchPresenter.setVisibility(VISIBLE);
			AccessibilityUtil.delayFocusToToolbarNavigationIcon(carSearchPresenter.getToolbar(), 300);
		}
	};

	public void showSuggestionState() {
		carSearchPresenter.showSuggestionState(true);
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
		}
	};

	private Transition showParamsOverlay = new Transition(CarResultsPresenter.class,
		ParamsOverlayState.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carResultsPresenter.animationStart();
			carSearchPresenter.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, forward);
			setBackgroundColorForSearchWidget(f, forward);
			carSearchPresenter.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			carResultsPresenter.setVisibility(forward ? GONE : VISIBLE);
			carSearchPresenter.setVisibility(forward ? VISIBLE : GONE);
			carResultsPresenter.animationFinalize();
			carSearchPresenter.animationFinalize(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(carSearchPresenter.getToolbar());
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(carResultsPresenter.toolbar);
			}
		}
	};

	private Transition searchToResults = new Transition(CarSearchPresenter.class,
		CarResultsPresenter.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			carResultsPresenter.setVisibility(VISIBLE);
			carSearchPresenter.setVisibility(VISIBLE);
			carResultsPresenter.animationStart();
			carSearchPresenter.animationStart(!forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			carResultsPresenter.animationUpdate(f, !forward);
			setBackgroundColorForSearchWidget(f, forward);
			carSearchPresenter.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
			carResultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			carSearchPresenter.setVisibility(forward ? GONE : VISIBLE);
			carResultsPresenter.animationFinalize();
			carSearchPresenter.animationFinalize(!forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(carResultsPresenter.toolbar);
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(carSearchPresenter.getToolbar());
			}
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
		show(carSearchPresenter, FLAG_CLEAR_TOP);
	}

	public void setBackgroundColorForSearchWidget(float f, boolean forward) {
		if (!forward) {
			carSearchPresenter.setBackgroundColor((Integer) (searchArgbEvaluator
				.evaluate(f, searchBackgroundColor.getStart(), searchBackgroundColor.getEnd())));
		}
		else {
			carSearchPresenter.setBackgroundColor(((Integer) searchArgbEvaluator
				.evaluate(f, searchBackgroundColor.getEnd(), searchBackgroundColor.getStart())));
		}
	}
}
