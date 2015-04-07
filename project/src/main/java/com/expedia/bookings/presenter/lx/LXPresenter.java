package com.expedia.bookings.presenter.lx;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

public class LXPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;

	public LXPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.search_params_widget)
	LXSearchParamsPresenter searchParamsWidget;

	@InjectView(R.id.search_list_presenter)
	LXResultsPresenter resultsPresenter;

	@InjectView(R.id.activity_details_presenter)
	LXDetailsPresenter detailsPresenter;

	private static class LXParamsOverlay {
		// ignore
	}

	@InjectView(R.id.lx_checkout_presenter)
	LXCheckoutPresenter checkoutPresenter;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		Events.register(this);
		addTransition(searchParamsToResults);
		addTransition(resultsToDetails);
		addTransition(searchOverlayOnResults);
		addTransition(searchOverlayOnDetails);
		addTransition(detailsToCheckout);
		addTransition(detailsToSearch);
		show(searchParamsWidget);
		searchParamsWidget.setVisibility(View.VISIBLE);
	}

	private Transition searchParamsToResults = new Transition(LXSearchParamsPresenter.class,
		LXResultsPresenter.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			resultsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(VISIBLE);
			resultsPresenter.animationStart(!forward);
			searchParamsWidget.animationStart(!forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			resultsPresenter.animationUpdate(f, !forward);
			searchParamsWidget.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			resultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			searchParamsWidget.setVisibility(forward ? GONE : VISIBLE);
			resultsPresenter.animationFinalize(!forward);
			searchParamsWidget.animationFinalize(!forward);
		}
	};

	private Transition detailsToCheckout = new VisibilityTransition(this, LXDetailsPresenter.class,
		LXCheckoutPresenter.class);

	private Transition resultsToDetails = new VisibilityTransition(this, LXResultsPresenter.class.getName(),
		LXDetailsPresenter.class.getName()) {
		@Override
		public void startTransition(boolean forward) {
		}

		@Override
		public void updateTransition(float f, boolean forward) {
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				resultsPresenter.setVisibility(View.GONE);
				detailsPresenter.setVisibility(View.VISIBLE);
			}
			else {
				resultsPresenter.setVisibility(View.VISIBLE);
				detailsPresenter.setVisibility(View.GONE);
				detailsPresenter.cleanup();
			}
		}
	};

	private Transition searchOverlayOnResults = new VisibilityTransition(this, LXResultsPresenter.class.getName(),
		LXParamsOverlay.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				resultsPresenter.setVisibility(View.VISIBLE);
				searchParamsWidget.setVisibility(View.VISIBLE);
				detailsPresenter.setVisibility(View.GONE);
			}
			else {
				resultsPresenter.setVisibility(View.VISIBLE);
				searchParamsWidget.setVisibility(View.GONE);
				detailsPresenter.setVisibility(View.GONE);
			}
		}
	};

	private Transition searchOverlayOnDetails = new VisibilityTransition(this, LXDetailsPresenter.class.getName(),
		LXParamsOverlay.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				detailsPresenter.setVisibility(View.VISIBLE);
				searchParamsWidget.setVisibility(View.VISIBLE);
			}
			else {
				detailsPresenter.setVisibility(View.VISIBLE);
				searchParamsWidget.setVisibility(View.GONE);
			}
		}
	};

	private Transition detailsToSearch = new VisibilityTransition(this, LXDetailsPresenter.class,
		LXSearchParamsPresenter.class);

	@Subscribe
	public void onNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		show(resultsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		show(detailsPresenter);
	}

	@Subscribe
	public void onShowSearchWidget(Events.LXShowSearchWidget event) {
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowParamsOverlayOnResults(Events.LXSearchParamsOverlay event) {
		show(new LXParamsOverlay());
	}

	@Subscribe
	public void onShowCheckout(Events.LXCreateTripSucceeded event) {
		show(checkoutPresenter);
	}
}
