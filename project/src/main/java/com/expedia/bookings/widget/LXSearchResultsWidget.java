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
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXSearchResultsWidget extends FrameLayout {

	public LXSearchResultsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final int LIST_DIVIDER_HEIGHT = 12;

	private static final int CARDS_FOR_LOADING_ANIMATION = 3;

	@InjectView(R.id.lx_search_results_list)
	RecyclerView recyclerView;

	@InjectView(R.id.lx_search_error_widget)
	LXErrorWidget errorScreen;

	private LXResultsListAdapter adapter;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);
		recyclerView.setLayoutManager(layoutManager);

		recyclerView
			.addItemDecoration(
				new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, 0, 0, false));
		recyclerView.setHasFixedSize(true);

		adapter = new LXResultsListAdapter();
		recyclerView.setAdapter(adapter);
		errorScreen.setVisibility(View.GONE);
		errorScreen.setToolbarVisibility(GONE);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public void bind(List<LXActivity> activities) {
		adapter.cleanup();
		recyclerView.setVisibility(View.VISIBLE);
		errorScreen.setVisibility(View.GONE);
		adapter.setActivities(activities);
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
		adapter.setDummyActivities(elements);
	}

	// Create list to show cards for loading animation
	private List<LXActivity> createDummyListForAnimation() {
		List<LXActivity> elements = new ArrayList<LXActivity>(CARDS_FOR_LOADING_ANIMATION);
		for (int i = 0; i < CARDS_FOR_LOADING_ANIMATION; i++) {
			elements.add(new LXActivity());
		}
		return elements;
	}
}
