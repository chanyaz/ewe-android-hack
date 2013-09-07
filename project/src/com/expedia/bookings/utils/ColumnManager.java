package com.expedia.bookings.utils;

import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class ColumnManager {

	private int mNumColumns = 0;
	private int mTotalWidth = 0;
	private int[] mColumnWidths;
	private int[] mColumnLefts;

	public ColumnManager(int numColumns) {
		mNumColumns = numColumns;
		mColumnWidths = new int[mNumColumns];
		mColumnLefts = new int[mNumColumns];
	}

	public void setTotalWidth(int totalWidth) {
		mTotalWidth = totalWidth;
		calculate();
	}

	public int getTotalWidth() {
		return mTotalWidth;
	}

	public int getColumnCount() {
		return mNumColumns;
	}

	public int getColWidth(int colIndex) {
		return mColumnWidths[colIndex];
	}

	public int getColLeft(int colIndex) {
		return mColumnLefts[colIndex];
	}

	public int getColRight(int colIndex) {
		return mColumnLefts[colIndex] + mColumnWidths[colIndex];
	}

	private void calculate() {
		int baseColSize = (int) (mTotalWidth / mNumColumns);
		int remainder = (int) (mTotalWidth % mNumColumns);

		int leftPos = mTotalWidth;
		for (int i = (mNumColumns - 1); i >= 0; i--) {
			int colWidth = baseColSize + (remainder > 0 ? 1 : 0);
			leftPos -= colWidth;
			remainder--;

			mColumnWidths[i] = colWidth;
			mColumnLefts[i] = leftPos;
		}
	}

	/**
	 * Set the frame width and position from the left edge of the screen using layout params.
	 * This assumes the parent container is a FrameLayout as it will be setting the layout params
	 * as FrameLayout.LayoutParams.
	 * 
	 * @param container - what we want to position
	 * @param width - how wide?
	 * @param leftMargin - how far from the left of the parent
	 */
	public static void setFrameWidthAndPosition(FrameLayout container, int width, int leftMargin) {
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) container.getLayoutParams();
		if (params == null) {
			params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.width = width;
		params.leftMargin = leftMargin;
		container.setLayoutParams(params);
	}

}
