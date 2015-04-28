package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.CarCategoryDetailsWidget;
import com.expedia.bookings.widget.CarCategoryListWidget;
import com.expedia.bookings.widget.CarFilterWidget;
import com.expedia.bookings.widget.ErrorWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class CarResultsPresenter extends Presenter implements UserAccountRefresher.IUserAccountRefreshListener {

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

	UserAccountRefresher userAccountRefresher;

	@InjectView(R.id.sort_toolbar)
	ViewGroup filterToolbar;

	@InjectView(R.id.filter)
	CarFilterWidget filter;

	@InjectView(R.id.filter_number_text)
	TextView filterNumber;

	@InjectView(R.id.filter_icon)
	View filterIcon;

	@InjectView(R.id.search_error_widget)
	ErrorWidget errorScreen;

	private ProgressDialog createTripDialog;
	private Subscription searchSubscription;
	private Subscription createTripSubscription;
	private CarSearchParams mParams;
	private CategorizedCarOffers mOffer;
	private SearchCarOffer offer;
	private String category;
	private CarSearch search = new CarSearch();
	String lastState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(categoriesToDetails);
		addTransition(categoriesToFilter);
		addTransition(categoriesToError);
		addTransition(detailsToFilter);
		addDefaultTransition(setUpLoading);

		createTripDialog = new ProgressDialog(getContext());
		createTripDialog.setMessage(getResources().getString(R.string.preparing_checkout_message));
		createTripDialog.setIndeterminate(true);
		createTripDialog.setCancelable(false);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setSubtitleTextColor(Color.WHITE);
		toolbar.inflateMenu(R.menu.cars_results_menu);
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance);
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

		userAccountRefresher = new UserAccountRefresher(getContext(), LineOfBusiness.CARS, this);
		filterNumber.setVisibility(GONE);
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
		if (createTripSubscription != null) {
			createTripSubscription.unsubscribe();
			createTripSubscription = null;
		}
	}

	private Observer<CarSearch> searchObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			OmnitureTracking.trackAppCarNoResults(getContext(), e.getMessage());
			Log.e("CarSearch - onError", e);
			show(categories, FLAG_CLEAR_BACKSTACK);

			if (RetrofitUtils.isNetworkError(e)) {
				showSearchErrorDialog(R.string.error_no_internet);
				return;
			}

			if (e instanceof ApiError) {
				handleInputValidationErrors((ApiError) e);
				return;
			}
		}

		@Override
		public void onNext(CarSearch carSearch) {
			search = carSearch;
			filterToolbar.setVisibility(View.VISIBLE);
			showNumberOfFilters(0);
			Events.post(new Events.CarsShowSearchResults(carSearch));
			show(categories, FLAG_CLEAR_TOP);
			bindFilter(carSearch);
			OmnitureTracking.trackAppCarSearch(getContext(), mParams, carSearch.categories.size());
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
		}

		@Override
		public void onNext(CarSearch carSearch) {
			if (lastState.equals(CarCategoryDetailsWidget.class.getName())) {
				Events.post(new Events.CarsIsFilteredOnDetails(carSearch, category));
			}
			else {
				Events.post(new Events.CarsIsFiltered(carSearch, category));
			}
		}
	};

	private void bindFilter(CarSearch carSearch) {
		filter.bind(carSearch);
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
			showInvalidInputErrorDialog(R.string.drop_off_too_far_error);
			break;
		case SEARCH_DURATION_TOO_SMALL:
			showInvalidInputErrorDialog(R.string.reservation_time_too_short);
			break;
		case SEARCH_DURATION_TOO_LARGE:
			showInvalidInputErrorDialog(R.string.date_out_of_range);
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
			showInvalidInputErrorDialog(R.string.error_server);
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
					Events.post(new Events.CarsKickOffSearchCall(mParams));
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
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	Transition categoriesToError = new Transition(CarFilterWidget.class, ErrorWidget.class) {
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

		@Override
		public void finalizeTransition(boolean forward) {
		}
	};

	Transition categoriesToDetails = new Transition(CarCategoryListWidget.class,
		CarCategoryDetailsWidget.class) {

		@Override
		public void startTransition(boolean forward) {
			toolbarBackground.setTranslationX(forward ? 0 : -toolbarBackground.getWidth());
			lastState = forward ? CarCategoryDetailsWidget.class.getName() : CarCategoryListWidget.class.getName();
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setAlpha(1f);

			categories.setTranslationX(forward ? 0 : -categories.getWidth());
			categories.setVisibility(VISIBLE);

			details.setTranslationX(forward ? details.getWidth() : 0);
			details.setVisibility(VISIBLE);

			int numCheckedFilters = filter.carFilter.carSupplierCheckedFilter.size() + (filter.carFilter.hasUnlimitedMileage ? 1 : 0) + (filter.carFilter.hasAirConditioning ? 1 : 0);

			if (!forward) {
				numCheckedFilters += filter.carFilter.carCategoryCheckedFilter.size();
				Events.post(new Events.CarsFilterDone(filter.carFilter));
			}
			filterToolbar.setTranslationY(0);
			showNumberOfFilters(numCheckedFilters);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translationD = forward ? -toolbarBackground.getWidth() * f : toolbarBackground.getWidth() * (f - 1);
			toolbarBackground.setTranslationX(translationD);

			float translationC = forward ? -categories.getWidth() * f : categories.getWidth() * (f - 1);
			categories.setTranslationX(translationC);

			float translationX = forward ? -details.getWidth() * (f - 1) : details.getWidth() * f;
			details.setTranslationX(translationX);
		}

		@Override
		public void endTransition(boolean forward) {
			if (!forward) {
				filter.hideCarCategories(false);
				filter.rebind();
			}
		}

		@Override
		public void finalizeTransition(boolean forward) {
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setTranslationX(0);
			toolbarBackground.setAlpha(forward ? 0f : 1f);

			categories.setVisibility(forward ? GONE : VISIBLE);
			categories.setTranslationX(0);

			details.setVisibility(forward ? VISIBLE : GONE);
			details.setTranslationX(0);

			details.reset();

			if (forward) {
				setToolBarDetailsText();
			}
			else {
				setToolBarResultsText();
				category = null;
			}
		}
	};

	private void setToolBarDetailsText() {
		if (mOffer != null) {
			toolbar.setTitle(mOffer.carCategoryDisplayLabel);
		}
	}

	private void setToolBarResultsText() {
		if (mParams != null) {
			String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), mParams,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
			toolbar.setTitle(mParams.originDescription);
			toolbar.setSubtitle(dateTimeRange);
		}
	}

	DefaultTransition setUpLoading = new DefaultTransition(CarCategoryListWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			lastState = CarCategoryListWidget.class.getName();
			categories.setVisibility(View.VISIBLE);
			details.setVisibility(View.GONE);
			errorScreen.setVisibility(View.GONE);
		}
	};

	// Create Trip network handling

	private Observer<CarCreateTripResponse> createTripObserver = new Observer<CarCreateTripResponse>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("CarCreateTrip - onError", e);
			createTripDialog.dismiss();

			if (RetrofitUtils.isNetworkError(e)) {
				showOnCreateNoInternetErrorDialog(R.string.error_no_internet);
			}
			else {
				showCreateTripErrorDialog();
			}
		}

		@Override
		public void onNext(CarCreateTripResponse response) {
			createTripDialog.dismiss();
			Events.post(new Events.CarsShowCheckout(response));
		}
	};

	private void showCreateTripErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.error_server))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	private void showOnCreateNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsKickOffCreateTrip(offer));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	@OnClick(R.id.sort_toolbar)
	public void onFilterClick() {
		show(filter);
	}

	Transition categoriesToFilter = new Transition(CarCategoryListWidget.class, CarFilterWidget.class) {
		@Override
		public void startTransition(boolean forward) {
			filter.setVisibility(View.VISIBLE);
			filter.hideCarCategories(false);
			filter.hideCarResultsSupplies(false);
			if (!forward) {
				int numCheckedFilters = filter.carFilter.carCategoryCheckedFilter.size() + filter.carFilter.carSupplierCheckedFilter.size() + (filter.carFilter.hasUnlimitedMileage ? 1 : 0) + (filter.carFilter.hasAirConditioning ? 1 : 0);
				showNumberOfFilters(numCheckedFilters);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			filter.setTranslationY(filter.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			filter.setVisibility(forward ? VISIBLE : GONE);
			filter.setTranslationY(forward ? 0 : filter.getHeight());
		}
	};


	private void showNumberOfFilters(int number) {
		filterNumber.setText(String.valueOf(number));
		boolean hasCheckedFilters = number > 0;
		filterNumber.setVisibility(hasCheckedFilters ? VISIBLE : GONE);
		filterIcon.setVisibility(hasCheckedFilters ? GONE : VISIBLE);
	}

	Transition detailsToFilter = new Transition(CarCategoryDetailsWidget.class, CarFilterWidget.class) {
		@Override
		public void startTransition(boolean forward) {
			filter.setVisibility(View.VISIBLE);
			filter.hideCarCategories(true);
			filter.hideCarResultsSupplies(true);
			if (!forward) {
				int numCheckedFilters = filter.carFilter.carSupplierCheckedFilter.size() + (filter.carFilter.hasUnlimitedMileage ? 1 : 0) + (filter.carFilter.hasAirConditioning ? 1 : 0);
				showNumberOfFilters(numCheckedFilters);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translatePercentage = forward ? 1f - f : f;
			filter.setTranslationY(filter.getHeight() * translatePercentage);
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			filter.setVisibility(forward ? VISIBLE : GONE);
			filter.setTranslationY(forward ? 0 : filter.getHeight());
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		if (!ExpediaBookingApp.sIsAutomation) {
			Events.post(new Events.CarsShowLoadingAnimation());
		}
		show(categories, FLAG_CLEAR_BACKSTACK);
		cleanup();
		mParams = event.carSearchParams;
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}

	@Subscribe
	public void onShowFilteredSearchResults(Events.CarsShowFilteredSearchResults event) {
		((Activity) getContext()).onBackPressed();
		filterToolbar.setVisibility(View.VISIBLE);
		Events.post(new Events.CarsShowSearchResults(search));
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
		show(errorScreen);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		category = event.categorizedCarOffers.carCategoryDisplayLabel;
		for (CategorizedCarOffers categorizedCarOffer : search.categories) {
			if (categorizedCarOffer.carCategoryDisplayLabel.equals(category)) {
				filter.setFilterVisibilites(categorizedCarOffer.offers);
			}
		}
		show(details, FLAG_CLEAR_TOP);
		mOffer = event.categorizedCarOffers;
		setToolBarDetailsText();
	}

	@Subscribe
	public void onShowResults(Events.CarsShowResults event) {
		show(categories, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowLoadingState(Events.CarsShowLoadingAnimation event) {
		filterToolbar.setVisibility(View.INVISIBLE);
	}

	@Subscribe
	public void onOfferSelected(Events.CarsKickOffCreateTrip event) {
		createTripDialog.show();
		cleanup();
		offer = event.offer;
		userAccountRefresher.ensureAccountIsRefreshed();
	}

	@Subscribe
	public void onCarSearchParams(Events.CarsKickOffSearchCall event) {
		cleanup();
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}

	public void animationStart(boolean forward) {
		toolbarBackground.setTranslationY(forward ? 0 : -toolbarBackground.getHeight());
		toolbar.setTranslationY(forward ? 0 : 50);
		toolbar.setVisibility(VISIBLE);
	}

	public void animationUpdate(float f, boolean forward) {
		toolbarBackground
			.setTranslationY(forward ? -toolbarBackground.getHeight() * f : -toolbarBackground.getHeight() * (1 - f));
		toolbar.setTranslationY(forward ? 50 * f : 50 * (1 - f));
	}

	public void animationFinalize(boolean forward) {
		toolbarBackground.setTranslationY(forward ? -toolbarBackground.getHeight() : 0);
		toolbar.setTranslationY(forward ? 50 : 0);
		toolbar.setVisibility(forward ? GONE : VISIBLE);
		toolbarBackground.setAlpha(
			Strings.equals(getCurrentState(), CarCategoryDetailsWidget.class.getName()) ? toolbarBackground.getAlpha()
				: 1f);
	}

	@Override
	public void onUserAccountRefreshed() {
		createTripSubscription = carServices.createTrip(offer, createTripObserver);
	}

	RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener() {
		private int scrolledDistance = 0;

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			int heightOfButton = filterToolbar.getHeight();
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				if (scrolledDistance > heightOfButton / 2) {
					filterToolbar.animate().translationY(heightOfButton).setInterpolator(new DecelerateInterpolator()).start();
				}
				else if (scrolledDistance != 0) {
					filterToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
				}
				scrolledDistance = 0;
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			if (recyclerView == details.offerList) {
				float ratio = details.parallaxScrollHeader();
				toolbarBackground.setAlpha(ratio);
			}

			int heightOfButton = filterToolbar.getHeight();
			if (scrolledDistance > 0) {
				scrolledDistance = Math.min(heightOfButton, scrolledDistance + dy);
				filterToolbar.setTranslationY(Math.min(heightOfButton, scrolledDistance));
			}
			else {
				scrolledDistance = Math.max(0, scrolledDistance + dy);
				filterToolbar.setTranslationY(Math.min(scrolledDistance, 0));
			}
		}
	};
}
