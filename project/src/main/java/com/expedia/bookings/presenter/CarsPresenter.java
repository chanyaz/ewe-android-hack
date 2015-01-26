package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.enums.CarsState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.CarSearchParamsWidget;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarsPresenter extends FrameLayout {

	private Subscription carSearchSubscription;
	private CarsState mState;

	@InjectView(R.id.widget_car_params)
	CarSearchParamsWidget widgetCarParams;

	@InjectView(R.id.widget_car_progress_indicator)
	ViewGroup carProgressIndicator;

	@InjectView(R.id.car_category_list)
	ViewGroup carCategoryList;

	public CarsPresenter(Context context) {
		super(context);
	}

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		widgetCarParams.setVisibility(View.GONE);
		carCategoryList.setVisibility(View.GONE);
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
		mState = state;
		setVisibilityForState(state);
	}

	private void setVisibilityForState(CarsState state) {
		switch (state) {
		case SEARCH:
			widgetCarParams.setVisibility(View.VISIBLE);
			carProgressIndicator.setVisibility(View.GONE);
			carCategoryList.setVisibility(View.GONE);
			break;
		case LOADING:
			widgetCarParams.setVisibility(View.INVISIBLE);
			carProgressIndicator.setVisibility(View.VISIBLE);
			carCategoryList.setVisibility(View.GONE);
		case RESULTS:
			widgetCarParams.setVisibility(View.VISIBLE);
			carProgressIndicator.setVisibility(View.GONE);
			carCategoryList.setVisibility(View.VISIBLE);
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
	public void toggleProgressIndicator(Events.CarsShowListLoading event) {
		carProgressIndicator.setVisibility(carProgressIndicator.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
	}

	@Subscribe
	public void onSearchResultsComplete(Events.CarsShowSearchResults event) {
		widgetCarParams.setVisibility(View.GONE);
		carCategoryList.setVisibility(View.VISIBLE);
	}
}
