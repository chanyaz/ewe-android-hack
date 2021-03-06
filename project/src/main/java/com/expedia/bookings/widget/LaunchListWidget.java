package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class LaunchListWidget extends RecyclerView {

	private static final String PICASSO_TAG = "LAUNCH_LIST";

	private LaunchListAdapter adapter;

	private View header;

	public LaunchListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
			StaggeredGridLayoutManager.VERTICAL);
		setLayoutManager(layoutManager);

		header = LayoutInflater.from(getContext()).inflate(R.layout.snippet_launch_list_header, null);
		adapter = new LaunchListAdapter(header);
		setAdapter(adapter);
		addItemDecoration(new LaunchListDividerDecoration(getContext(), false));
		setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));
	}

	public void setHeaderPaddingTop(float paddingTop) {
		int left = header.getPaddingLeft();
		int top = (int) paddingTop;
		int right = header.getPaddingRight();
		int bottom = header.getPaddingBottom();
		header.setPadding(left, top, right, bottom);
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
	public void onNearbyHotelsSearchResults(Events.LaunchHotelSearchResponse event) {
		String headerTitle = getResources().getString(R.string.nearby_deals_title);
		Db.setLaunchListHotelData(event.topHotels);
		adapter.setListData(event.topHotels, headerTitle);
		adapter.notifyDataSetChanged();
	}

	@Subscribe
	public void onCollectionDownloadComplete(Events.CollectionDownloadComplete event) {
		String headerTitle = event.collection.title;
		if (ProductFlavorFeatureConfiguration.getInstance().getCollectionCount() != 0
			&& ProductFlavorFeatureConfiguration.getInstance().getCollectionCount() < event.collection.locations
			.size()) {
			event.collection.locations = event.collection.locations
				.subList(0, ProductFlavorFeatureConfiguration.getInstance().getCollectionCount());
		}
		adapter.setListData(event.collection.locations, headerTitle);
		adapter.notifyDataSetChanged();
	}

	public void showListLoadingAnimation() {
		List<Integer> elements = createDummyListForAnimation();
		String headerTitle = getResources().getString(R.string.loading_header);
		adapter.setListData(elements, headerTitle);
		adapter.notifyDataSetChanged();
	}

	// Create list to show cards for loading animation
	public List<Integer> createDummyListForAnimation() {
		List<Integer> elements = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			elements.add(0);
		}
		return elements;
	}

	/**
	 An ItemDecoration that accomplishes this pattern:
	 | |____0____| |
	 | |_1_| |_2_| |
	 | |_3_| |_4_| |
	 | |____5____| |
	 | |_6_| |_7_| |
	 | |_8_| |_9_| |
	 | |___10____| |
	 | |_11| |12_| |
	 | etc etc etc |
	  **/

	private class LaunchListDividerDecoration extends RecyclerDividerDecoration {

		int mTop;
		int mBottom;
		int mLeft;
		int mRight;
		int mMiddle;

		private LaunchListDividerDecoration(Context context, boolean drawDivider) {

			mTop = 0;
			mLeft = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_side);
			mMiddle = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_middle);
			mBottom = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_bottom);
			mRight = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_side);

		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			outRect.top = mTop;
			outRect.bottom = mBottom;

			// Because of a header
			int pos = parent.getChildPosition(view) - 1;
			// Big guys (0, 5, 10, etc)
			if (pos % 5 == 0) {
				outRect.left = mLeft;
				outRect.right = mRight;
			}
			// Right column (2, 4, 7, 9, etc)
			else if ((pos % 5) % 2 == 0) {
				outRect.left = mMiddle / 2;
				outRect.right = mRight;
			}
			// Left column (1, 3, 6, 8, etc)
			else {
				outRect.left = mLeft;
				outRect.right = mMiddle / 2;
			}
		}
	}
}
