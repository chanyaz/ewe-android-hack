package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarsResultsPresenter extends Presenter {

	public CarsResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.loading)
	View loading;

	@InjectView(R.id.categories)
	View categories;

	@InjectView(R.id.details)
	View details;

	private Subscription searchSubscription;

	@Override
	protected void onDetachedFromWindow() {
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
			hide(loading);
			show(categories);
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		show(loading);
		cleanup();
		searchSubscription = CarDb.getCarServices()
			.carSearch(event.carSearchParams, searchObserver);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		show(details);
	}
}
