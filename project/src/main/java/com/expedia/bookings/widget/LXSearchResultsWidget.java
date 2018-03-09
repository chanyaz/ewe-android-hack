package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXSearchResultsWidget extends FrameLayout {

	private boolean userBucketedForRTRTest;

	public LXSearchResultsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final int LIST_DIVIDER_HEIGHT = 12;
	private static final int CARDS_FOR_LOADING_ANIMATION = 3;

	@InjectView(R.id.lx_search_results_list)
	RecyclerView recyclerView;

	@InjectView(R.id.lx_search_error_widget)
	LXErrorWidget errorScreen;

	public LXResultsListAdapter adapter;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(layoutManager);

		//  Footer : Height of filter view container to make the view scrollable.
		int filterViewHeight = (int) getResources()
			.getDimension(Ui.obtainThemeResID(getContext(), R.attr.skin_lxResultsBottomPadding));

		recyclerView.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), 0, LIST_DIVIDER_HEIGHT, 0, LIST_DIVIDER_HEIGHT,
				0, filterViewHeight, false));
		recyclerView.setHasFixedSize(true);

		adapter = new LXResultsListAdapter();
		recyclerView.setAdapter(adapter);
		errorScreen.setVisibility(View.GONE);
		errorScreen.setToolbarVisibility(GONE);
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

	public void bind(List<LXActivity> activities, String discountType) {
		adapter.setItems(activities, discountType);
		update();
	}

	public void bind(List<LXActivity> activities) {
		adapter.setItems(activities);
		adapter.initializeScrollDepthMap(activities.size());
		update();
	}

	public void update() {
		recyclerView.setVisibility(View.VISIBLE);
		recyclerView.getLayoutManager().scrollToPosition(0);
		errorScreen.setVisibility(View.GONE);
		adapter.setUserBucketedForRTRTest(userBucketedForRTRTest);
	}

	@Subscribe
	public void onLXSearchError(Events.LXShowSearchError event) {
		recyclerView.setVisibility(View.GONE);
		errorScreen.bind(event.error, event.searchType);
		errorScreen.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onLXShowLoadingAnimation(Events.LXShowLoadingAnimation event) {
		recyclerView.setVisibility(View.VISIBLE);
		errorScreen.setVisibility(View.GONE);
		List<LXActivity> elements = createDummyListForAnimation();
		adapter.setDummyItems(elements);
	}

	@Subscribe
	public void onLXSearchFilterResultsReady(Events.LXSearchFilterResultsReady event) {
		if (CollectionUtils.isNotEmpty(event.filteredActivities)) {
			adapter.setItems(event.filteredActivities);
		}
	}

	// Create list to show cards for loading animation
	private List<LXActivity> createDummyListForAnimation() {
		List<LXActivity> elements = new ArrayList<LXActivity>(CARDS_FOR_LOADING_ANIMATION);
		for (int i = 0; i < CARDS_FOR_LOADING_ANIMATION; i++) {
			elements.add(new LXActivity());
		}
		return elements;
	}

	public RecyclerView getRecyclerView() {
		return recyclerView;
	}

	public void setUserBucketedForRTRTest(boolean userBucketedForRTRTest) {
		this.userBucketedForRTRTest = userBucketedForRTRTest;
	}
}
