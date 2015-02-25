package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NearbyHotelsWidget extends FrameLayout {

	private static final String PICASSO_TAG = "NEARBY_HOTELS_LIST";

	@InjectView(R.id.nearby_hotel_list)
	RecyclerView nearbyHotels;

	private NearbyHotelsListAdapter adapter;

	public NearbyHotelsWidget(Context context) {
		super(context);
	}

	public NearbyHotelsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
			StaggeredGridLayoutManager.VERTICAL);
		nearbyHotels.setLayoutManager(layoutManager);

		nearbyHotels.addItemDecoration(new NearbyHotelsDividerDecoration(getContext(), 8, false));

		adapter = new NearbyHotelsListAdapter();
		nearbyHotels.setAdapter(adapter);
		nearbyHotels.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));
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
	public void onNearbyHotelsSearchResults(Events.NearbyHotelSearchResults event) {
		adapter.setNearbyHotels(event.topTen);
		adapter.notifyDataSetChanged();
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

	private class NearbyHotelsDividerDecoration extends RecyclerDividerDecoration {

		int mTop;
		int mBottom;
		int mLeft;
		int mRight;

		// Divider Separator
		boolean shouldDrawDivider = false;

		private NearbyHotelsDividerDecoration(Context context, int margin, boolean drawDivider) {

			shouldDrawDivider = drawDivider;

			mTop = (int) context.getResources().getDisplayMetrics().density * margin / 2;
			mBottom = (int) context.getResources().getDisplayMetrics().density * margin / 2;
			mLeft = mTop * 2;
			mRight = mTop * 2;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			outRect.top = mTop;
			outRect.bottom = mBottom;

			int pos = parent.getChildPosition(view);
			// Big guys (0, 5, 10, etc)
			if (pos % 5 == 0) {
				outRect.left = mLeft;
				outRect.right = mRight;
			}
			// Right column (2, 4, 7, 9, etc)
			else if ((pos % 5) % 2 == 0) {
				outRect.left = mLeft / 2;
				outRect.right = mRight;
			}
			// Left column (1, 3, 6, 8, etc)
			else {
				outRect.left = mLeft;
				outRect.right = mRight / 2;
			}
		}
	}
}
