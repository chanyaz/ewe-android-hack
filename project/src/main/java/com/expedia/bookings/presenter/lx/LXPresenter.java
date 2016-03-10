package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXConfirmationWidget;
import com.expedia.bookings.widget.LoadingOverlayWidget;
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


	// This will always be the first to be shown under the AB test/ Non- AB Test scenario.
	@InjectView(R.id.activity_recommended_details_presenter)
	LXDetailsWithRecommendationsPresenter recommendationPresenter;

	// This will take care of the recommendation selected under the A/B Test.
	@InjectView(R.id.activity_details_presenter)
	LXDetailsPresenter detailsPresenter;

	@InjectView(R.id.details_loading_overlay)
	LoadingOverlayWidget loadingOverlay;

	@InjectView(R.id.confirmation)
	LXConfirmationWidget confirmationWidget;

	private float searchStartingAlpha;

	private static class LXParamsOverlay {
		// ignore
	}

	@InjectView(R.id.lx_checkout_presenter)
	LXCheckoutPresenter checkoutPresenter;

	@Inject
	LXState lxState;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		addTransition(searchParamsToResults);
		addTransition(resultsToRecommendations);
		addTransition(searchOverlayOnResults);

		addTransition(searchOverlayOnDetails);
		addTransition(searchOverlayOnRecommendations);

		addTransition(detailsToCheckout);
		addTransition(recommendationsToCheckout);

		addTransition(recommendationToDetails);

		addTransition(detailsToSearch);
		addTransition(recommendationsToSearch);

		addTransition(checkoutToConfirmation);
		addTransition(checkoutToResults);
		show(resultsPresenter);
		resultsPresenter.setVisibility(VISIBLE);

		int[] attrs = {R.attr.skin_lxPrimaryColor};
		TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
		loadingOverlay.setBackgroundAttr(ta.getDrawable(0));
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
			resultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			searchParamsWidget.setVisibility(forward ? GONE : VISIBLE);
			resultsPresenter.animationFinalize(!forward);
			searchParamsWidget.animationFinalize(!forward);
		}
	};

	private Transition detailsToCheckout = new VisibilityTransition(this, LXDetailsPresenter.class, LXCheckoutPresenter.class);
	private Transition recommendationsToCheckout = new VisibilityTransition(this, LXDetailsWithRecommendationsPresenter.class, LXCheckoutPresenter.class);

	private Presenter.Transition recommendationToDetails = new Presenter.Transition(
		LXDetailsWithRecommendationsPresenter.class.getName(), LXDetailsPresenter.class.getName(),
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
			recommendationPresenter.setVisibility(VISIBLE);
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
			detailsPresenter.setVisibility(forward ? VISIBLE : GONE);
			recommendationPresenter.setVisibility(forward ? GONE : VISIBLE);
			loadingOverlay.setVisibility(GONE);
			detailsPresenter.animationFinalize(!forward);
			if (!forward) {
				detailsPresenter.cleanup();
				lxState.onActivitySelected(new Events.LXActivitySelected(recommendationPresenter.getLxActivity()));
				lxState.onShowActivityDetails(new Events.LXShowDetails(recommendationPresenter.details.getActivityDetails()));
			}
		}
	};

	private Presenter.Transition resultsToRecommendations = new Presenter.Transition(LXResultsPresenter.class.getName(),
		LXDetailsWithRecommendationsPresenter.class.getName(),
		new DecelerateInterpolator(), ANIMATION_DURATION) {
		private int detailsHeight;

		@Override
		public void startTransition(boolean forward) {
			final int parentHeight = getHeight();
			detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext());
			float pos = forward ? parentHeight + detailsHeight : detailsHeight;
			recommendationPresenter.setTranslationY(pos);
			resultsPresenter.setVisibility(View.VISIBLE);
			recommendationPresenter.animationStart(!forward);
			recommendationPresenter.setVisibility(VISIBLE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float pos = forward ? detailsHeight + (-f * detailsHeight) : (f * detailsHeight);
			recommendationPresenter.setTranslationY(pos);
			recommendationPresenter.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
			recommendationPresenter.setTranslationY(forward ? 0 : detailsHeight);
			recommendationPresenter.setVisibility(forward ? VISIBLE : GONE);
			resultsPresenter.setVisibility(forward ? GONE : VISIBLE);
			loadingOverlay.setVisibility(GONE);
			recommendationPresenter.animationFinalize(!forward);
			if (!forward) {
				resultsPresenter.trackLXSearch();
				recommendationPresenter.cleanup();
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
			resultsPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			detailsPresenter.setVisibility(View.GONE);
			resultsPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
		}
	};


	private Transition searchOverlayOnDetails = transitionDetailsStateToSearchOverlay(LXDetailsPresenter.class);
	private Transition searchOverlayOnRecommendations = transitionDetailsStateToSearchOverlay(
		LXDetailsWithRecommendationsPresenter.class);

	@NonNull
	private Transition transitionDetailsStateToSearchOverlay(Class state) {
		return new Transition(state,
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
				detailsPresenter.setVisibility(VISIBLE);
				searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
				detailsPresenter.animationFinalize(forward);
				searchParamsWidget.animationFinalize(forward);
			}
		};
	}

	private Transition detailsToSearch = new VisibilityTransition(this, LXDetailsPresenter.class, LXSearchParamsPresenter.class);
	private Transition recommendationsToSearch = new VisibilityTransition(this, LXDetailsWithRecommendationsPresenter.class, LXSearchParamsPresenter.class);

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

		if (isCurrentStateDetailsWithRecommedation()) {
			detailsPresenter.onActivitySelected(event.lxActivity);
		}
		else {
			recommendationPresenter.onActivitySelected(event.lxActivity);
		}
	}

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		loadingOverlay.animate(false);

		if (isCurrentStateDetailsWithRecommedation()) {
			show(detailsPresenter);
			detailsPresenter.details.onShowActivityDetails(event.activityDetails);
		}
		else {
			show(recommendationPresenter);
			recommendationPresenter.details.onShowActivityDetails(event.activityDetails);
		}
	}

	@Subscribe
	public void onDetailsDateChanged(Events.LXDetailsDateChanged event) {
		if (isCurrentStateDetailsWithRecommedation()) {
			recommendationPresenter.details.onDetailsDateChanged(event.dateSelected, event.buttonSelected);
		}
		else {
			detailsPresenter.details.onDetailsDateChanged(event.dateSelected, event.buttonSelected);
		}
	}

	@Subscribe
	public void onShowSearchWidget(Events.LXShowSearchWidget event) {
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onActivitySelectedRetry(Events.LXActivitySelectedRetry event) {
		show(isCurrentStateDetailsWithRecommedation() ? recommendationPresenter : detailsPresenter, FLAG_CLEAR_TOP);
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

	public void setIsGroundTransport(boolean isGroundTransport) {
		resultsPresenter.setIsFromGroundTransport(isGroundTransport);
	}
	public void setUserBucketedForCategoriesTest(boolean isUserBucketedForTest) {
		resultsPresenter.setUserBucketedForCategoriesTest(isUserBucketedForTest);
	}
	public void setUserBucketedForRTRTest(boolean userBucketedForRTRTest) {
		recommendationPresenter.details.setUserBucketedForRTRTest(userBucketedForRTRTest);
		detailsPresenter.details.setUserBucketedForRTRTest(userBucketedForRTRTest);
		resultsPresenter.searchResultsWidget.setUserBucketedForRTRTest(userBucketedForRTRTest);
	}

	public void setUserBucketedForRecommendationTest(boolean isUserBucketedForTest) {
		recommendationPresenter.setUserBucketedForRecommendationTest(isUserBucketedForTest);
	}

	public boolean isCurrentStateDetailsWithRecommedation() {
		return LXDetailsWithRecommendationsPresenter.class.getName().equals(getCurrentState());
	}
}
