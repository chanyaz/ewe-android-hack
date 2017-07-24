package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.RecyclerDividerDecoration;

/**
 * An ItemDecoration that accomplishes this pattern:
 * | |____0____| |
 * | |_1_| |_2_| |
 * | |_3_| |_4_| |
 * | |____5____| |
 * | |_6_| |_7_| |
 * | |_8_| |_9_| |
 * | |___10____| |
 * | |_11| |12_| |
 * | etc etc etc |
 **/

class LaunchListDividerDecoration extends RecyclerDividerDecoration {

	private int mTop;
	private int mBottom;
	private int mLeft;
	private int mRight;
	private int mMiddle;

	LaunchListDividerDecoration(Context context) {
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

		LaunchListAdapter adapter = (LaunchListAdapter) parent.getAdapter();

		int recyclerViewChildIndex = parent.getChildAdapterPosition(view);
		int actualPosition = parent.getChildAdapterPosition(view) - adapter.getOffset();
		int itemViewType = adapter.getItemViewType(recyclerViewChildIndex);

		boolean isLobView = itemViewType == LaunchDataItem.LOB_VIEW;
		boolean isStatic = LaunchListAdapter.isStaticCard(itemViewType);

		if (isLobView) {
			outRect.left = 0;
			outRect.right = 0;
		}
		// Big guys (0, 5, 10, etc)
		else if (actualPosition % 5 == 0 || isStatic) {
			outRect.left = mLeft;
			outRect.right = mRight;
		}
		// Right column (2, 4, 7, 9, etc)
		else if ((actualPosition % 5) % 2 == 0) {
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
