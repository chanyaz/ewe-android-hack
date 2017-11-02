package com.expedia.bookings.presenter.lx;

import java.util.HashMap;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.R;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.lx.LXTheme;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.LeftToRightTransition;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FilterButtonWithCountWidget;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.expedia.bookings.widget.LXSortFilterWidget;
import com.expedia.bookings.widget.LXThemeResultsWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class LXResultsPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;
	@Inject
	LxServices lxServices;

	@Inject
	LXState lxState;

	@InjectView(R.id.lx_search_results_widget)
	LXSearchResultsWidget searchResultsWidget;

	@InjectView(R.id.lx_theme_results_widget)
	LXThemeResultsWidget themeResultsWidget;

	Disposable searchSubscription;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.sort_filter_widget)
	LXSortFilterWidget sortFilterWidget;

	@InjectView(R.id.sort_filter_button_container)
	FilterButtonWithCountWidget sortFilterButton;

	// This is here just for an animation
	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@InjectView(R.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	@InjectView(R.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	@InjectView(R.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	@InjectView(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	@InjectView(R.id.transparent_view)
	View transparentView;

	@InjectView(R.id.sort_filter_widget_animate_view)
	View sortFilterWidgetAnimateView;

	@OnClick(R.id.transparent_view)
	public void onTransparentViewClick() {
		show(searchResultsWidget, FLAG_CLEAR_TOP);
	}

	private int searchTop;
	private ArrowXDrawable navIcon;

	private SearchResultObserver searchResultObserver = new SearchResultObserver();

	private SearchResultFilterObserver searchResultFilterObserver = new SearchResultFilterObserver();
	private LXSearchResponse searchResponse;
	private ThemeResultObserver themeResultObserver = new ThemeResultObserver();
	private ThemeResultSortObserver themeResultSortObserver = new ThemeResultSortObserver();

	private boolean isGroundTransport;
	private static final String GT_FILTERS = "Shared Transfers|Private Transfers";
	private boolean isUserBucketedForCategoriesTest;
	private LXTheme themeSelected = new LXTheme();
	private boolean lxFilterTextSearchEnabled;

	@OnClick(R.id.sort_filter_button)
	public void onSortFilterClicked() {
		OmnitureTracking.trackAppLXSortAndFilterOpen();
		show(sortFilterWidget);
	}

	public LXResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Transitions
	private Presenter.Transition searchResultsToSortFilter = new Presenter.Transition(LXSearchResultsWidget.class,
		LXSortFilterWidget.class,
		new DecelerateInterpolator(), ANIMATION_DURATION) {

		@Override
		public void startTransition(boolean forward) {
			sortFilterButton.showNumberOfFilters(sortFilterWidget.getNumberOfSelectedFilters());
			sortFilterWidget.setVisibility(View.VISIBLE);

			transparentView.setAlpha(forward ? 0.5f : 0);
			transparentView.setVisibility(forward ? VISIBLE : GONE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			sortFilterWidget.setTranslationY(sortFilterWidget.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
			if (!isUserBucketedForCategoriesTest) {
				sortFilterWidget.setTranslationY(forward ? 0 : sortFilterWidget.getHeight());
			}
			sortFilterWidget.setVisibility(forward ? VISIBLE : GONE);
			if (forward) {
				sortFilterWidget.setFocusToToolbarForAccessibility();
				ViewCompat
					.setImportantForAccessibility(toolbar, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
				ViewCompat.setImportantForAccessibility(searchResultsWidget,
					ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
				ViewCompat.setImportantForAccessibility(transparentView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
			}
			else {
				ViewCompat.setImportantForAccessibility(toolbar, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
				ViewCompat
					.setImportantForAccessibility(searchResultsWidget, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
				ViewCompat.setImportantForAccessibility(transparentView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
			}
		}
	};

	Transition themeResultsToActivityResults = new LeftToRightTransition(this, LXThemeResultsWidget.class,
		LXSearchResultsWidget.class) {

		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			if (!forward) {
				searchResultsWidget.setVisibility(GONE);
			}
		}

		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			themeResultsWidget.setVisibility(forward ? GONE : VISIBLE);
			searchResultsWidget.setVisibility(forward ? VISIBLE : GONE);
			AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);

		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		addTransition(searchResultsToSortFilter);
		addTransition(themeResultsToActivityResults);

		setUserBucketedForCategoriesTest(
			AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCategoryABTest));

		lxFilterTextSearchEnabled = AbacusFeatureConfigManager
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXFilterSearch);

		setupToolbar();
		int toolbarSize = Ui.getStatusBarHeight(getContext());
		if (toolbarSize > 0) {
			themeResultsWidget.setPadding(0, Ui.toolbarSizeWithStatusBar(getContext()), 0, 0);
		}
		searchResultsWidget.getRecyclerView().setOnScrollListener(recyclerScrollListener);
		sortFilterButton.setFilterText(getResources().getString(R.string.sort_and_filter));
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	private void cleanup() {
		if (searchSubscription != null) {
			searchSubscription.dispose();
		}
	}

	private Observer<LXTheme> lxThemeSearchObserver = new DisposableObserver<LXTheme>() {
		@Override
		public void onComplete() {
			//ignore
		}

		@Override
		public void onError(Throwable e) {
			//ignore
		}

		@Override
		public void onNext(LXTheme theme) {
			OmnitureTracking.trackLinkLXCategoryClicks(theme.titleEN);
			sortFilterWidget.resetSortAndFilter();

			sortFilterWidget.bind(theme.filterCategories);
			sortFilterButton.setFilterText(getResources().getString(R.string.sort_and_filter));
			themeSelected = theme;
			show(searchResultsWidget, FLAG_CLEAR_TOP);
			sortFilterButton.showNumberOfFilters(0);
			sortFilterButton.setVisibility(VISIBLE);
			searchSubscription = lxServices.lxThemeSortAndFilter(
				themeSelected,
				new LXSortFilterMetadata(new HashMap<String, LXCategoryMetadata>(), LXSortType.POPULARITY),
				themeResultSortObserver, lxFilterTextSearchEnabled);
			setToolbarTitles(theme.title,
				LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, false),
				LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, true));
			sortFilterWidget.invalidate();

			switch (theme.themeType) {
			case TopRatedActivities:
				sortFilterWidget.categoryFilterVisibility(false);
				break;
			case AllThingsToDo:
				sortFilterWidget.setThemeAllThingsToDo(true);
			default:
				sortFilterWidget.categoryFilterVisibility(true);
				break;
			}
		}
	};

	private class ThemeResultObserver extends SearchResultObserver {

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			OmnitureTracking.trackAppLXSearchCategories(lxState.searchParams, lxSearchResponse);
			searchResponse = lxSearchResponse;
			themeResultsWidget.bind(lxSearchResponse.lxThemes, "SFO");
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title),
				lxState.searchParams.getLocation());
			searchResultsWidget.setVisibility(GONE);
			themeResultsWidget.setVisibility(VISIBLE);
			show(themeResultsWidget, FLAG_CLEAR_BACKSTACK);
		}
	}

	class SearchResultObserver extends DisposableObserver<LXSearchResponse> {
		public SearchType searchType;
		public View widget;

		@Override
		public void onComplete() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("LXSearch - onError", e);
			show(widget, FLAG_CLEAR_BACKSTACK);

			if (RetrofitUtils.isNetworkError(e)) {
				showSearchErrorDialog(R.string.error_no_internet);
				return;
			}
			else {
				LXNavUtils.handleLXSearchFailure(e, searchType, isGroundTransport);
			}

			sortFilterButton.setVisibility(View.GONE);
		}

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			searchResponse = lxSearchResponse;
			// Search Results Omniture Tracking on load of search screen.
			OmnitureTracking.trackAppLXRTRABTest();
			trackLXSearch();
			Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));
			searchResultsWidget.bind(lxSearchResponse.activities);
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
			sortFilterWidget.bind(lxSearchResponse.filterCategories);
			if (!isGroundTransport) {
				sortFilterButton.setVisibility(View.VISIBLE);
			}
			sortFilterButton.showNumberOfFilters(0);
			AdTracker.trackLXSearchResults(lxState.searchParams, lxSearchResponse);

			handleActivityDetailsDeeplink(lxSearchResponse);
			themeResultsWidget.setVisibility(GONE);
		}
	}

	private void handleActivityDetailsDeeplink(LXSearchResponse lxSearchResponse) {
		if (Strings.isNotEmpty(lxState.searchParams.getActivityId())) {
			LXActivity activity = lxSearchResponse.getActivityFromID(lxState.searchParams.getActivityId());
			if (activity != null) {
				Events.post(new Events.LXActivitySelected(activity));
			}
		}
	}

	private class SearchResultFilterObserver extends SearchResultObserver {

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			searchResponse = lxSearchResponse;
			trackLXSearch();
			Events.post(
				new Events.LXSearchFilterResultsReady(lxSearchResponse.activities, lxSearchResponse.filterCategories));
			if (!lxSearchResponse.isFromCachedResponse) {
				Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));
			}
			sortFilterWidget.bind(lxSearchResponse.filterCategories);
			if (!isGroundTransport) {
				sortFilterButton.setVisibility(View.VISIBLE);
			}
			sortFilterButton.showNumberOfFilters(sortFilterWidget.getNumberOfSelectedFilters());
		}
	}

	private class ThemeResultSortObserver extends DisposableObserver<LXTheme> {

		@Override
		public void onComplete() {
			// ignore
		}

		@Override
		public void onError(Throwable e) {
			// ignore
		}

		@Override
		public void onNext(LXTheme theme) {
			themeSelected = theme;
			searchResultsWidget.bind(theme.activities);
			Events.post(new Events.LXSearchFilterResultsReady(theme.activities, theme.filterCategories));
		}
	}

	private void showSearchErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXNewSearchParamsAvailable(lxState.searchParams));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXShowSearchWidget());
				}
			})
			.show();
	}

	@Subscribe
	public void onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		OmnitureTracking.trackAppLXCategoryABTest();

		// Dispatch loading animation event if explicit search. Default search dispatches event separately.
		if (event.lxSearchParams.getSearchType().equals(SearchType.EXPLICIT_SEARCH)) {
			Events.post(new Events.LXShowLoadingAnimation());
		}

		setUserBucketedForCategoriesTest(
			AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCategoryABTest));

		cleanup();
		sortFilterWidget.bind(null);
		sortFilterButton.setVisibility(View.GONE);
		searchResultFilterObserver.searchType = event.lxSearchParams.getSearchType();

		String filters = null;
		boolean areExternalFiltersSupplied = false;
		if (isGroundTransport) {
			filters = GT_FILTERS;
			areExternalFiltersSupplied = true;
		}
		else if (Strings.isNotEmpty(event.lxSearchParams.getFilters())) {
			filters = event.lxSearchParams.getFilters();
			areExternalFiltersSupplied = true;
		}
		if (isUserBucketedForCategoriesTest && Strings.isEmpty(event.lxSearchParams.getFilters())) {
			show(themeResultsWidget, FLAG_CLEAR_BACKSTACK);
			themeResultsWidget.setVisibility(VISIBLE);
			searchResultsWidget.setVisibility(GONE);
			themeResultObserver.searchType = event.lxSearchParams.getSearchType();
			themeResultObserver.widget = themeResultsWidget;
			searchSubscription = lxServices.lxCategorySearch(event.lxSearchParams, themeResultObserver);
			if (!themeResultsWidget.getThemePublishSubject().hasObservers()) {
				themeResultsWidget.getThemePublishSubject().subscribe(lxThemeSearchObserver);
			}
			sortFilterButton.setFilterText(getResources().getString(R.string.sort));
			sortFilterWidget.setToolbarTitle(getResources().getString(R.string.sort));
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title),
				event.lxSearchParams.getLocation());
		}
		else {
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
			themeResultsWidget.setVisibility(GONE);
			searchResultsWidget.setVisibility(VISIBLE);
			searchResultObserver.searchType = event.lxSearchParams.getSearchType();
			searchResultObserver.widget = searchResultsWidget;
			searchResultFilterObserver.widget = searchResultsWidget;
			searchSubscription = lxServices.lxSearchSortFilter(event.lxSearchParams,
				areExternalFiltersSupplied ? new LXSortFilterMetadata(filters) : null,
				areExternalFiltersSupplied ? searchResultFilterObserver : searchResultObserver,
				lxFilterTextSearchEnabled);
			sortFilterButton.setFilterText(getResources().getString(R.string.sort_and_filter));
			sortFilterWidget.setToolbarTitle(getResources().getString(R.string.sort_and_filter));
			setToolbarTitles(event.lxSearchParams.getLocation(),
				LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, false),
				LXDataUtils.getToolbarSearchDateText(getContext(), lxState.searchParams, true));
		}

		if (areExternalFiltersSupplied) {
			sortFilterWidget.setSelectedFilterCategories(filters);
		}
	}

	@Subscribe
	public void onLXSearchError(Events.LXShowSearchError event) {
		if (event.searchType.equals(SearchType.DEFAULT_SEARCH)
			&& event.error.errorCode != ApiError.Code.LX_SEARCH_NO_RESULTS) {
			toolBarDetailText.setText(getResources().getString(R.string.lx_error_current_location_toolbar_text));
			toolBarSubtitleText.setVisibility(View.GONE);
		}
	}

	@Subscribe
	public void onLXFilterChanged(Events.LXFilterChanged event) {
		if (isUserBucketedForCategoriesTest) {
			searchSubscription = lxServices.lxThemeSortAndFilter(themeSelected, event.lxSortFilterMetadata,
				themeResultSortObserver, lxFilterTextSearchEnabled);
		}
		else {
			searchSubscription = lxServices
				.lxSearchSortFilter(null, event.lxSortFilterMetadata, searchResultFilterObserver,
					lxFilterTextSearchEnabled);
		}
	}

	@Subscribe
	public void onLXFilterDoneClicked(Events.LXFilterDoneClicked event) {
		if (isUserBucketedForCategoriesTest) {
			show(searchResultsWidget, FLAG_CLEAR_TOP);
		}
		else {
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		}
		trackLXSearch();
		AdTracker.trackFilteredLXSearchResults(lxState.searchParams, searchResponse);
	}

	@Subscribe
	public void onLXShowLoadingAnimation(Events.LXShowLoadingAnimation event) {
		if (isUserBucketedForCategoriesTest) {
			themeResultsWidget.setVisibility(VISIBLE);
			searchResultsWidget.setVisibility(GONE);
		}
		else {
			themeResultsWidget.setVisibility(GONE);
			searchResultsWidget.setVisibility(VISIBLE);
		}
	}

	private void setupToolbar() {
		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc);
		toolbar.inflateMenu(R.menu.lx_results_details_menu);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_open_search:
					Events.post(new Events.LXSearchParamsOverlay());
					return true;
				}
				return false;
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbarBackground.getLayoutParams().height += statusBarHeight;
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		toolBarDetailText.setText(getResources().getString(R.string.lx_getting_current_location));
	}

	private void setToolbarTitles(String detailsText, String subtitleText, String searchDateContDesc) {
		toolBarDetailText.setText(detailsText);
		toolBarSubtitleText.setText(subtitleText);
		toolBarSubtitleText.setContentDescription(searchDateContDesc);
		toolBarSubtitleText.setVisibility(View.VISIBLE);
	}

	private void setToolbarTitles(String detailsText, String subtitleText) {
		setToolbarTitles(detailsText, subtitleText, null);
	}

	public void animationStart(boolean forward) {
		searchTop = toolBarSearchText.getTop() - toolbarTwo.getTop();
		toolbar.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(searchTop);
		toolBarSubtitleText.setTranslationY(searchTop);
	}

	public void animationUpdate(float f, boolean forward) {
		float yTrans = forward ? -(searchTop * -f) : (searchTop * (1 - f));
		toolBarDetailText.setTranslationY(yTrans);
		toolBarSubtitleText.setTranslationY(yTrans);
		navIcon.setParameter(forward ? f : Math.abs(1 - f));
	}

	public void animationFinalize(boolean forward) {
		toolbarBackground.setAlpha(1f);
		toolbar.setVisibility(VISIBLE);
		toolbarBackground.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(0);
		toolBarSubtitleText.setTranslationY(0);
		navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.BACK.getType());
	}

	RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener() {
		private int scrolledDistance = 0;
		private int heightOfButton = (int) getResources().getDimension(R.dimen.lx_sort_filter_container_height);

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				if (scrolledDistance > heightOfButton / 2) {
					sortFilterButton.animate().translationY(heightOfButton)
						.setInterpolator(new DecelerateInterpolator()).start();
				}
				else {
					sortFilterButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
				}
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			if (scrolledDistance > 0) {
				scrolledDistance = Math.min(heightOfButton, scrolledDistance + dy);
				sortFilterButton.setTranslationY(Math.min(heightOfButton, scrolledDistance));
			}
			else {
				scrolledDistance = Math.max(0, scrolledDistance + dy);
				sortFilterButton.setTranslationY(Math.min(scrolledDistance, 0));
			}
		}
	};

	public void setIsFromGroundTransport(boolean isGroundTransport) {
		this.isGroundTransport = isGroundTransport;
	}

	public void setUserBucketedForCategoriesTest(boolean isUserBucketedForTest) {
		this.isUserBucketedForCategoriesTest = isUserBucketedForTest && !isGroundTransport;
		sortFilterWidget.setUserBucketedForCategoriesTest(isUserBucketedForCategoriesTest);
	}

	@Override
	public boolean back() {
		if (LXSearchResultsWidget.class.getName().equals(getCurrentState()) && searchResponse != null
			&& searchResponse.regionId != null && isUserBucketedForCategoriesTest) {
			OmnitureTracking.trackAppLXSearchCategories(lxState.searchParams, searchResponse);
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title),
				lxState.searchParams.getLocation());
		}
		if (LXSortFilterWidget.class.getName().equals(getCurrentState())) {
			if (sortFilterWidget.isFilteredToZeroResults()) {
				sortFilterWidget.getDynamicFeedbackWidget().animateDynamicFeedbackWidget();
				return true;
			}
		}
		return super.back();
	}

	public void trackLXSearch() {
		if (searchResponse != null && searchResponse.regionId != null) {
			OmnitureTracking.trackAppLXSearch(lxState.searchParams, searchResponse, isGroundTransport);
		}
	}
}
