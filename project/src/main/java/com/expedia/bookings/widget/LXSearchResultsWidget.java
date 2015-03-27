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
import butterknife.OnClick;

public class LXSearchResultsWidget extends FrameLayout {

	public LXSearchResultsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final int LIST_DIVIDER_HEIGHT = 12;

	private static final int CARDS_FOR_LOADING_ANIMATION = 3;

	@InjectView(R.id.lx_search_results_list)
	RecyclerView recyclerView;

	@InjectView(R.id.lx_search_failure)
	FrameLayout searchFailure;

	private LXResultsListAdapter adapter;

	@OnClick(R.id.edit_search)
	public void onEditSearch() {
		Events.post(new Events.LXShowSearchWidget());
	}

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
		searchFailure.setVisibility(View.GONE);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onLXSearchAvailable(Events.LXSearchResultsAvailable event) {
		adapter.cleanup();
		recyclerView.setVisibility(View.VISIBLE);
		searchFailure.setVisibility(View.GONE);
		adapter.loadingState = false;
		adapter.setActivities(event.lxSearchResponse.activities);
	}

	@Subscribe
	public void onLXSearchError(Events.LXShowSearchError event) {
		recyclerView.setVisibility(View.GONE);
		searchFailure.setVisibility(View.VISIBLE);
	}

	@Subscribe
	public void onLXShowLoadingAnimation(Events.LXShowLoadingAnimation event) {
		recyclerView.setVisibility(View.VISIBLE);
		List<LXActivity> elements = createDummyListForAnimation();
		adapter.loadingState = true;
		adapter.setActivities(elements);
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
