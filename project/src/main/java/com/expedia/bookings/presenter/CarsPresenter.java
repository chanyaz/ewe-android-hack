package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarsPresenter extends FrameLayout {
	public CarsPresenter(Context context) {
		super(context);
	}

	public CarsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.car_category_list)
	ViewGroup carCategoryList;

	@InjectView(R.id.widget_car_params)
	ViewGroup widgetCarParams;

	private Subscription carSearchSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		carCategoryList.setVisibility(View.GONE);
		widgetCarParams.setVisibility(View.VISIBLE);
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
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Log.d("CarsPresenter - carSearch.onNext");
			CarDb.carSearch = carSearch;
		}
	};

	@Subscribe
	public void onSearchResultsComplete(Events.CarsShowSearchResults event) {
		widgetCarParams.setVisibility(View.GONE);
		carCategoryList.setVisibility(View.VISIBLE);
	}
}
