package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.TransitionElement;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.lob.lx.ui.viewmodel.LXSearchViewModel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.LXConfirmationWidget;
import com.expedia.bookings.widget.LoadingOverlayWidget;
import com.expedia.vm.LXMapViewModel;
import com.google.android.gms.maps.MapView;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;

public class LXPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;
	private boolean isGroundTransport;

	public LXPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.search_params_widget)
	public LXSearchPresenter searchParamsWidget;

	@InjectView(R.id.search_list_presenter)
	LXResultsPresenter resultsPresenter;

	@InjectView(R.id.details_map_view)
	MapView detailsMapView;

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

		searchParamsWidget.setSearchViewModel(new LXSearchViewModel(getContext()));

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

		searchParamsWidget.getSearchViewModel().getSearchParamsObservable().subscribe(lxSearchParamsObserver);
		recommendationPresenter.fullscreenMapView.setViewmodel(new LXMapViewModel(getContext()));
		setLxDetailMap();
	}

	private Observer<LxSearchParams> lxSearchParamsObserver = new Observer<LxSearchParams>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(LxSearchParams params) {
			Events.post(new Events.LXNewSearchParamsAvailable(params));
		}
	};

	TransitionElement searchBackgroundColor = new TransitionElement(ContextCompat.getColor(getContext(), R.color.search_anim_background), Color.TRANSPARENT);
	ArgbEvaluator searchArgbEvaluator = new ArgbEvaluator();

	private Transition searchParamsToResults = new Transition(LXSearchPresenter.class,
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
			setBackgroundColorForSearchWidget(f, forward);
			searchParamsWidget.animationUpdate(f, !forward);
		}

		@Override
		public void endTransition(boolean forward) {
			resultsPresenter.setVisibility(forward ? VISIBLE : GONE);
			searchParamsWidget.setVisibility(forward ? GONE : VISIBLE);
			resultsPresenter.animationFinalize(!forward);
			searchParamsWidget.animationFinalize(!forward);
			if (searchParamsWidget.getFirstLaunch()) {
				searchParamsWidget.showSuggestionState(false);
			}
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar);
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(searchParamsWidget.getToolbar());
			}
		}
	};

	private Transition detailsToCheckout = new VisibilityTransition(this, LXDetailsPresenter.class, LXCheckoutPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (!forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(detailsPresenter.toolbar);
			}
		}
	};
	private Transition recommendationsToCheckout = new VisibilityTransition(this, LXDetailsWithRecommendationsPresenter.class, LXCheckoutPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (!forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(recommendationPresenter.toolbar);
			}
		}
	};

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
				lxState.onShowActivityDetails(
					new Events.LXShowDetails(recommendationPresenter.details.getActivityDetails()));
				AccessibilityUtil.setFocusToToolbarNavigationIcon(recommendationPresenter.toolbar);
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(detailsPresenter.toolbar);
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
				AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar);
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(recommendationPresenter.toolbar);
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
			recommendationPresenter.setVisibility(View.GONE);
			resultsPresenter.animationStart(forward);
			searchParamsWidget.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			resultsPresenter.animationUpdate(f, forward);
			setBackgroundColorForSearchWidget(f, forward);
			searchParamsWidget.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			if (forward) {
				resultsPresenter.setVisibility(GONE);
			}
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			resultsPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(searchParamsWidget.getToolbar());
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar);
			}
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
			setBackgroundColorForSearchWidget(f, forward);
			searchParamsWidget.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			if (forward) {
				detailsPresenter.setVisibility(GONE);
			}
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			detailsPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(searchParamsWidget.getToolbar());
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(detailsPresenter.toolbar);
			}
		}
	};

	private Transition searchOverlayOnRecommendations = new Transition(LXDetailsWithRecommendationsPresenter.class,
		LXParamsOverlay.class, new DecelerateInterpolator(), ANIMATION_DURATION) {
		@Override
		public void startTransition(boolean forward) {
			recommendationPresenter.setVisibility(VISIBLE);
			searchParamsWidget.setVisibility(VISIBLE);
			if (forward) {
				searchStartingAlpha = recommendationPresenter.animationStart(forward);
			}
			else {
				recommendationPresenter.animationStart(forward);
			}
			searchParamsWidget.animationStart(forward);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			recommendationPresenter.animationUpdate(f, forward);
			setBackgroundColorForSearchWidget(f, forward);
			searchParamsWidget.animationUpdate(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			if (forward) {
				recommendationPresenter.setVisibility(GONE);
			}
			searchParamsWidget.setVisibility(forward ? VISIBLE : GONE);
			recommendationPresenter.animationFinalize(forward);
			searchParamsWidget.animationFinalize(forward);
		}
	};

	private Transition detailsToSearch = new VisibilityTransition(this, LXDetailsPresenter.class, LXSearchPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(searchParamsWidget.getToolbar());
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(detailsPresenter.toolbar);
			}
		}
	};
	private Transition recommendationsToSearch = new VisibilityTransition(this, LXDetailsWithRecommendationsPresenter.class, LXSearchPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(searchParamsWidget.getToolbar());
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(recommendationPresenter.toolbar);
			}
		}
	};

	private Transition checkoutToConfirmation = new VisibilityTransition(this, LXCheckoutPresenter.class, LXConfirmationWidget.class);

	private Transition checkoutToResults = new VisibilityTransition(this, LXCheckoutPresenter.class, LXResultsPresenter.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar);
			}
		}
	};

	@Subscribe
	public void onNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar);
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
		OmnitureTracking.trackAppLXSearchBox(isGroundTransport);
		show(searchParamsWidget, FLAG_CLEAR_BACKSTACK | FLAG_CLEAR_TOP);
		searchParamsWidget.showDefault();
	}

	@Subscribe
	public void onActivitySelectedRetry(Events.LXActivitySelectedRetry event) {
		show(isCurrentStateDetailsWithRecommedation() ? recommendationPresenter : detailsPresenter, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowParamsOverlayOnResults(Events.LXSearchParamsOverlay event) {
		OmnitureTracking.trackAppLXSearchBox(isGroundTransport);
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
		this.isGroundTransport = isGroundTransport;
		resultsPresenter.setIsFromGroundTransport(isGroundTransport);
		recommendationPresenter.details.setIsFromGroundTransport(isGroundTransport);
		detailsPresenter.details.setIsFromGroundTransport(isGroundTransport);
		checkoutPresenter.setIsFromGroundTransport(isGroundTransport);
		checkoutPresenter.checkout.setIsFromGroundTransport(isGroundTransport);
		confirmationWidget.setIsFromGroundTransport(isGroundTransport);
		checkoutPresenter.checkout.paymentInfoCardView.getViewmodel().getLineOfBusiness()
			.onNext(isGroundTransport ? LineOfBusiness.TRANSPORT : LineOfBusiness.LX);
		checkoutPresenter.checkout.mainContactInfoCardView
			.setLineOfBusiness(isGroundTransport ? LineOfBusiness.TRANSPORT : LineOfBusiness.LX);
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

	public void setBackgroundColorForSearchWidget(float f, boolean forward) {
		if (!forward) {
			searchParamsWidget.setBackgroundColor((Integer) (searchArgbEvaluator
				.evaluate(f, searchBackgroundColor.getStart(), searchBackgroundColor.getEnd())));
		}
		else {
			searchParamsWidget.setBackgroundColor(((Integer) searchArgbEvaluator
				.evaluate(f, searchBackgroundColor.getEnd(), searchBackgroundColor.getStart())));
		}
	}

	private void setLxDetailMap() {
		FrameLayout detailsStub = (FrameLayout) recommendationPresenter.fullscreenMapView.findViewById(R.id.stub_map);
		((ViewGroup) detailsMapView.getParent()).removeAllViews();
		detailsStub.addView(detailsMapView);
		recommendationPresenter.fullscreenMapView.setMap(detailsMapView);
		recommendationPresenter.fullscreenMapView.getMapView().getMapAsync(recommendationPresenter.fullscreenMapView);
		recommendationPresenter.fullscreenMapView.getMapView().setVisibility(VISIBLE);
		detailsPresenter.fullscreenMapView = recommendationPresenter.fullscreenMapView;
	}
}
