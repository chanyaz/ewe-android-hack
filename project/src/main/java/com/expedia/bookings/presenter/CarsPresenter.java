package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
	}

	@Subscribe
	public void onSearchResultsComplete(Events.EnableCarsSearchResults event) {
		widgetCarParams.setVisibility(View.GONE);
		carCategoryList.setVisibility(View.VISIBLE);
	}
}
