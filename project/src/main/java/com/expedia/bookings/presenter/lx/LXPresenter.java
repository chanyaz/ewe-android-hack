package com.expedia.bookings.presenter.lx;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXConfirmationWidget;
import com.expedia.bookings.widget.LXLoadingOverlayWidget;
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

	@InjectView(R.id.lx_loading_overlay)
	LXLoadingOverlayWidget loadingOverlay;

	@InjectView(R.id.confirmation)
	LXConfirmationWidget confirmationWidget;

	private float searchStartingAlpha;

	private static class LXParamsOverlay {
		// ignore
	}

	@InjectView(R.id.lx_checkout_presenter)
	LXCheckoutPresenter checkoutPresenter;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		addTransition(searchParamsToResults);
		addTransition(resultsToDetails);
		addTransition(searchOverlayOnResults);
		addTransition(searchOverlayOnDetails);
		addTransition(detailsToCheckout);
		addTransition(detailsToSearch);
		addTransition(checkoutToConfirmation);
		addTransition(checkoutToResults);
		show(resultsPresenter);
		resultsPresenter.setVisibility(VISIBLE);

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
			searchParamsWidget.animationUpdate(f, !forward, 1f);
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

	private Transition detailsToCheckout = new VisibilityTransition(this, LXDetailsPresenter.class, LXCheckoutPresenter.class);

	private Presenter.Transition resultsToDetails = new Presenter.Transition(LXResultsPresenter.class.getName(),
		LXDetailsPresenter.class.getName(),
		new DecelerateInterpolator(), ANIMATION_DURATION) {
		private int detailsHeight;

		@Override
		public void startTransition(boolean forward) {
			final int parentHeight = getHeight();
			detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext());
			float pos = forward ? parentHeight + detailsHeight : detailsHeight;
			detailsPresenter.setTranslationY(pos);
			detailsPresenter.setVisibility(View.VISIBLE);
			detailsPresenter.animationStart(!forward);
			resultsPresenter.setVisibility(VISIBLE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float pos = forward ? detailsHeight + (-f * detailsHeight) : (f * detailsHeight);
			detailsPresenter.setTranslationY(pos);
			detailsPresenter.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
			detailsPresenter.setTranslationY(forward ? 0 : detailsHeight);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			detailsPresenter.setTranslationY(forward ? 0 : detailsHeight);
			detailsPresenter.setVisibility(forward ? VISIBLE : GONE);
			resultsPresenter.setVisibility(forward ? GONE : VISIBLE);
			loadingOverlay.setVisibility(GONE);
			detailsPresenter.animationFinalize(!forward);
			if (!forward) {
				detailsPresenter.cleanup();
			}
		}
	};

	private Transition searchOverlayOnResults = new Transition(LXResultsPresenter.class,
	LXParamsOverlay.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			resultsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(VISIBLE);
			detailsPresenter.setVisibility(View.GONE);
			resultsPresenter.animationStart(forward);
			searchParamsWidget.animationStart(forward);
		}
	
		@Override
		public void updateTransition(float f, boolean forward) {
			resultsPresenter.animationUpdate(f, forward);
			searchParamsWidget.animationUpdate(f, forward, 1f);
		}
	
		@Override
		public void endTransition(boolean forward) {
		}
	
		@Override
		public void finalizeTransition(boolean forward) {
			resultsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			detailsPresenter.setVisibility(View.GONE);
			resultsPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
		}
	};

	private Transition searchOverlayOnDetails = new Transition(LXDetailsPresenter.class,
		LXParamsOverlay.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			detailsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(VISIBLE);
			if (forward) {
				searchStartingAlpha = detailsPresenter.animationStart(forward);
			}
			else {
				detailsPresenter.animationStart(forward);
			}
			searchParamsWidget.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			detailsPresenter.animationUpdate(f, forward);
			searchParamsWidget.animationUpdate(f, forward, searchStartingAlpha);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			detailsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			detailsPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
		}
	};

	private Transition detailsToSearch = new VisibilityTransition(this, LXDetailsPresenter.class, LXSearchParamsPresenter.class);

	private Transition checkoutToConfirmation = new VisibilityTransition(this, LXCheckoutPresenter.class, LXConfirmationWidget.class);

	private Transition checkoutToResults = new VisibilityTransition(this, LXCheckoutPresenter.class, LXResultsPresenter.class);

	@Subscribe
	public void onNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		show(resultsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onNewSearch(Events.LXNewSearch event) {
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK);
	}

	@Subscribe
	public void onActivitySelected(Events.LXActivitySelected event) {
		loadingOverlay.setVisibility(VISIBLE);
		loadingOverlay.animate(true);
	}

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		loadingOverlay.animate(false);
		show(detailsPresenter);
	}

	@Subscribe
	public void onShowSearchWidget(Events.LXShowSearchWidget event) {
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onActivitySelectedRetry(Events.LXActivitySelectedRetry event) {
		show(detailsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowParamsOverlayOnResults(Events.LXSearchParamsOverlay event) {
		OmnitureTracking.trackAppLXSearchBox();
		show(new LXParamsOverlay());
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		show(checkoutPresenter);
	}

	@Subscribe
	public void onCheckoutSuccess(Events.LXCheckoutSucceeded event) {
		show(confirmationWidget, FLAG_CLEAR_BACKSTACK);
	}
}
