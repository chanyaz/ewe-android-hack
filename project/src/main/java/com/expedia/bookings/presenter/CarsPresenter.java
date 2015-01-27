package com.expedia.bookings.presenter;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.enums.CarsState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.CarCheckoutWidget;
import com.expedia.bookings.widget.CarSearchParamsWidget;
import com.expedia.bookings.widget.CategoryDetailsWidget;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarsPresenter extends FrameLayout {

	private Subscription carSearchSubscription;
	private Stack<CarsState> stateStack;

	@InjectView(R.id.widget_car_params)
	CarSearchParamsWidget widgetCarParams;

	@InjectView(R.id.widget_car_progress_indicator)
	ViewGroup carProgressIndicator;

	@InjectView(R.id.car_category_list)
	ViewGroup carCategoryList;

	@InjectView(R.id.car_details)
	CategoryDetailsWidget detailsWidget;

	@InjectView(R.id.car_checkout)
	CarCheckoutWidget checkoutWidget;

	public CarsPresenter(Context context) {
		this(context, null);
	}

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		stateStack = new Stack<>();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Events.unregister(this);
		if (carSearchSubscription != null) {
			carSearchSubscription.unsubscribe();
		}
	}

	public void show(CarsState state) {
		if (stateStack.isEmpty() || state != stateStack.peek()) {
			stateStack.push(state);
		}
		setState(state);
	}

	/**
	 * @return true if consumed back press
	 */
	public boolean handleBackPress() {
		if (stateStack.isEmpty()) {
			return false;
		}
		else {
			stateStack.pop();
			if (stateStack.isEmpty()) {
				return false;
			}

			// TODO put LOADING state into a "CarResults" presenter
			if (stateStack.peek() == CarsState.LOADING) {
				stateStack.pop();
			}

			show(stateStack.peek());
			return true;
		}
	}

	private void setState(CarsState state) {
		widgetCarParams.setVisibility(View.GONE);
		carProgressIndicator.setVisibility(View.GONE);
		carCategoryList.setVisibility(View.GONE);
		detailsWidget.setVisibility(View.GONE);
		checkoutWidget.setVisibility(View.GONE);

		switch (state) {
		case SEARCH:
			widgetCarParams.setVisibility(View.VISIBLE);
			break;
		case LOADING:
			carProgressIndicator.setVisibility(View.VISIBLE);
			break;
		case RESULTS:
			carCategoryList.setVisibility(View.VISIBLE);
			break;
		case DETAILS:
			detailsWidget.setCategorizedOffers(state.offers);
			detailsWidget.setVisibility(View.VISIBLE);
			break;
		case CHECKOUT:
			checkoutWidget.setOffer(state.offer);
			checkoutWidget.setVisibility(View.VISIBLE);
			break;
		default:
			throw new UnsupportedOperationException("CarsPresenter.show() invoked with unsupported state");
		}
	}

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		CarDb.setSearchParams(event.carSearchParams);

		carSearchSubscription = CarDb.getCarServices()
			.carSearch(event.carSearchParams, carSearchSubscriber);
	}

	private Observer<CarSearch> carSearchSubscriber = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			Log.d("CarsPresenter - carSearch.onCompleted");
			Events.post(new Events.CarsShowSearchResults());
		}

		@Override
		public void onError(Throwable e) {
			Log.d("CarsPresenter - carSearch.onError", e);
			Events.post(new Events.CarsShowListLoading());
			widgetCarParams.showAlertMessage(R.string.error_car, R.string.ok);
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Log.d("CarsPresenter - carSearch.onNext");
			CarDb.carSearch = carSearch;
		}
	};

	@Subscribe
	public void onShowListLoading(Events.CarsShowListLoading event) {
		show(CarsState.LOADING);
	}

	@Subscribe
	public void onSearchResultsComplete(Events.CarsShowSearchResults event) {
		show(CarsState.RESULTS);
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		CarsState detailsState = CarsState.DETAILS;
		detailsState.offers = event.categorizedCarOffers;
		show(detailsState);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		CarsState checkoutState = CarsState.CHECKOUT;
		checkoutState.offer = event.carOffer;
		show(checkoutState);
	}
}
