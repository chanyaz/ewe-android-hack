package com.expedia.bookings.utils;

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

}
