package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoryListWidget extends FrameLayout {

	public CarCategoryListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.category_list)
	RecyclerView recyclerView;

	CarCategoriesListAdapter adapter;

	private static final String PICASSO_TAG = "CAR_CATEGORY_LIST";
	private static final int LIST_DIVIDER_HEIGHT = 12;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		// category list
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(layoutManager);

		TypedValue typedValue = new TypedValue();
		int[] textSizeAttr = new int[] { android.R.attr.actionBarSize };
		TypedArray a = getContext().obtainStyledAttributes(typedValue.data, textSizeAttr);
		int toolbarSize = (int) a.getDimension(0, 44);

		recyclerView.addItemDecoration(new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, toolbarSize, toolbarSize, false));
		recyclerView.setHasFixedSize(true);
		recyclerView.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));

		adapter = new CarCategoriesListAdapter();
		recyclerView.setAdapter(adapter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onCarsShowSearchResults(Events.CarsShowSearchResults event) {
		adapter.setCategories(event.results.categories);
		adapter.notifyDataSetChanged();
	}
}
