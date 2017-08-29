package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoryListWidget extends FrameLayout {

	public CarCategoryListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.category_list)
	public RecyclerView recyclerView;

	CarCategoriesListAdapter adapter;

	private static final String PICASSO_TAG = "CAR_CATEGORY_LIST";
	private static final int LIST_DIVIDER_HEIGHT = 12;
	private static final int CARDS_FOR_LOADING_ANIMATION = 3;

	private CarSearchParam mParams;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		// category list
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(layoutManager);

		int toolbarSize = Ui.getToolbarSize(getContext());

		//  Footer : Height of filter view container to make the view scrollable.
		int filterViewHeight = (int) getResources().getDimension(R.dimen.lx_sort_filter_container_height);

		recyclerView.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), 0, LIST_DIVIDER_HEIGHT, 0, LIST_DIVIDER_HEIGHT,
				toolbarSize + Ui.getStatusBarHeight(getContext()), filterViewHeight, false));
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
	public void onCarsIsFiltered(Events.CarsIsFiltered event) {
		recyclerView.setVisibility(View.VISIBLE);
		adapter.setCategories(event.filteredCarSearch.categories);
		CarCategoriesListAdapter.loadingState = false;
		adapter.notifyDataSetChanged();
	}

	@Subscribe
	public void onCarsShowResultsForProductKey(Events.CarsShowResultsForProductKey event) {
		recyclerView.setVisibility(View.VISIBLE);
		adapter.setCategories(event.productKeyCarSearch.categories);
		CarCategoriesListAdapter.loadingState = false;
		adapter.notifyDataSetChanged();
		Events.post(new Events.CarsShowProductKeyDetails(event.productKeyCarSearch));
	}

	@Subscribe
	public void onCarsShowSearchResults(Events.CarsShowSearchResults event) {
		recyclerView.setVisibility(View.VISIBLE);
		adapter.setCategories(event.results.categories);
		CarCategoriesListAdapter.loadingState = false;
		adapter.notifyDataSetChanged();
		AdTracker.trackCarResult(event.results, mParams);
	}

	@Subscribe
	public void onCarShowLoadingAnimation(Events.CarsShowLoadingAnimation event) {
		recyclerView.setVisibility(View.VISIBLE);
		List<CategorizedCarOffers> elements = createDummyListForAnimation();
		CarCategoriesListAdapter.loadingState = true;
		adapter.setCategories(elements);
		adapter.notifyDataSetChanged();
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
