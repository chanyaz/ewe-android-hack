package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXSearchResultsWidget extends FrameLayout {

	public LXSearchResultsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.lx_search_results_list)
	RecyclerView recyclerView;

	private LXResultsListAdapter adapter;

	private static final int LIST_DIVIDER_HEIGHT = 4;

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
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onLXSearchAvailable(Events.LXShowSearchResults event) {
		adapter.setActivities(event.activities);
	}
}
