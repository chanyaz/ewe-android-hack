package com.expedia.bookings.invaders;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Rect;
import android.util.Pair;

import com.expedia.bookings.utils.GridManager;
import com.mobiata.android.Log;

/**
 * Created by jdrotos on 6/17/14.
 */
public class InvaderGameBoard {

	//Game Settings
	private static final int INVADER_ROWS = 5;
	private static final int INVADER_COLS = 11;

	//Number of horizontal steps from farthest left to farthest right
	private static final int HORIZONTAL_STEPS = 12;

	//Number of vertical steps for the top row to reach the bottom of the board
	private static final int VERTIAL_STEPS = 8;

	//When we move the ship, how far does it move in pixels
	private static final int SHIP_MOVE_PIXELS_DEFAULT = 8;

	//We don't always use the full board, we maintain a ratio
	private static final float WIDTH_HEIGHT_RATIO = 4f / 3f;

	//Our invaders stay together in a  block, what portion of the whole playing board is that block
	private static final float INVADERS_OCCUPY_WIDTH_PERCENTAGE = 0.8f;
	private static final float INVADERS_OCCUPY_HEIGHT_PERCENTAGE = 0.5f;

	//What percentage of the invader block should actually be spacers
	private static final float INVADER_BLOCK_HORIZONTAL_SPACE_PERCENTAGE = 0.4f;
	private static final float INVADER_BLOCK_VERTICAL_SPACE_PERCENTAGE = 0.2f;

	//Figure out the ship size based on board size
	private static final float SHIP_BOARD_PERCENTAGE_WIDTH = 0.1f;
	private static final float SHIP_BOARD_PERCENTAGE_HEIGHT = 0.1f;


	//These store the distance that everything is translated before considering the board state etc.
	private int mWholeBoardTransX;
	private int mWholeBoardTransY;

	//The dimens of the playable board
	private int mBoardWidth;
	private int mBoardHeight;

	//Invader dimens
	private int mInvaderWidth;
	private int mInvaderHeight;

	//Invader spacer dimens
	private int mInvaderSpacerWidth;
	private int mInvaderSpacerHeight;

	//Invader state
	private boolean[][] mExplodedInvaders;

	//Ship dimens
	private int mShipWidth;
	private int mShipHeight;

	//Invader Step sizes
	private int mInvaderStepX;
	private int mInvaderStepY;

	//Ship steps
	private int mShipStepX;
	private int mCurrentShipStepCount;
	private int mMaxShipStepCount;

	//Invader grid (NOTE THIS JUST DEFINES THE BLOCK THAT CONTAINS THE INVADERS, NOT THE WHOLE BOARD)
	private GridManager mInvaderGrid;


	public int getShipWidth() {
		return mShipWidth;
	}

	public int getShipHeight() {
		return mShipHeight;
	}

	public int getInvaderHeight() {
		return mInvaderHeight;
	}

	public int getInvaderWidth() {
		return mInvaderWidth;
	}

	public int getBoardWidth() {
		return mBoardWidth;
	}

	public int getBoardHeight() {
		return mBoardHeight;
	}

	public int getInvaderSpacerWidth() {
		return mInvaderSpacerWidth;
	}

	public int getInvaderSpacerHeight() {
		return mInvaderSpacerHeight;
	}

	public int getCurrentShipStepCount() {
		return mCurrentShipStepCount;
	}

	public void setCurrentShipStepCount(int currentShipStepCount) {
		mCurrentShipStepCount = currentShipStepCount;
	}

	public int getInvaderRows() {
		return INVADER_ROWS;
	}

	public int getInvaderCols() {
		return INVADER_COLS;
	}

	public InvaderGameBoard(int totalWidth, int totalHeight) {
		initBoard(totalWidth, totalHeight);
	}


	/**
	 * This will init the size values of the board
	 *
	 * @param totalWidth
	 * @param totalHeight
	 */
	private void initBoard(int totalWidth, int totalHeight) {
		mBoardWidth = totalWidth;
		mBoardHeight = totalHeight;

		//Set up the array detecting which invader has been shot
		mExplodedInvaders = new boolean[INVADER_COLS][INVADER_ROWS];
		for (int i = 0; i < mExplodedInvaders.length; i++) {
			for (int j = 0; j < mExplodedInvaders[i].length; j++) {
				//Nobody is exploded at the start
				mExplodedInvaders[i][j] = false;
			}
		}

		//Figure out the playing board size with the correct ratio
		float idealHeight = totalWidth / WIDTH_HEIGHT_RATIO;
		float idealWidth = totalHeight * WIDTH_HEIGHT_RATIO;
		if (idealHeight <= totalHeight) {
			mBoardHeight = (int) idealHeight;
			mBoardWidth = totalWidth;
		}
		else if (idealWidth <= totalWidth) {
			mBoardHeight = totalHeight;
			mBoardWidth = (int) idealWidth;
		}

		//Figure out how to position the entire board
		mWholeBoardTransX = (int) ((totalWidth - mBoardWidth) / 2f);
		mWholeBoardTransY = (int) ((totalHeight - mBoardHeight) / 2f);

		//Figure out our invader sizes and the spacer sizes
		int invaderBlockWidth = (int) (INVADERS_OCCUPY_WIDTH_PERCENTAGE * mBoardWidth);
		int totalSpacerWidth = (int) (invaderBlockWidth * INVADER_BLOCK_HORIZONTAL_SPACE_PERCENTAGE);
		mInvaderSpacerWidth = (int) (totalSpacerWidth / (float) (INVADER_COLS - 1));
		mInvaderWidth = (int) ((invaderBlockWidth - totalSpacerWidth) / (float) INVADER_COLS);

		int invaderBlockHeight = (int) (INVADERS_OCCUPY_HEIGHT_PERCENTAGE * mBoardHeight);
		int totalSpacerHeight = (int) (invaderBlockHeight * INVADER_BLOCK_VERTICAL_SPACE_PERCENTAGE);
		mInvaderSpacerHeight = (int) (totalSpacerHeight / (float) (INVADER_ROWS - 1));
		mInvaderHeight = (int) ((invaderBlockHeight - totalSpacerHeight) / (float) INVADER_ROWS);

		//Figure out step sizes
		mInvaderStepX = (int) ((mBoardWidth - invaderBlockWidth) / (float) HORIZONTAL_STEPS);
		mInvaderStepY = (int) ((mBoardHeight - invaderBlockHeight) / (float) VERTIAL_STEPS);

		//Generate the grid for our invaders
		int totalGridRows = INVADER_ROWS * 2 - 1;//All rows and spacers
		int totalGridCols = INVADER_COLS * 2 - 1;//All Columns and spacers
		mInvaderGrid = new GridManager(totalGridRows, totalGridCols);
		mInvaderGrid.setDimensions(invaderBlockWidth, invaderBlockHeight);
		for (int row = 0; row < totalGridRows; row++) {
			if (row % 2 == 0) {
				//Our even numbers are our invaders
				mInvaderGrid.setRowSize(row, mInvaderHeight);
			}
		}
		for (int col = 0; col < totalGridCols; col++) {
			if (col % 2 == 0) {
				//Our even numbers are our invaders
				mInvaderGrid.setColumnSize(col, mInvaderWidth);
			}
		}

		//Figure out our ship size
		mShipWidth = (int) (SHIP_BOARD_PERCENTAGE_WIDTH * mBoardWidth);
		mShipHeight = (int) (SHIP_BOARD_PERCENTAGE_HEIGHT * mBoardWidth);

		//Ship step size
		mShipStepX = SHIP_MOVE_PIXELS_DEFAULT;
		mMaxShipStepCount = (int) ((mBoardWidth - mShipWidth) / (float) mShipStepX);
		mCurrentShipStepCount = 0;

		Log.d("initBoard boardH:" + mBoardHeight + " boardW:" + mBoardWidth + " boardTransX:" + mWholeBoardTransX
			+ " boardTransY:" + mWholeBoardTransY + " invaderWidth:" + mInvaderWidth + " invaderHeight:"
			+ mInvaderHeight + " shipWidth:" + mShipWidth + " shipHeight:" + mShipHeight + " mShipStepX:" + mShipStepX
			+ " mMaxShipStepCount:" + mMaxShipStepCount);
	}

	private int getWholeBlockTransX(int gameTick) {
		int dubSteps = 2 * HORIZONTAL_STEPS;
		int mod = gameTick % dubSteps;
		if (mod > HORIZONTAL_STEPS) {
			return mWholeBoardTransX + (HORIZONTAL_STEPS - (mod - HORIZONTAL_STEPS)) * mInvaderStepX;
		}
		else {
			return mWholeBoardTransX + mod * mInvaderStepX;
		}
	}

	private int getWholeBlockTransY(int gameTick) {
		int dubStep = 2 * HORIZONTAL_STEPS;
		int numVerticalSteps = (int) Math.floor(gameTick / (float) dubStep);
		return mWholeBoardTransY + numVerticalSteps * mInvaderStepY;
	}

	public int getInvaderTransX(int gameTick, int invaderColumn) {
		if (invaderColumn < 0 || invaderColumn >= INVADER_COLS) {
			throw new RuntimeException("Invalid invader column:" + invaderColumn);
		}
		return getWholeBlockTransX(gameTick) + mInvaderGrid.getColLeft(invaderColumn * 2);
	}

	public int getInvaderTransY(int gameTick, int invaderRow) {
		if (invaderRow < 0 || invaderRow >= INVADER_ROWS) {
			throw new RuntimeException("Invalid invader row:" + invaderRow);
		}
		return getWholeBlockTransY(gameTick) + mInvaderGrid.getRowTop(invaderRow * 2);
	}

	public boolean getInvaderExploded(int col, int row) {
		return mExplodedInvaders[col][row];
	}

	public void setInvaderExploded(int col, int row) {
		mExplodedInvaders[col][row] = true;
	}

	public int moveShip(int steps) {
		mCurrentShipStepCount += steps;
		if (mCurrentShipStepCount < 0) {
			mCurrentShipStepCount = 0;
		}
		if (mCurrentShipStepCount > mMaxShipStepCount) {
			mCurrentShipStepCount = mMaxShipStepCount;
		}
		Log.d("moveShip steps:" + steps + " shipTransX:" + getShipTransX());
		return getShipTransX();
	}

	public boolean areAllInvadersExploded(int gametick) {
		for (int i = 0; i < mExplodedInvaders.length; i++) {
			for (int j = 0; j < mExplodedInvaders[i].length; j++) {
				//Nobody is exploded at the start
				if (!mExplodedInvaders[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isShipHittingInvader(int gametick) {
		int shipY = getShipTransY();
		int shipX = getShipTransX();
		for (int col = 0; col < mExplodedInvaders.length; col++) {
			for (int row = mExplodedInvaders[col].length - 1; row >= 0; row--) {
				int invaderY = getInvaderTransY(gametick, row);
				if (invaderY + mInvaderHeight >= shipY && !mExplodedInvaders[col][row]) {
					int invaderX = getInvaderTransX(gametick, col);
					Rect invader = new Rect(invaderX, invaderY, invaderX + mInvaderWidth, invaderY + mInvaderHeight);
					if (invader.intersects(shipX, shipY, shipX + mShipWidth, shipY + mShipHeight)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean areInvadersHittingTheGround(int gametick) {
		for (int col = 0; col < mExplodedInvaders.length; col++) {
			for (int row = mExplodedInvaders[col].length - 1; row >= 0; row--) {
				if (!mExplodedInvaders[col][row]) {
					if (getInvaderTransY(gametick, row) + mInvaderHeight >= mBoardHeight + mWholeBoardTransY) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getShipTransX() {
		return mWholeBoardTransX + mCurrentShipStepCount * mShipStepX;
	}

	public int getShipTransY() {
		return mWholeBoardTransY + mBoardHeight - mShipHeight;
	}

	public int getShipLaserTransX(int laserWidth) {
		return (int) (getShipTransX() + mShipWidth / 2f - laserWidth / 2f);
	}

	public int getShipLaserStartTransY(int laserHeight) {
		return getShipTransY() - laserHeight;
	}

	public int getShipLaserHitY(int gametick, int laserWidth) {
		int laserX = getShipLaserTransX(laserWidth);
		int laserEnd = laserX + laserWidth;
		for (int col = 0; col < mExplodedInvaders.length; col++) {
			int colX = getInvaderTransX(gametick, col);
			int colXEnd = colX + mInvaderWidth;
			if ((laserX >= colX && laserX <= colXEnd) || (laserEnd >= colX && laserEnd <= colXEnd)) {
				for (int row = mExplodedInvaders[col].length - 1; row >= 0; row--) {
					if (!mExplodedInvaders[col][row]) {
						return getInvaderTransY(gametick, row) + mInvaderHeight;
					}
				}
			}
		}
		return -1;
	}

	public Pair<Integer, Integer> getShipLaserInvaderHitIndex(int gametick, int laserWidth) {
		int laserX = getShipLaserTransX(laserWidth);
		int laserEnd = laserX + laserWidth;
		for (int col = 0; col < mExplodedInvaders.length; col++) {
			int colX = getInvaderTransX(gametick, col);
			int colXEnd = colX + mInvaderWidth;
			if ((laserX >= colX && laserX <= colXEnd) || (laserEnd >= colX && laserEnd <= colXEnd)) {
				for (int row = mExplodedInvaders[col].length - 1; row >= 0; row--) {
					if (!mExplodedInvaders[col][row]) {
						return new Pair<Integer, Integer>(col, row);
					}
				}
			}
		}
		return null;
	}

	public Pair<Integer, Integer> getInvaderLaserOrigin(int gameTick, int laserWidth) {
		ArrayList<Integer> randomColumns = new ArrayList<Integer>();
		for (int col = 0; col < mExplodedInvaders.length; col++) {
			randomColumns.add(col);
		}
		Collections.shuffle(randomColumns);
		for (Integer col : randomColumns) {
			//Start at the bottom
			for (int row = mExplodedInvaders[col].length - 1; row >= 0; row--) {
				if (!mExplodedInvaders[col][row]) {
					int y = getInvaderTransY(gameTick, row) + mInvaderHeight;
					int x = (int) (getInvaderTransX(gameTick, col) + mInvaderWidth / 2f - laserWidth / 2f);
					return new Pair<Integer, Integer>(y, x);
				}
			}
		}
		return null;
	}


	public int getNumberOfRowsDestroyed(){
		int rowsDestroyed = 0;
		for(int row = mExplodedInvaders[0].length -1; row >= 0; row--){
			boolean hasValidInvader = false;
			for(int col = 0; col < mExplodedInvaders.length; col++){
				hasValidInvader |= !mExplodedInvaders[col][row];
			}
			if(!hasValidInvader){
				rowsDestroyed++;
			}
		}
		return rowsDestroyed;
	}
}
