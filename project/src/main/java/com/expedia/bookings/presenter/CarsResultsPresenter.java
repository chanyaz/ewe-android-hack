package com.expedia.bookings.presenter;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.ButterKnife.Action;
import butterknife.InjectView;
import butterknife.InjectViews;
import rx.Observer;
import rx.Subscription;

public class CarsResultsPresenter extends FrameLayout {
	public CarsResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectViews({ R.id.loading, R.id.categories, R.id.details })
	List<View> all;

	@InjectView(R.id.loading)
	View loading;

	@InjectView(R.id.categories)
	View categories;

	@InjectView(R.id.details)
	View details;

	private Subscription searchSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		cleanup();
		super.onDetachedFromWindow();
	}

	private void cleanup() {
		if (searchSubscription != null) {
			searchSubscription.unsubscribe();
			searchSubscription = null;
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
			showCategories();
		}
	};

	private static final Action<View> SHOW = new Action<View>() {
		@Override
		public void apply(View view, int index) {
			view.setVisibility(View.VISIBLE);
		}
	};

	private static final Action<View> HIDE = new Action<View>() {
		@Override
		public void apply(View view, int index) {
			view.setVisibility(View.GONE);
		}
	};

	private void showLoading() {
		showOnly(loading);
	}

	private void showCategories() {
		showOnly(categories);
	}

	private void showDetails() {
		showOnly(details);
	}

	private void showOnly(View v) {
		ButterKnife.apply(all, HIDE);
		SHOW.apply(v, 0);
	}

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		showLoading();
		cleanup();
		searchSubscription = CarDb.getCarServices()
			.carSearch(event.carSearchParams, searchObserver);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		showDetails();
	}
}
