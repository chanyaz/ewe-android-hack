package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.ApiException;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.expedia.bookings.widget.LXSortFilterWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class LXResultsPresenter extends Presenter {

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

	@InjectView(R.id.sort_filter_button)
	LinearLayout sortFilterButton;

	// This is here just for an animation
	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@OnClick(R.id.sort_filter_button)
	public void onSortFilterClicked() {
		show(sortFilterWidget);
	}

	public LXResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Transitions
	private Presenter.Transition searchResultsToSortFilter = new Presenter.Transition(LXSearchResultsWidget.class, LXSortFilterWidget.class) {
		private int sortFilterWidgetHeight;

		@Override
		public void startTransition(boolean forward) {
			int parentHeight = getHeight();
			sortFilterWidgetHeight = sortFilterWidget.getHeight();
			float pos = forward ? parentHeight + sortFilterWidgetHeight : sortFilterWidgetHeight;
			sortFilterWidget.setTranslationY(pos);
			sortFilterWidget.setVisibility(View.VISIBLE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float pos = forward ? sortFilterWidgetHeight + (-f * sortFilterWidgetHeight) : (f * sortFilterWidgetHeight);
			sortFilterWidget.setTranslationY(pos);
		}

		@Override
		public void endTransition(boolean forward) {
			sortFilterWidget.setTranslationY(forward ? 0 : sortFilterWidgetHeight);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			sortFilterWidget.setTranslationY(forward ? 0 : sortFilterWidgetHeight);
			sortFilterWidget.setVisibility(forward ? VISIBLE : GONE);
		}
	};

	private DefaultTransition setUpLoading = new DefaultTransition(LXSearchResultsWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			// Do not show loading animation for automation builds.
			if (!ExpediaBookingApp.sIsAutomation) {
				Events.post(new Events.LXShowLoadingAnimation());
			}
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Events.register(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addTransition(searchResultsToSortFilter);
		addDefaultTransition(setUpLoading);
		setupToolbar();
		searchResultsWidget.setPadding(0, Ui.toolbarSizeWithStatusBar(getContext()), 0, 0);
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

	private Observer<LXSearchResponse> searchResultObserver = new Observer<LXSearchResponse>() {

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
			else if (e instanceof ApiException) {
				ApiException apiException = (ApiException) e;
				Events.post(new Events.LXShowSearchError(apiException.apiError, SearchType.EXPLICIT_SEARCH));
				return;
			}

			//Bucket all other errors as Unknown to give some feedback to the user
			ApiError apiError = new ApiError();
			apiError.errorCode = ApiError.Code.UNKNOWN_ERROR;
			Events.post(new Events.LXShowSearchError(apiError, SearchType.EXPLICIT_SEARCH));
			sortFilterButton.setVisibility(View.GONE);
		}

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			// Search Results Omniture Tracking on load of search screen.
			OmnitureTracking.trackAppLXSearch(getContext(), lxState.searchParams, lxSearchResponse);
			Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));
			sortFilterWidget.bind(lxSearchResponse.filterCategories);
			searchResultsWidget.bind(lxSearchResponse.activities);
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
			sortFilterButton.setVisibility(View.VISIBLE);
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
		if (!ExpediaBookingApp.sIsAutomation) {
			Events.post(new Events.LXShowLoadingAnimation());
		}
		cleanup();
		setToolbarTitles(event.lxSearchParams);
		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		sortFilterWidget.bind(null);
		sortFilterButton.setVisibility(View.GONE);
		searchSubscription = lxServices.lxSearchSortFilter(event.lxSearchParams, sortFilterWidget.filterSortEventStream(), searchResultObserver);
	}

	private void setupToolbar() {
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
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
	}

	private void setToolbarTitles(LXSearchParams searchParams) {
		toolbar.setTitle(searchParams.location);

		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMdd(searchParams.startDate), DateUtils.localDateToMMMdd(searchParams.endDate));
		toolbar.setSubtitle(dateRange);
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
			Strings.equals(getCurrentState(), LXSearchResultsWidget.class.getName()) ? toolbarBackground.getAlpha() : 1f);
	}
}
