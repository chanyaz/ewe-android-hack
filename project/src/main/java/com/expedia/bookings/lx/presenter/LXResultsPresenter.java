package com.expedia.bookings.lx.presenter;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FilterButtonWithCountWidget;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.expedia.bookings.widget.LXSortFilterWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class LXResultsPresenter extends Presenter {

	private static final int ANIMATION_DURATION = 400;
	@Inject
	LxServices lxServices;

	@Inject
	LXState lxState;

	@InjectView(R2.id.lx_search_results_widget)
	LXSearchResultsWidget searchResultsWidget;

	Disposable searchSubscription;

	@InjectView(R2.id.toolbar)
	Toolbar toolbar;

	@InjectView(R2.id.sort_filter_widget)
	LXSortFilterWidget sortFilterWidget;

	@InjectView(R2.id.sort_filter_button_container)
	FilterButtonWithCountWidget sortFilterButton;

	// This is here just for an animation
	@InjectView(R2.id.toolbar_background)
	View toolbarBackground;

	@InjectView(R2.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	@InjectView(R2.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	@InjectView(R2.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	@InjectView(R2.id.toolbar_two)
	LinearLayout toolbarTwo;

	@InjectView(R2.id.transparent_view)
	View transparentView;

	@InjectView(R2.id.sort_filter_widget_animate_view)
	View sortFilterWidgetAnimateView;

	@InjectView(R2.id.mip_srp_banner_image)
	ImageView mipSrpBannerImage;

	@InjectView(R2.id.mip_srp_banner_brand)
	android.widget.TextView mipSrpBannerBrand;

	@InjectView(R2.id.mip_srp_banner_discount)
	android.widget.TextView mipSrpBannerDiscount;

	@InjectView(R2.id.mip_srp_banner)
	LinearLayout mipSrpBanner;

	@OnClick(R2.id.transparent_view)
	public void onTransparentViewClick() {
		show(searchResultsWidget, FLAG_CLEAR_TOP);
	}

	private int searchTop;
	private ArrowXDrawable navIcon;

	private SearchResultObserver searchResultObserver = new SearchResultObserver();

	private SearchResultFilterObserver searchResultFilterObserver = new SearchResultFilterObserver();
	private LXSearchResponse searchResponse;

	private boolean lxFilterTextSearchEnabled;
	private boolean isMipEnabled;

	@OnClick(R2.id.sort_filter_button)
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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).lxComponent().inject(this);
		addTransition(searchResultsToSortFilter);

		lxFilterTextSearchEnabled = AbacusFeatureConfigManager
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXFilterSearch);

		isMipEnabled = AbacusFeatureConfigManager.isBucketedForTest(getContext(), AbacusUtils.EBAndroidLXMIP);

		setupToolbar();
		searchResultsWidget.getRecyclerView().setOnScrollListener(recyclerScrollListener);
		sortFilterButton.setFilterText(getResources().getString(R.string.sort_and_filter));
		sortFilterWidget.doneButtonClicked.subscribe(unit -> onLXFilterDoneClicked());
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
				LXNavUtils.handleLXSearchFailure(e, searchType);
			}

			sortFilterButton.setVisibility(View.GONE);
		}

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			searchResponse = lxSearchResponse;
			// Search Results Omniture Tracking on load of search screen.
			OmnitureTracking.trackAppLXRTRABTest();
			Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));

			if (lxSearchResponse.promoDiscountType != null) {
				if (isMipEnabled  && !Constants.MOD_PROMO_TYPE.equals(lxSearchResponse.promoDiscountType)) {
					lxState.setPromoDiscountType(searchResponse.promoDiscountType);
					mipSrpBanner.setVisibility(VISIBLE);

					mipSrpBannerBrand.setText(Phrase.from(getContext(), R.string.mip_srp_header_brand_TEMPLATE).put("brand", BuildConfig.brand).format().toString());

					mipSrpBannerDiscount.setText(Phrase.from(getContext(),
							R.string.mip_srp_header_discount_TEMPLATE).put("discount",
							LXUtils.getMaxPromoDiscount(lxSearchResponse.activities)).format().toString());
					int mipImageId = LXDataUtils.getMIPImageId(lxSearchResponse.promoDiscountType);
					if (mipImageId == 0) {
						mipSrpBanner.setVisibility(GONE);
					}
					else {
						mipSrpBannerImage.setImageResource(mipImageId);
					}
				}
				else {
					mipSrpBanner.setVisibility(GONE);
					if (AbacusFeatureConfigManager.isBucketedForTest(getContext(), AbacusUtils.EBAndroidLXMOD)) {
						lxState.setPromoDiscountType(Constants.MOD_PROMO_TYPE);
					}
				}
			}
			trackLXSearch();
			searchResultsWidget.bind(lxSearchResponse.activities, lxSearchResponse.promoDiscountType, searchType);
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
			sortFilterWidget.bind(lxSearchResponse.filterCategories);
			sortFilterButton.setVisibility(View.VISIBLE);
			sortFilterButton.showNumberOfFilters(0);
			AdTracker.trackLXSearchResults(lxState.searchParams, lxSearchResponse);

			handleActivityDetailsDeeplink(lxSearchResponse);
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
			sortFilterButton.setVisibility(View.VISIBLE);
			sortFilterButton.showNumberOfFilters(sortFilterWidget.getNumberOfSelectedFilters());
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
		// Dispatch loading animation event if explicit search. Default search dispatches event separately.
		if (event.lxSearchParams.getSearchType().equals(SearchType.EXPLICIT_SEARCH)) {
			Events.post(new Events.LXShowLoadingAnimation());
		}

		cleanup();
		sortFilterWidget.bind(null);
		sortFilterButton.setVisibility(View.GONE);
		searchResultFilterObserver.searchType = event.lxSearchParams.getSearchType();

		String filters = null;
		boolean areExternalFiltersSupplied = false;
		if (Strings.isNotEmpty(event.lxSearchParams.getFilters())) {
			filters = event.lxSearchParams.getFilters();
			areExternalFiltersSupplied = true;
		}

		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
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

		if (areExternalFiltersSupplied) {
			sortFilterWidget.setSelectedFilterCategories(filters);
		}
	}

	@Subscribe
	public void onLXSearchError(Events.LXShowSearchError event) {
		if (event.searchType.equals(SearchType.DEFAULT_SEARCH)
			&& event.error.getErrorCode() != ApiError.Code.LX_SEARCH_NO_RESULTS) {
			toolBarDetailText.setText(getResources().getString(R.string.lx_error_current_location_toolbar_text));
			toolBarSubtitleText.setVisibility(View.GONE);
		}
	}

	@Subscribe
	public void onLXFilterChanged(Events.LXFilterChanged event) {
		searchSubscription = lxServices
			.lxSearchSortFilter(null, event.lxSortFilterMetadata, searchResultFilterObserver,
				lxFilterTextSearchEnabled);
	}

	public void onLXFilterDoneClicked() {
		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		trackLXSearch();
		AdTracker.trackFilteredLXSearchResults(lxState.searchParams, searchResponse);
	}

	@Subscribe
	public void onLXShowLoadingAnimation(Events.LXShowLoadingAnimation event) {
		searchResultsWidget.setVisibility(VISIBLE);
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

	@Override
	public boolean back() {
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
			OmnitureTracking.trackAppLXSearch(lxState.searchParams, searchResponse, lxState.getPromoDiscountType());
		}
	}
}
