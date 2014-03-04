package com.expedia.bookings.utils;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class GridManager {

	private GridAxis mRows;
	private GridAxis mCols;

	public GridManager() {
		this(1, 1);
	}

	public GridManager(int numRows, int numColumns) {
		setGridSize(numRows, numColumns);
	}

	public void setTotalWidth(int width) {
		mCols.setTotalSize(width);
	}

	public void setTotalHeight(int height) {
		mRows.setTotalSize(height);
	}

	public void setDimensions(int width, int height) {
		setTotalWidth(width);
		setTotalHeight(height);
	}

	public void setNumRows(int numRows) {
		if (mRows == null) {
			mRows = new GridAxis(numRows);
		}
		else {
			mRows.setNumItems(numRows);
		}
	}

	public void setNumCols(int numCols) {
		if (mCols == null) {
			mCols = new GridAxis(numCols);
		}
		else {
			mCols.setNumItems(numCols);
		}
	}

	public void setGridSize(int numRows, int numCols) {
		setNumRows(numRows);
		setNumCols(numCols);
	}

	public void setColumnSize(int colInd, int size) {
		mCols.setItemFixedSize(colInd, size);
	}

	public void setRowSize(int rowInd, int size) {
		mRows.setItemFixedSize(rowInd, size);
	}

	public void setColumnPercentage(int colInd, float perc) {
		mCols.setItemFixedPercentage(colInd, perc);
	}

	public void setRowPercentage(int rowInd, float perc) {
		mRows.setItemFixedPercentage(rowInd, perc);
	}

	public int getTotalWidth() {
		return mCols.getTotalSize();
	}

	public int getTotalHeight() {
		return mRows.getTotalSize();
	}

	public int getNumRows() {
		return mRows.getItemCount();
	}

	public int getNumCols() {
		return mCols.getItemCount();
	}

	public int getRowHeight(int rowNum) {
		return mRows.getItemSize(rowNum);
	}

	public int getColWidth(int colNum) {
		return mCols.getItemSize(colNum);
	}

	public int getColLeft(int colNum) {
		return mCols.getItemEdge(colNum);
	}

	public int getColRight(int colNum) {
		return mCols.getItemFarEdge(colNum);
	}

	public int getRowTop(int rowNum) {
		return mRows.getItemEdge(rowNum);
	}

	public int getRowBottom(int rowNum) {
		return mRows.getItemFarEdge(rowNum);
	}

	public boolean isLandscape() {
		return mCols.getTotalSize() > mRows.getTotalSize();
	}

	/**
	 * Set the container so it is positioned to fill the provided column
	 *
	 * @param view   - what we want to size/position
	 * @param column - which column should we size/position it to
	 */
	public void setContainerToColumn(View view, int column) {
		setContainerToColumnSpan(view, column, column);
	}

	/**
	 * Set the container so it is positioned to fill the provided column span
	 *
	 * @param view     - what we want to size/position
	 * @param startCol - the start column where the left edge will be placed
	 * @param endCol   - the end column, the contianers right edge will match this columns right edge
	 */
	public void setContainerToColumnSpan(View view, int startCol, int endCol) {
		int width = mCols.getItemFarEdge(endCol) - mCols.getItemEdge(startCol);
		int left = mCols.getItemEdge(startCol);
		setFrameWidthAndPosition(view, width, left);
	}

	/**
	 * Set the container so it is positioned to fill the provider row
	 *
	 * @param view - what we want to size/position
	 * @param row  - which row we should size/position it to
	 */
	public void setContainerToRow(View view, int row) {
		setContainerToRowSpan(view, row, row);
	}

	/**
	 * Set the container so it is positioned to fill the provided row span
	 *
	 * @param view     - what we want to size/position
	 * @param startRow - the start row where the top edge will be placed
	 * @param endRow   - the end row, the contianers bottom edge will match this columns bottom edge
	 */
	public void setContainerToRowSpan(View view, int startRow, int endRow) {
		int height = mRows.getItemFarEdge(endRow) - mRows.getItemEdge(startRow);
		int top = mRows.getItemEdge(startRow);
		setFrameHeightAndPosition(view, height, top);
	}

	/**
	 * Set the frame width and position from the left edge of the screen using layout params.
	 * This assumes the parent container is a FrameLayout as it will be setting the layout params
	 * as FrameLayout.LayoutParams.
	 *
	 * @param view       - what we want to position
	 * @param width      - how wide?
	 * @param leftMargin - how far from the left of the parent
	 */
	public static void setFrameWidthAndPosition(View view, int width, int leftMargin) {
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
		if (params == null) {
			params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.width = width;
		params.leftMargin = leftMargin;
		view.setLayoutParams(params);
	}

	/**
	 * Set the frame height and position from the top edge of the screen using layout params.
	 * This assumes the parent container is a FrameLayout as it will be setting the layout params
	 * as FrameLayout.LayoutParams.
	 *
	 * @param view      - what we want to position
	 * @param height    - how tall?
	 * @param topMargin - how far from the top of the parent
	 */
	public static void setFrameHeightAndPosition(View view, int height, int topMargin) {
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
		if (params == null) {
			params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.height = height;
		params.topMargin = topMargin;
		view.setLayoutParams(params);
	}

	/**
	 * This class does the calculations for the boundaries/sizes of one axis in the grid (e.g. rows/columns/z-index/time/???)
	 */
	@SuppressLint("UseSparseArrays")
	private static class GridAxis {
		private int mNumItems = 0;
		private int mTotalSize = 0;
		private int[] mItemSizes;
		private int[] mItemEdges;

		private HashMap<Integer, Integer> mFixedItemSizes = new HashMap<Integer, Integer>();
		private HashMap<Integer, Float> mFixedItemPercentages = new HashMap<Integer, Float>();

		public GridAxis(int numItems) {
			setNumItems(numItems);
		}

		public void setTotalSize(int totalSize) {
			if (mTotalSize != totalSize) {
				mTotalSize = totalSize;
				calculate();
			}
		}

		public void setNumItems(int numItems) {
			mNumItems = numItems;
			mItemSizes = new int[mNumItems];
			mItemEdges = new int[mNumItems];

			if (mTotalSize > 0) {
				calculate();
			}
		}

		public void setItemFixedSize(int index, int size) {
			mFixedItemSizes.put(index, size);
			if (mTotalSize > 0) {
				calculate();
			}
		}

		public void setItemFixedPercentage(int index, float percentage) {
			mFixedItemPercentages.put(index, percentage);
			if (mTotalSize > 0) {
				calculate();
			}
		}

		public int getTotalSize() {
			return mTotalSize;
		}

		public int getItemCount() {
			return mNumItems;
		}

		public int getItemSize(int colIndex) {
			if (colIndex >= mItemSizes.length) {
				return 0;
			}
			return mItemSizes[colIndex];
		}

		public int getItemEdge(int colIndex) {
			if (colIndex >= mItemEdges.length) {
				return 0;
			}
			return mItemEdges[colIndex];
		}

		public int getItemFarEdge(int colIndex) {
			if (colIndex >= mItemEdges.length || colIndex >= mItemSizes.length) {
				return 0;
			}
			return mItemEdges[colIndex] + mItemSizes[colIndex];
		}

		private void calculate() {

			//Preprocess
			int remainingSize = mTotalSize;
			int preDefinedItems = 0;
			for (int i = 0; i < mNumItems; i++) {
				if (mFixedItemSizes.containsKey(i)) {
					remainingSize -= mFixedItemSizes.get(i);
					preDefinedItems++;
				}
				else if (mFixedItemPercentages.containsKey(i)) {
					remainingSize -= (mFixedItemPercentages.get(i) * mTotalSize);
					preDefinedItems++;
				}
			}

			//Set sizes
			int nonSpecifiedItemCount = mNumItems - preDefinedItems;
			int nonSpecifiedItemBaseSize = nonSpecifiedItemCount > 0 ? (remainingSize / nonSpecifiedItemCount) : 0;
			int edge = 0;
			int totalUsedSize = 0;
			for (int i = 0; i < mNumItems; i++) {
				int size = nonSpecifiedItemBaseSize;
				if (mFixedItemSizes.containsKey(i)) {
					size = mFixedItemSizes.get(i);
				}
				else if (mFixedItemPercentages.containsKey(i)) {
					size = (int) (mFixedItemPercentages.get(i) * mTotalSize);
				}
				mItemEdges[i] = edge;
				mItemSizes[i] = size;
				edge += size;
				totalUsedSize += size;
			}

			//Account for remainder
			int remainderSize = mTotalSize - totalUsedSize;
			if (remainderSize != 0) {
				//We just add the remainder to the last column, as this doesn't mess with our edge positions
				mItemSizes[mNumItems - 1] += remainderSize;
			}
		}
	}

}
