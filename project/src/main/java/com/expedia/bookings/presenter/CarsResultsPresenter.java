package com.expedia.bookings.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
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

	private ProgressDialog createTripDialog;
	private Subscription searchSubscription;
	private Subscription createTripSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		createTripDialog = new ProgressDialog(getContext());
		createTripDialog.setMessage("Preparing checkout...");
		createTripDialog.setIndeterminate(true);
	}

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
			hide(loading);
			show(categories);
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
		show(loading);
		cleanup();
		searchSubscription = CarDb.getCarServices()
			.carSearch(event.carSearchParams, searchObserver);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		show(details);
	}

	@Subscribe
	public void onOfferSelected(Events.CarsKickOffCreateTrip event) {
		createTripDialog.show();
		cleanup();
		createTripSubscription = CarDb.getCarServices()
			.createTrip(event.offer.productKey, event.offer.fare.total.amount.toString(), createTripObserver);
	}
}
