package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CarCategoryDetailsWidget;
import com.expedia.bookings.widget.CarCategoryListWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarsResultsPresenter extends Presenter {

	public CarsResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Inject
	CarServices carServices;

	@InjectView(R.id.loading)
	ProgressBar loading;

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
	ViewGroup sortToolbar;

	private ProgressDialog createTripDialog;
	private Subscription searchSubscription;
	private Subscription createTripSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(loadingToCategories);
		addTransition(categoriesToDetails);
		addDefaultTransition(setUpLoading);

		createTripDialog = new ProgressDialog(getContext());
		createTripDialog.setMessage("Preparing checkout...");
		createTripDialog.setIndeterminate(true);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setSubtitleTextColor(Color.WHITE);
		toolbar.inflateMenu(R.menu.cars_results_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_search:
					Events.post(new Events.CarsGoToSearch());
					break;
				}
				return false;
			}
		});

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
			throw new RuntimeException(e);
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Events.post(new Events.CarsShowSearchResults(carSearch));
			show(categories, true);
		}
	};

	Transition loadingToCategories = new VisibilityTransition(this, ProgressBar.class.getName(), CarCategoryListWidget.class.getName());
	Transition categoriesToDetails = new VisibilityTransition(this, CarCategoryListWidget.class.getName(), CarCategoryDetailsWidget.class.getName());
	DefaultTransition setUpLoading = new DefaultTransition(ProgressBar.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			loading.setVisibility(View.VISIBLE);
			categories.setVisibility(View.GONE);
			details.setVisibility(View.GONE);
		}
	};

	private Observer<CarCreateTripResponse> createTripObserver = new Observer<CarCreateTripResponse>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			throw new RuntimeException(e);
		}

		@Override
		public void onNext(CarCreateTripResponse response) {
			createTripDialog.dismiss();
			Events.post(new Events.CarsShowCheckout(response));
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(loading, true);
		cleanup();
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), event.carSearchParams,
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);

		toolbar.setTitle(event.carSearchParams.originDescription);
		toolbar.setSubtitle(dateTimeRange);

		searchSubscription = carServices.carSearch(event.carSearchParams, searchObserver);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		show(details);
		toolbar.setTitle(event.categorizedCarOffers.category.toString());
	}

	@Subscribe
	public void onOfferSelected(Events.CarsKickOffCreateTrip event) {
		createTripDialog.show();
		cleanup();
		createTripSubscription = carServices
			.createTrip(event.offer.productKey, event.offer.fare.total.amount.toString(), createTripObserver);
	}

}
