package com.expedia.bookings.utils;

import android.view.ViewGroup;
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

		int left = 0;
		for(int i = 0; i < mNumColumns; i++){
			int width = baseColSize;
			if(i == 0){
				//the first column gets to be slightly larger
				width += remainder;
			}
			mColumnLefts[i] = left;
			mColumnWidths[i] = width;
			left+=width;
		}
	}

	/**
	 * Set the container so it is positioned to fill the provided column
	 * @param container - what we want to size/position
	 * @param column - which column should we size/position it to
	 */
	public void setContainerToColumn(ViewGroup container, int column) {
		setContainerToColumnSpan(container, column, column);
	}

	/**
	 * Set the container so it is positioned to fill the provided column span
	 * @param container - what we want to size/position
	 * @param startCol - the start column where the left edge will be placed
	 * @param endCol - the end column, the contianers right edge will match this columns right edge
	 */
	public void setContainerToColumnSpan(ViewGroup container, int startCol, int endCol) {
		int width = getColRight(endCol) - getColLeft(startCol);
		int left = getColLeft(startCol);
		setFrameWidthAndPosition(container, width, left);
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
	public static void setFrameWidthAndPosition(ViewGroup container, int width, int leftMargin) {
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) container.getLayoutParams();
		if (params == null) {
			params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.width = width;
		params.leftMargin = leftMargin;
		container.setLayoutParams(params);
	}

}
