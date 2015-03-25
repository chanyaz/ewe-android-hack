package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoryListWidget extends FrameLayout {

	public CarCategoryListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.category_list)
	public RecyclerView recyclerView;

	@InjectView(R.id.search_error_widget)
	ErrorWidget errorScreen;

	CarCategoriesListAdapter adapter;

	private static final String PICASSO_TAG = "CAR_CATEGORY_LIST";
	private static final int LIST_DIVIDER_HEIGHT = 12;
	private static final int CARDS_FOR_LOADING_ANIMATION = 3;

	private CarSearchParams mParams;

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

		recyclerView.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, toolbarSize, 0, false));
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
	public void onCarsSearchFailed(Events.CarsSearchFailed event) {
		Events.post(new Events.CarsKickOffSearchCall(mParams));
	}

	@Subscribe
	public void onCarsShowSearchResults(Events.CarsShowSearchResults event) {
		adapter.cleanup();
		recyclerView.setVisibility(View.VISIBLE);
		errorScreen.setVisibility(View.GONE);
		adapter.setCategories(event.results.categories);
		adapter.loadingState = false;
		adapter.notifyDataSetChanged();
	}

	@Subscribe
	public void onCarShowLoadingAnimation(Events.CarsShowLoadingAnimation event) {
		recyclerView.setVisibility(View.VISIBLE);
		List<CategorizedCarOffers> elements = createDummyListForAnimation();
		adapter.loadingState = true;
		errorScreen.setVisibility(View.GONE);
		adapter.setCategories(elements);
		adapter.notifyDataSetChanged();
	}

	@Subscribe
	public void onCarsShowSearchResultsError(Events.CarsShowSearchResultsError event) {
		recyclerView.setVisibility(View.GONE);
		errorScreen.bind(event.error);
		errorScreen.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		mParams = event.carSearchParams;
	}

	// Create list to show cards for loading animation
	public List<CategorizedCarOffers> createDummyListForAnimation() {
		List<CategorizedCarOffers> elements = new ArrayList<CategorizedCarOffers>(CARDS_FOR_LOADING_ANIMATION);
		for (int i = 0; i < CARDS_FOR_LOADING_ANIMATION; i++) {
			elements.add(new CategorizedCarOffers());
		}
		return elements;
	}

}
