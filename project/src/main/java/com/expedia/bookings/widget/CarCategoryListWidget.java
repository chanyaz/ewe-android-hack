package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoryListWidget extends FrameLayout {


	public CarCategoryListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CarCategoryListWidget(Context context) {
		super(context);
	}

	@InjectView(R.id.recycler_view_category)
	RecyclerView recyclerView;

	CarCategoriesListAdapter listAdapter;

	private LinearLayoutManager mLayoutManager;

	private static final String PICASSO_TAG = "CAR_CATEGORY_LIST";
	private static final int LIST_DIVIDER_HEIGHT = 8;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		setUpRecyclerView();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Events.unregister(this);
	}

	private void setUpRecyclerView() {
		mLayoutManager = new LinearLayoutManager(getContext());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mLayoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(mLayoutManager);

		recyclerView.addItemDecoration(new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, LIST_DIVIDER_HEIGHT));
		recyclerView.setHasFixedSize(true);
		recyclerView.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));
		listAdapter = new CarCategoriesListAdapter();
		recyclerView.setAdapter(listAdapter);
	}

	@Subscribe
	public void onSignalForList(Events.CarsShowSearchResults event) {
		listAdapter.setCategories(CarDb.carSearch.categories);
		listAdapter.notifyDataSetChanged();
	}
}
