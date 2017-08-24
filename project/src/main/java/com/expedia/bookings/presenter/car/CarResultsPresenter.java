package com.expedia.bookings.presenter.car;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.LeftToRightTransition;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CarCategoryDetailsWidget;
import com.expedia.bookings.widget.CarCategoryListWidget;
import com.expedia.bookings.widget.CarFilterWidget;
import com.expedia.bookings.widget.ErrorWidget;
import com.expedia.bookings.widget.FilterButtonWithCountWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.subjects.PublishSubject;

public class CarResultsPresenter extends Presenter {

	private ArrowXDrawable navIcon;

	public CarResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Inject
	CarServices carServices;

	@InjectView(R.id.categories)
	CarCategoryListWidget categories;

	@InjectView(R.id.details)
	CarCategoryDetailsWidget details;

	// This is here just for an animation
	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.sort_toolbar)
	FilterButtonWithCountWidget filterToolbar;

	@InjectView(R.id.category_sort_toolbar)
	FilterButtonWithCountWidget categoryFilterToolbar;

	@InjectView(R.id.toolbar_dropshadow)
	View toolbarDropshadow;

	@InjectView(R.id.filter)
	CarFilterWidget filter;

	@InjectView(R.id.search_error_widget)
	ErrorWidget errorScreen;

	@InjectView(R.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	@InjectView(R.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	@InjectView(R.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	@InjectView(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	private Subscription searchSubscription;
	private CarSearchParam searchedParams;
	private CategorizedCarOffers selectedCategorizedCarOffers;
	private CarSearch unfilteredSearch = new CarSearch();
	private int searchTop;
	private CarSearch filteredSearch = new CarSearch();
	final PublishSubject filterDonePublishSubject = PublishSubject.create();
	final PublishSubject locationDescriptionSubject = PublishSubject.create();

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(categoriesToDetails);
		addTransition(categoriesToFilter);
		addTransition(filterToError);
		addTransition(categoriesToError);
		addTransition(detailsToFilter);
		addDefaultTransition(setUpLoading);

		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationContentDescription(getResources().getString(R.string.toolbar_nav_icon_cont_desc));
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setSubtitleTextColor(Color.WHITE);
		toolbar.inflateMenu(R.menu.cars_results_menu);
		toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_search:
					Events.post(new Events.CarsGoToOverlay());
					return true;
				}
				return false;
			}
		});
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		categories.recyclerView.setOnScrollListener(recyclerScrollListener);
		details.offerList.setOnScrollListener(recyclerScrollListener);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbarBackground.getLayoutParams().height += statusBarHeight;
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		filterToolbar.setFilterText(getResources().getString(R.string.filter));
		filterToolbar.setContentDescription(getResources().getString(R.string.cars_filter_button_cont_desc));
		categoryFilterToolbar.setFilterText(getResources().getString(R.string.filter));
		categoryFilterToolbar.setContentDescription(getResources().getString(R.string.cars_filter_button_cont_desc));
		details.getSearchCarOfferPublishSubject().subscribe(carOfferObserver);
		filterDonePublishSubject.subscribe(filterDoneObserver);
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	public void cleanup() {
		if (searchSubscription != null) {
			searchSubscription.unsubscribe();
			searchSubscription = null;
		}
	}

	private final Observer<CarSearch> searchWithProductKeyObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			if (e instanceof ApiError) {
				handleInputValidationErrors((ApiError) e);
			}
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Events.post(new Events.CarsShowResultsForProductKey(carSearch));
			handleCarSearchResults(carSearch);
		}
	};

	private final Observer<CarSearch> searchObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("CarSearch - onError", e);

			if (RetrofitUtils.isNetworkError(e)) {
				showSearchErrorDialog(R.string.error_no_internet);
				return;
			}

			if (e instanceof ApiError) {
				handleInputValidationErrors((ApiError) e);
				OmnitureTracking.trackAppCarNoResults((ApiError) e);
			}
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Events.post(new Events.CarsShowSearchResults(carSearch));
			handleCarSearchResults(carSearch);
		}
	};

	private Observer<CarSearch> searchFilterObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			if (e instanceof ApiError) {
				handleInputValidationErrors((ApiError) e);
				return;
			}
			throw new OnErrorNotImplementedException(e);
		}

		@Override
		public void onNext(CarSearch filteredCarSearch) {
			filteredSearch = filteredCarSearch;
			CategorizedCarOffers filteredBucket = null;

			if (selectedCategorizedCarOffers != null) {
				if (filteredCarSearch.hasDisplayLabel(selectedCategorizedCarOffers.carCategoryDisplayLabel)) {
					filteredBucket = filteredCarSearch.getFromDisplayLabel(selectedCategorizedCarOffers.carCategoryDisplayLabel);
				}
				else {
					filteredBucket = new CategorizedCarOffers();
					filteredBucket.carCategoryDisplayLabel = selectedCategorizedCarOffers.carCategoryDisplayLabel;
					filteredBucket.category = selectedCategorizedCarOffers.category;
					filteredBucket.type = selectedCategorizedCarOffers.type;
				}
			}
			Events.post(new Events.CarsIsFiltered(filteredCarSearch, filteredBucket));
		}
	};

	private Observer<CarSearch> filterDoneObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(CarSearch carSearch) {
			AdTracker.trackFilteredCarResult(filteredSearch, searchedParams);
		}
	};

	private void handleCarSearchResults(CarSearch carSearch) {
		unfilteredSearch = carSearch;
		categoryFilterToolbar.setVisibility(View.VISIBLE);
		categoryFilterToolbar.showNumberOfFilters(0);
		show(categories, FLAG_CLEAR_TOP);
		bindFilter(carSearch);
		OmnitureTracking.trackAppCarSearch(searchedParams, unfilteredSearch.categories.size());
	}

	private void bindFilter(CarSearch carSearch) {
		filter.bind(carSearch, filterDonePublishSubject);
	}

	/* handle CAR_SEARCH_WINDOW_VIOLATION detail errors and show default error screen
	* for all the other search error codes.
	*/
	private void handleInputValidationErrors(ApiError apiError) {
		if (apiError.errorDetailCode == null) {
			showDefaultError(apiError);
			return;
		}

		switch (apiError.errorDetailCode) {
		case DROP_OFF_DATE_TOO_LATE:
			showInvalidInputErrorDialog(R.string.error_date_too_far);
			break;
		case SEARCH_DURATION_TOO_SMALL:
			showInvalidInputErrorDialog(R.string.reservation_time_too_short);
			break;
		case SEARCH_DURATION_TOO_LARGE:
			showInvalidInputErrorDialog(R.string.error_date_too_far);
			break;
		case PICKUP_DATE_TOO_EARLY:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		case PICKUP_DATE_IN_THE_PAST:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		case PICKUP_DATE_AND_DROP_OFF_DATE_ARE_THE_SAME:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		default:
			showInvalidInputErrorDialog(
				Phrase.from(getContext(), R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format()
					.toString());
			break;
		}
	}

	private void showDefaultError(ApiError apiError) {
		Events.post(new Events.CarsShowSearchResultsError(apiError));
	}

	private void showSearchErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsKickOffSearchCall(searchedParams));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	private void showInvalidInputErrorDialog(@StringRes int message) {
		showInvalidInputErrorDialog(getResources().getString(message));
	}

	private void showInvalidInputErrorDialog(String message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(message)
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	Transition categoriesToError = new Transition(CarCategoryListWidget.class, ErrorWidget.class) {
		@Override
		public void startTransition(boolean forward) {
			categories.setVisibility(forward ? GONE : VISIBLE);
			errorScreen.setVisibility(forward ? VISIBLE : GONE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
		}

		@Override
		public void endTransition(boolean forward) {
		}
	};

	Transition filterToError = new Transition(CarFilterWidget.class, ErrorWidget.class) {
		@Override
		public void startTransition(boolean forward) {
			filter.setVisibility(forward ? GONE : VISIBLE);
			errorScreen.setVisibility(forward ? VISIBLE : GONE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
		}

		@Override
		public void endTransition(boolean forward) {
		}
	};

	Transition categoriesToDetails = new LeftToRightTransition(this, CarCategoryListWidget.class, CarCategoryDetailsWidget.class) {
		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);

			toolbarBackground.setTranslationX(forward ? 0 : -toolbarBackground.getWidth());
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setAlpha(1f);
			toolbarDropshadow.setAlpha(1f);

			filterToolbar.setVisibility(VISIBLE);
			filterToolbar.setTranslationY(0);
			filterToolbar.showNumberOfFilters(filter.getNumCheckedFilters(forward));

			categoryFilterToolbar.setTranslationY(0);
			categoryFilterToolbar.showNumberOfFilters(filter.getNumCheckedFilters(forward));

		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);

			float translationD = forward ? -toolbarBackground.getWidth() * f : toolbarBackground.getWidth() * (f - 1);
			toolbarBackground.setTranslationX(translationD);
		}

		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (forward) {
				filter.onTransitionToDetails(unfilteredSearch.getFromDisplayLabel(selectedCategorizedCarOffers.carCategoryDisplayLabel));
			}
			else {
				selectedCategorizedCarOffers = null;
				filter.onTransitionToResults();
			}
			filterToolbar.setVisibility(forward ? VISIBLE : GONE);
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setTranslationX(0);
			toolbarBackground.setAlpha(forward ? 0f : 1f);
			toolbarDropshadow.setAlpha(forward ? 0f : 1f);

			details.reset();

			if (forward) {
				setToolBarDetailsText();
			}
			else {
				setToolBarResultsText();
			}
			AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
		}
	};

	private void setToolBarDetailsText() {
		if (selectedCategorizedCarOffers != null) {
			toolBarDetailText.setText(selectedCategorizedCarOffers.carCategoryDisplayLabel);
		}
	}

	private void setToolBarResultsText() {
		if (searchedParams != null) {
			String dateTimeRange = DateFormatUtils.formatStartEndDateTimeRange(getContext(), searchedParams.getStartDateTime(),
				searchedParams.getEndDateTime(), false);
			if (!TextUtils.isEmpty(searchedParams.getOriginDescription())) {
				toolBarDetailText.setText(searchedParams.getOriginDescription());
			}
			else if (unfilteredSearch.categories != null && unfilteredSearch.categories.size() > 0 && unfilteredSearch.categories.get(0).offers.size() > 0) {
				String locationDescription = unfilteredSearch.categories.get(0).offers.get(0).pickUpLocation.locationDescription;
				toolBarDetailText.setText(locationDescription);
				locationDescriptionSubject.onNext(locationDescription);
			}
			toolBarSubtitleText.setText(dateTimeRange);
		}
	}

	DefaultTransition setUpLoading = new DefaultTransition(CarCategoryListWidget.class.getName()) {
		@Override
		public void endTransition(boolean forward) {
			categories.setVisibility(View.VISIBLE);
			details.setVisibility(View.GONE);
			errorScreen.setVisibility(View.GONE);
		}
	};

	@OnClick(R.id.sort_toolbar)
	public void onFilterClick() {
		show(filter);
	}

	@OnClick(R.id.category_sort_toolbar)
	public void onCategoryFilterClick() {
		show(filter);
	}

	Transition categoriesToFilter = new Transition(CarCategoryListWidget.class, CarFilterWidget.class, new DecelerateInterpolator(2f), 500) {
		@Override
		public void startTransition(boolean forward) {
			filter.setVisibility(VISIBLE);
			categories.setVisibility(VISIBLE);
			toolbar.setVisibility(VISIBLE);
			categoryFilterToolbar.setVisibility(VISIBLE);
			if (!forward) {
				categoryFilterToolbar.showNumberOfFilters(filter.getNumCheckedFilters(false /*isDetails*/));
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			filter.setTranslationY(filter.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
			filter.setVisibility(forward ? VISIBLE : GONE);
			categories.setVisibility(forward ? GONE : VISIBLE);
			toolbar.setVisibility(forward ? GONE : VISIBLE);
			categoryFilterToolbar.setVisibility(forward ? GONE : VISIBLE);
			filter.setTranslationY(forward ? 0 : filter.getHeight());
			if (!forward) {
				OmnitureTracking.trackAppCarSearch(searchedParams, unfilteredSearch.categories.size());
			}
			else {
				OmnitureTracking.trackAppCarFilter();
				filter.setFocusToToolbarForAccessibility();
			}
		}
	};

	Transition detailsToFilter = new Transition(CarCategoryDetailsWidget.class, CarFilterWidget.class, new DecelerateInterpolator(2f), 500) {
		@Override
		public void startTransition(boolean forward) {
			filter.setVisibility(VISIBLE);
			details.setVisibility(VISIBLE);
			toolbar.setVisibility(VISIBLE);
			filterToolbar.setVisibility(VISIBLE);
			if (!forward) {
				filterToolbar.showNumberOfFilters(filter.getNumCheckedFilters(true /*isDetails*/));
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			filter.setTranslationY(filter.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
			filter.setVisibility(forward ? VISIBLE : GONE);
			details.setVisibility(forward ? GONE : VISIBLE);
			toolbar.setVisibility(forward ? GONE : VISIBLE);
			filterToolbar.setVisibility(forward ? GONE : VISIBLE);
			filter.setTranslationY(forward ? 0 : filter.getHeight());
			if (forward) {
				OmnitureTracking.trackAppCarFilter();
				filter.setFocusToToolbarForAccessibility();
			}
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		Events.post(new Events.CarsShowLoadingAnimation());
		show(categories, FLAG_CLEAR_BACKSTACK);
		cleanup();
		searchedParams = event.carSearchParams;
		if (event.productKey != null) {
			searchSubscription = carServices.carSearchWithProductKey(searchedParams, event.productKey, searchWithProductKeyObserver);
		}
		else {
			searchSubscription = carServices.carSearch(searchedParams, searchObserver);
		}
		setToolBarResultsText();
	}

	@Subscribe
	public void onShowFilteredSearchResults(Events.CarsShowFilteredSearchResults event) {
		((Activity) getContext()).onBackPressed();
		categoryFilterToolbar.setVisibility(VISIBLE);
		Events.post(new Events.CarsShowSearchResults(unfilteredSearch));
	}

	@Subscribe
	public void onCarFilterDonePressed(Events.CarsFilterDone event) {
		searchSubscription = carServices
			.carFilterSearch(searchFilterObserver, event.carFilter);
		setToolBarResultsText();
	}

	@Subscribe
	public void onCarsShowSearchResultsError(Events.CarsShowSearchResultsError event) {
		errorScreen.bind(event.error);
		show(errorScreen, Presenter.FLAG_CLEAR_BACKSTACK);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		show(details, FLAG_CLEAR_TOP);
		selectedCategorizedCarOffers = event.categorizedCarOffers;
		setToolBarDetailsText();
	}

	@Subscribe
	public void onShowDetailsForProductKey(Events.CarsShowProductKeyDetails event) {
		if (CollectionUtils.isNotEmpty(event.productKeyCarSearch.categories)) {
			show(details, FLAG_CLEAR_TOP);
			selectedCategorizedCarOffers = event.productKeyCarSearch.categories.get(0);
			setToolBarDetailsText();
		}
	}

	@Subscribe
	public void onShowLoadingState(Events.CarsShowLoadingAnimation event) {
		filterToolbar.setVisibility(View.INVISIBLE);
		categoryFilterToolbar.setVisibility(View.INVISIBLE);
	}

	@Subscribe
	public void onCarSearchParams(Events.CarsKickOffSearchCall event) {
		cleanup();
		searchSubscription = carServices.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}

	public float animationStart() {
		toolbar.setVisibility(VISIBLE);
		searchTop = toolBarSearchText.getTop() - toolbarTwo.getTop();
		toolBarDetailText.setTranslationY(searchTop);
		toolBarSubtitleText.setTranslationY(searchTop);

		return (CarCategoryDetailsWidget.class.getName().equals(getCurrentState())) ? toolbarBackground.getAlpha() : 1f;
	}

	public void animationUpdate(float f, boolean forward) {
		float alphaD = forward ? Math.abs(1 - f) : f;
		toolbar.setAlpha(alphaD);
		float yTrans = forward ?  - (searchTop * -f) : (searchTop * (1 - f));
		toolBarDetailText.setTranslationY(yTrans);
		toolBarSubtitleText.setTranslationY(yTrans);
		navIcon.setParameter(Math.abs(1 - alphaD));
		errorScreen.animationUpdate(Math.abs(1 - alphaD));
	}

	public void animationFinalize() {
		toolbarBackground.setAlpha(
			Strings.equals(getCurrentState(), CarCategoryDetailsWidget.class.getName()) ? toolbarBackground.getAlpha()
				: 1f);
		toolbarDropshadow.setAlpha(
			Strings.equals(getCurrentState(), CarCategoryDetailsWidget.class.getName()) ? toolbarBackground.getAlpha()
				: 1f);
		toolbar.setVisibility(VISIBLE);
		toolbarBackground.setVisibility(VISIBLE);
		toolBarDetailText.setTranslationY(0);
		toolBarSubtitleText.setTranslationY(0);
		navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.BACK.getType());
		errorScreen.animationUpdate(ArrowXDrawableUtil.ArrowDrawableType.BACK.getType());
		AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
	}

	RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener() {
		private int scrolledDistance = 0;
		private Boolean isTalkBackEnabled = AccessibilityUtil.isTalkBackEnabled(getContext());

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			int heightOfButton = filterToolbar.getHeight();
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				if (scrolledDistance > heightOfButton / 2) {
					filterToolbar.animate().translationY(heightOfButton).setInterpolator(new DecelerateInterpolator()).start();
					categoryFilterToolbar.animate().translationY(heightOfButton).setInterpolator(new DecelerateInterpolator()).start();
				}
				else if (scrolledDistance != 0) {
					filterToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
					categoryFilterToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
				}
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			if (recyclerView == details.offerList && details.offerList.getChildAt(0) != null) {
				float ratio = details.parallaxScrollHeader();
				toolbarBackground.setAlpha(ratio);
				toolbarDropshadow.setAlpha(ratio);
			}
			if (!isTalkBackEnabled) {
				int heightOfButton = filterToolbar.getHeight();
				if (scrolledDistance > 0) {

					scrolledDistance = Math.min(heightOfButton, scrolledDistance + dy);
					filterToolbar.setTranslationY(Math.min(heightOfButton, scrolledDistance));
					categoryFilterToolbar.setTranslationY(Math.min(heightOfButton, scrolledDistance));
				}
				else {
					scrolledDistance = Math.max(0, scrolledDistance + dy);
					filterToolbar.setTranslationY(Math.min(scrolledDistance, 0));
					categoryFilterToolbar.setTranslationY(Math.min(scrolledDistance, 0));
				}
			}
		}
	};

	@Override
	public boolean back() {
		if (CarFilterWidget.class.getName().equals(getCurrentState()) && filter.isFilteredToZeroResults()) {
			filter.getDynamicFeedbackWidget().animateDynamicFeedbackWidget();
			return true;
		}
		return super.back();
	}

	private Observer<SearchCarOffer> carOfferObserver = new Observer<SearchCarOffer>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(SearchCarOffer searchCarOffer) {
			AdTracker.trackCarDetails(searchedParams, searchCarOffer);
		}
	};
}
