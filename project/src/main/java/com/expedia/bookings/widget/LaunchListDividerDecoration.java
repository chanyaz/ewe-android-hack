package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;

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

public class LaunchListDividerDecoration extends RecyclerDividerDecoration {

	int mTop;
	int mBottom;
	int mLeft;
	int mRight;
	int mMiddle;
	int headerCount = 1;

	public LaunchListDividerDecoration(Context context, boolean isLobViewAdded) {
		mTop = 0;
		mLeft = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_side);
		mMiddle = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_middle);
		mBottom = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_bottom);
		mRight = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_side);
		if (isLobViewAdded) {
			headerCount = 2;
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.top = mTop;
		outRect.bottom = mBottom;

		int pos = parent.getChildAdapterPosition(view) - headerCount;
		// when we have 2 headers one for lob and another one staff picks or near by hotel header
		if (pos == -2) {
			outRect.left = 0;
			outRect.right = 0;
		}
		// Big guys (0, 5, 10, etc)
		else if (pos % 5 == 0) {
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
