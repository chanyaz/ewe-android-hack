package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXSearchResultsWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
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

	public LXResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Transitions

	private DefaultTransition setUpLoading = new DefaultTransition(LXSearchResultsWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			// Do not show loading animation for automation builds.
			if (!ExpediaBookingApp.sIsAutomation) {
				Events.post(new Events.LXShowLoadingAnimation());
			}
			searchResultsWidget.setVisibility(View.VISIBLE);
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Events.register(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addDefaultTransition(setUpLoading);
		setupToolbar();
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
			Events.post(new Events.LXShowSearchError());
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		}

		@Override
		public void onNext(LXSearchResponse lxSearchResponse) {
			// Search Results Omniture Tracking on load of search screen.
			OmnitureTracking.trackAppLXSearch(getContext(), lxState.searchParams, lxSearchResponse);
			Events.post(new Events.LXSearchResultsAvailable(lxSearchResponse));
			show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		}
	};

	@Subscribe
	public void onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		cleanup();
		setToolbarTitles();
		show(searchResultsWidget, FLAG_CLEAR_BACKSTACK);
		searchSubscription = lxServices.lxSearch(event.lxSearchParams, searchResultObserver);
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
		if (statusBarHeight > 0) {
			int toolbarColor = getContext().getResources().getColor(R.color.lx_primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, searchResultsWidget, toolbarColor));
		}
	}

	private void setToolbarTitles() {
		LXSearchParams searchParams = lxState.searchParams;
		toolbar.setTitle(searchParams.location);

		String dateRange = String.format(getResources().getString(R.string.lx_toolbar_date_range_template),
			DateUtils.localDateToMMMdd(searchParams.startDate), DateUtils.localDateToMMMdd(searchParams.endDate));
		toolbar.setSubtitle(dateRange);
	}

}
