package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FilterButtonWithCountWidget;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.expedia.bookings.widget.LXSortFilterWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class LXResultsPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;
	@Inject
	LXServices lxServices;

	@Inject
	LXState lxState;

	@InjectView(R.id.lx_search_results_widget)
	LXSearchResultsWidget searchResultsWidget;

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

	private int searchTop;
	private ArrowXDrawable navIcon;

	private SearchResultObserver searchResultObserver = new SearchResultObserver();

	private SearchResultFilterObserver searchResultFilterObserver = new SearchResultFilterObserver();
	private LXSearchResponse searchResponse;

	private boolean isGroundTransport;
	private static final String GT_FILTERS = "Shared Transfers|Private Transfers";

	@OnClick(R.id.sort_filter_button)
	public void onSortFilterClicked() {
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
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			sortFilterWidget.setTranslationY(sortFilterWidget.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			sortFilterWidget.setTranslationY(forward ? 0 : sortFilterWidget.getHeight());
			sortFilterWidget.setVisibility(forward ? VISIBLE : GONE);
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addTransition(searchResultsToSortFilter);
		setupToolbar();
		int toolbarSize = Ui.getStatusBarHeight(getContext());
		if (toolbarSize > 0) {
			searchResultsWidget.setPadding(0, Ui.toolbarSizeWithStatusBar(getContext()), 0, 0);
		}
		searchResultsWidget.getRecyclerView().setOnScrollListener(recyclerScrollListener);
		sortFilterButton.setFilterText(getResources().getString(R.string.lx_sort_filter));
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

	private class SearchResultObserver implements Observer<LXSearchResponse> {
		public SearchType searchType;

		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("LXSearch - onError", e);
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);

			if (RetrofitUtils.isNetworkError(e)) {
				showSearchErrorDialog(R.string.error_no_internet);
				return;
			}
			else if (e instanceof ApiError) {
				Events.post(new Events.LXShowSearchError((ApiError) e, searchType));
				return;
			}

			//Bucket all other errors as Unknown to give some feedback to the user
			ApiError error = new ApiError(ApiError.Code.UNKNOWN_ERROR);
			error.errorInfo = new ApiError.ErrorInfo();
			error.errorInfo.summary = "Unknown";
			error.errorInfo.cause = "Unknown";
			Events.post(new Events.LXShowSearchError(error, searchType));
			sortFilterButton.setVisibility(View.GONE);
		}

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			searchResponse = lxSearchResponse;
			// Search Results Omniture Tracking on load of search screen.
			OmnitureTracking.trackAppLXSearch(lxState.searchParams, lxSearchResponse);
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
		// Dispatch loading animation event if explicit search. Default search dispatches event separately.
		if (event.lxSearchParams.searchType.equals(SearchType.EXPLICIT_SEARCH)) {
			Events.post(new Events.LXShowLoadingAnimation());
		}
		cleanup();
		setToolbarTitles(event.lxSearchParams);
		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		sortFilterWidget.bind(null);
		sortFilterButton.setVisibility(View.GONE);
		searchResultObserver.searchType = event.lxSearchParams.searchType;
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
		searchSubscription = lxServices.lxSearchSortFilter(event.lxSearchParams,
			areExternalFiltersSupplied ? new LXSortFilterMetadata(filters) : null,
			areExternalFiltersSupplied ? searchResultFilterObserver : searchResultObserver);

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
		searchSubscription = lxServices.lxSearchSortFilter(null, event.lxSortFilterMetadata, searchResultFilterObserver);
	}

	@Subscribe
	public void onLXFilterDoneClicked(Events.LXFilterDoneClicked event) {
		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		AdTracker.trackFilteredLXSearchResults(lxState.searchParams, searchResponse);
	}

	private void setupToolbar() {
		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);
		toolbar.setNavigationIcon(navIcon);
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

	private void setToolbarTitles(LXSearchParams searchParams) {
		toolBarDetailText.setText(searchParams.location);
		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMd(searchParams.startDate), DateUtils.localDateToMMMd(searchParams.endDate));
		toolBarSubtitleText.setText(dateRange);
		toolBarSubtitleText.setVisibility(View.VISIBLE);
	}

	public void animationStart(boolean forward) {
		searchTop = toolBarSearchText.getTop() - toolbarTwo.getTop();
		toolbar.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(searchTop);
		toolBarSubtitleText.setTranslationY(searchTop);
	}

	public void animationUpdate(float f, boolean forward) {
		float yTrans = forward ?  - (searchTop * -f) : (searchTop * (1 - f));
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
}
