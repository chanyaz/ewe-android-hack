package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class CarResultsWidget extends LinearLayout {

	public CarResultsWidget(Context context) {
		super(context);
	}

	public CarResultsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CarResultsWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@InjectView(R.id.recycler_view_category)
	RecyclerView recyclerView;

	CarsListAdapter carsListAdapter;

	private LinearLayoutManager mLayoutManager;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		setUpRecyclerView();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (carSearchSubscription != null) {
			carSearchSubscription.unsubscribe();
		}
	}

	//////////////////////////
	// Car Search Download

	Subscription carSearchSubscription;

	@OnClick(R.id.download_now_btn)
	public void startDownload() {
		Ui.showToast(getContext(), "clicked");
		carSearchSubscription = CarServices
			.getInstance()
			.doBoringCarSearch(carSearchSubscriber);

	}

	private Observer<CarSearch> carSearchSubscriber = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			Log.d("TestCarSearchWidget - onCompleted");
			Ui.showToast(getContext(), "onComplete");
			carsListAdapter.setCategoriesTestList(CarDb.carSearch.getCategoriesFromResponse());
			carsListAdapter.notifyDataSetChanged();
		}

		@Override
		public void onError(Throwable e) {
			Log.d("TestCarSearchWidget - onError", e);
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Log.d("TestCarSearchWidget - onNext");
			CarDb.carSearch = carSearch;
		}
	};

	private void setUpRecyclerView() {
		mLayoutManager = new LinearLayoutManager(getContext());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mLayoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setHasFixedSize(true);
		carsListAdapter = new CarsListAdapter();
		recyclerView.setAdapter(carsListAdapter);
	}
}
