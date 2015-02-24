package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
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
	private CarSearchParams mParams;
	private CategorizedCarOffers mOffer;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(loadingToCategories);
		addTransition(categoriesToDetails);
		addTransition(loadingToDetails);
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
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_search:
					Events.post(new Events.CarsGoToOverlay());
					break;
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

		details.offerList.setOnScrollListener(parallaxScrollListener);

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
			show(categories, FLAG_CLEAR_BACKSTACK);
		}
	};

	Transition loadingToCategories = new VisibilityTransition(this, ProgressBar.class.getName(),
		CarCategoryListWidget.class.getName());
	Transition loadingToDetails = new VisibilityTransition(this, ProgressBar.class.getName(),
		CarCategoryDetailsWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			toolbarBackground.setVisibility(View.VISIBLE);
			toolbarBackground.setTranslationX(0);
		}
	};

	Transition categoriesToDetails = new Transition(CarCategoryListWidget.class,
		CarCategoryDetailsWidget.class) {

		@Override
		public void startTransition(boolean forward) {
			toolbarBackground.setTranslationX(forward ? 0 : -toolbarBackground.getWidth());
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setAlpha(1f);

			categories.setTranslationX(forward ? 0 : -categories.getWidth());
			categories.setVisibility(VISIBLE);

			details.setTranslationX(forward ? details.getWidth() : 0);
			details.setVisibility(VISIBLE);
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
			}

		}
	};

	private void setToolBarDetailsText() {
		if (mOffer != null) {
			toolbar.setTitle(mOffer.category.toString());
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
		show(loading, FLAG_CLEAR_BACKSTACK);
		cleanup();
		mParams = event.carSearchParams;
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		show(details);
		mOffer = event.categorizedCarOffers;
		setToolBarDetailsText();
	}

	@Subscribe
	public void onOfferSelected(Events.CarsKickOffCreateTrip event) {
		createTripDialog.show();
		cleanup();
		createTripSubscription = carServices
			.createTrip(event.offer.productKey, event.offer.fare.total.amount.toString(), createTripObserver);
	}

	public void animationStart(boolean forward) {
		toolbarBackground.setTranslationY(forward ? 0 : -toolbarBackground.getHeight());
		toolbar.setTranslationY(forward ? 0 : 50);
	}

	public void animationUpdate(float f, boolean forward) {
		toolbarBackground
			.setTranslationY(forward ? -toolbarBackground.getHeight() * f : -toolbarBackground.getHeight() * (1 - f));
		toolbar.setTranslationY(forward ? 50 * f : 50 * (1 - f));
	}

	public void animationFinalize(boolean forward) {
		toolbarBackground.setTranslationY(forward ? -toolbarBackground.getHeight() : 0);
		toolbar.setTranslationY(forward ? 50 : 0);
	}

	RecyclerView.OnScrollListener parallaxScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			float ratio = details.parallaxScrollHeader();
			toolbarBackground.setAlpha(ratio);
			moveSortBar(dy);

		}
	};

	private void moveSortBar(int dy) {
		float y = dy + sortToolbar.getTranslationY();
		sortToolbar.setTranslationY(dy < 0 ? Math.max(y, 0) : Math.min(y, sortToolbar.getHeight()));
	}
}
