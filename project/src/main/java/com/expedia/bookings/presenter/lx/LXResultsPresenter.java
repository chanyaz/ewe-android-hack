package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.lx.LXTheme;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.LeftToRightTransition;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.DateUtils;
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
import rx.Observer;
import rx.Subscription;

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

	Subscription searchSubscription;

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

	@OnClick(R.id.sort_filter_button)
	public void onSortFilterClicked() {
		OmnitureTracking.trackAppLXSortAndFilterOpen();
		show(sortFilterWidget);
	}

	public LXResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Transitions
	private Presenter.Transition searchResultsToSortFilter = new Presenter.Transition(LXSearchResultsWidget.class, LXSortFilterWidget.class,
		new DecelerateInterpolator(), ANIMATION_DURATION) {

		@Override
		public void startTransition(boolean forward) {
			sortFilterButton.showNumberOfFilters(sortFilterWidget.getNumberOfSelectedFilters());
			sortFilterWidget.setVisibility(View.VISIBLE);

			// Kitkat & below view is not adjusting height and weights.

			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) sortFilterWidgetAnimateView
					.getLayoutParams();
				layoutParams.height = 1;
				layoutParams.weight = 1;
				sortFilterWidgetAnimateView.setLayoutParams(layoutParams);
				sortFilterWidgetAnimateView.requestLayout();
			}

			if (isUserBucketedForCategoriesTest) {
				transparentView.setAlpha(forward ? 0 : 0.5f);
				transparentView.setVisibility(VISIBLE);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			sortFilterWidget.setTranslationY(sortFilterWidget.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
			if (isUserBucketedForCategoriesTest) {
				transparentView.setAlpha(forward ? 0.5f : 0);
				transparentView.setVisibility(forward ? VISIBLE : GONE);
			}
			else {
				sortFilterWidget.setTranslationY(forward ? 0 : sortFilterWidget.getHeight());
			}
			sortFilterWidget.setVisibility(forward ? VISIBLE : GONE);
			if (forward) {
				sortFilterWidget.setFocusToToolbarForAccessibility();
			}
		}
	};

	Transition themeResultsToActivityResults = new LeftToRightTransition(this, LXThemeResultsWidget.class, LXSearchResultsWidget.class) {
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		addTransition(searchResultsToSortFilter);
		addTransition(themeResultsToActivityResults);

		setUserBucketedForCategoriesTest(Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCategoryABTest));

		setupToolbar();
		int toolbarSize = Ui.getStatusBarHeight(getContext());
		if (toolbarSize > 0) {
			searchResultsWidget.setPadding(0, Ui.toolbarSizeWithStatusBar(getContext()), 0, 0);
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
			searchSubscription.unsubscribe();
		}
	}

	private Observer<LXTheme> lxThemeSearchObserver = new Observer<LXTheme>() {
		@Override
		public void onCompleted() {
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
				themeSelected, new LXSortFilterMetadata(theme.filterCategories, LXSortType.POPULARITY),
				themeResultSortObserver);
			setToolbarTitles(theme.title, getSearchDate());
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
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title), lxState.searchParams.location);
			searchResultsWidget.setVisibility(GONE);
			themeResultsWidget.setVisibility(VISIBLE);
			show(themeResultsWidget, FLAG_CLEAR_BACKSTACK);
		}
	}

	private class SearchResultObserver implements Observer<LXSearchResponse> {
		public SearchType searchType;
		public View widget;

		@Override
		public void onCompleted() {
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
			AdTracker.trackLXSearch(lxState.searchParams);
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
		if (Strings.isNotEmpty(lxState.searchParams.activityId)) {
			LXActivity activity = lxSearchResponse.getActivityFromID(lxState.searchParams.activityId);
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
			Events.post(new Events.LXSearchFilterResultsReady(lxSearchResponse.activities, lxSearchResponse.filterCategories));
			if (!lxSearchResponse.isFromCachedResponse) {
				Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));
			}
			sortFilterWidget.bind(lxSearchResponse.filterCategories);
			if (!isGroundTransport) {
				sortFilterButton.setVisibility(View.VISIBLE);
			}
			sortFilterButton.showNumberOfFilters(sortFilterWidget.getNumberOfSelectedFilters());
		}
	};

	private class ThemeResultSortObserver implements Observer<LXTheme> {

		@Override
		public void onCompleted() {
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
		if (event.lxSearchParams.searchType.equals(SearchType.EXPLICIT_SEARCH)) {
			Events.post(new Events.LXShowLoadingAnimation());
		}

		setUserBucketedForCategoriesTest(Db.getAbacusResponse()
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCategoryABTest));

		cleanup();
		sortFilterWidget.bind(null);
		sortFilterButton.setVisibility(View.GONE);
		searchResultFilterObserver.searchType = event.lxSearchParams.searchType;

		String filters = null;
		boolean areExternalFiltersSupplied = false;
		if (isGroundTransport) {
			filters = GT_FILTERS;
			areExternalFiltersSupplied = true;
		}
		else if (Strings.isNotEmpty(event.lxSearchParams.filters)) {
			filters = event.lxSearchParams.filters;
			areExternalFiltersSupplied = true;
		}
		if (isUserBucketedForCategoriesTest && Strings.isEmpty(event.lxSearchParams.filters)) {
			show(themeResultsWidget, FLAG_CLEAR_BACKSTACK);
			themeResultsWidget.setVisibility(VISIBLE);
			searchResultsWidget.setVisibility(GONE);
			themeResultObserver.searchType = event.lxSearchParams.searchType;
			themeResultObserver.widget = themeResultsWidget;
			searchSubscription = lxServices.lxCategorySearch(event.lxSearchParams, themeResultObserver);
			themeResultsWidget.getThemePublishSubject().subscribe(lxThemeSearchObserver);
			sortFilterButton.setFilterText(getResources().getString(R.string.sort));
			sortFilterWidget.setToolbarTitle(getResources().getString(R.string.sort));
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title), event.lxSearchParams.location);
		}
		else {
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
			themeResultsWidget.setVisibility(GONE);
			searchResultsWidget.setVisibility(VISIBLE);
			searchResultObserver.searchType = event.lxSearchParams.searchType;
			searchResultObserver.widget = searchResultsWidget;
			searchResultFilterObserver.widget = searchResultsWidget;
			searchSubscription = lxServices.lxSearchSortFilter(event.lxSearchParams,
				areExternalFiltersSupplied ? new LXSortFilterMetadata(filters) : null,
				areExternalFiltersSupplied ? searchResultFilterObserver : searchResultObserver);
			sortFilterButton.setFilterText(getResources().getString(R.string.sort_and_filter));
			sortFilterWidget.setToolbarTitle(getResources().getString(R.string.sort_and_filter));
			setToolbarTitles(event.lxSearchParams.location, getSearchDate());
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
				themeResultSortObserver);
		}
		else {
			searchSubscription = lxServices
				.lxSearchSortFilter(null, event.lxSortFilterMetadata, searchResultFilterObserver);
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

	private String getSearchDate() {
		return String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMd(lxState.searchParams.startDate), DateUtils.localDateToMMMd(lxState.searchParams.endDate));
	}

	private void setupToolbar() {
		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationContentDescription(R.string.toolbar_search_nav_icon_cont_desc);
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

	private void setToolbarTitles(String detailsText, String subtitleText) {
		toolBarDetailText.setText(detailsText);
		toolBarSubtitleText.setText(subtitleText);
		toolBarSubtitleText.setVisibility(View.VISIBLE);
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
					sortFilterButton.animate().translationY(heightOfButton).setInterpolator(new DecelerateInterpolator()).start();
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
			setToolbarTitles(getResources().getString(R.string.lx_select_a_category_title), lxState.searchParams.location);
		}
		return super.back();
	}

	public void trackLXSearch() {
		if (searchResponse != null && searchResponse.regionId != null) {
			OmnitureTracking.trackAppLXSearch(lxState.searchParams, searchResponse, isGroundTransport);
		}
	}

}
