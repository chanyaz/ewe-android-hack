package com.expedia.bookings.invaders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.Log;

/**
 * Created by jdrotos on 6/17/14.
 */
public class InvaderGameLayout extends FrameLayout {

	private static final int SHIP_MOVE_DURATION = 50;
	private static final int SHIP_HIT_FLASH_ANIM_DURATION = 1000;
	private static final int SHIP_LASER_DIMEN = 24;
	private static final int INVADER_LASER_DIMEN = 32;
	private static final int INVDER_LASER_DURATION = 2000;
	private static final int START_LIVES = 3;
	private static final int DEFAULT_TICK_DURATION = 300;
	private static final int ROW_DESTROYED_TICK_SHORTEN = 40;

	private int mTickDuration;

	private long mLastTickTimeMs;
	private int mGameTick;
	private boolean mGamePaused;
	private boolean mGameStarted;
	private boolean mGameVictory;
	private boolean mGameLoss;

	private int mGamePoints;
	private int mLivesRemaining;

	private InvaderGameBoard mBoard;
	private InvaderView[][] mInvaders;
	private ShipView mShip;
	private GameRunner mGameRunner;

	//Ship laser stuff
	private int mLaserDestY;
	private Pair<Integer, Integer> mLaserWillHitInvaderAt;
	private LaserView mShipLaserView;
	private ValueAnimator mShipLaserAnim;

	//Invader laser stuff
	private LaserView mInvaderLaserView;
	private ValueAnimator mInvaderLaserAnim;

	//The listener
	private IInvaderGameListener mListener;

	public InvaderGameLayout(Context context) {
		super(context);
		init(context);
	}

	public InvaderGameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public InvaderGameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setGameListener(IInvaderGameListener listener) {
		mListener = listener;
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (getWidth() > 0 && getHeight() > 0) {
			if (changed || mBoard == null) {
				resetGame();
				//If we wanted to keep some state on size change, here is where we would reload it
			}
		}
	}

	private void init(Context context) {

	}

	public void startGame() {
		mGameRunner = new GameRunner();
		mGameStarted = true;
		post(mGameRunner);
		if (mListener != null) {
			mListener.onGameStarted();
		}
	}

	public void resetGame() {
		mGameRunner = null;

		mTickDuration = DEFAULT_TICK_DURATION;
		mLivesRemaining = START_LIVES;
		mGamePoints = 0;
		mGameTick = 0;

		mGameLoss = false;
		mGameVictory = false;
		mGamePaused = false;
		mGameStarted = false;

		mBoard = new InvaderGameBoard(getWidth(), getHeight());

		removeAllViews();
		initInvadersFromBoard(mBoard);
		initShipFromBoard(mBoard);
		updateInvaders(0);
		updateShip();

		if (mListener != null) {
			mListener.onGameReset();
		}
		if (mListener != null) {
			mListener.onScoreUpdate(mGamePoints);
		}
		if (mListener != null) {
			mListener.onAvailableLivesUpdate(mLivesRemaining);
		}
	}

	public boolean getGameStarted() {
		return mGameRunner != null && mGameStarted;
	}

	public void pauseGame(boolean fireListener) {
		if (!mGamePaused) {
			if (mGameRunner != null) {
				mGameRunner = null;
			}
			mGamePaused = true;

			//TODO: Pause laser animators...

			if (fireListener && mListener != null) {
				mListener.onGamePaused();
			}
		}
	}

	public boolean getGamePaused() {
		return mGamePaused;
	}

	public void resumeGame() {
		if (mGamePaused) {
			mGamePaused = false;
			startGame();
			if (mListener != null) {
				mListener.onGameResumed();
			}
		}
	}

	public void moveShipLeft(int duration) {
		if (getGameStarted() && gameLoopOkToRun()) {
			mShip.animate().setDuration(duration).translationX(mBoard.moveShip(-1)).start();
		}
	}

	public void moveShipRight(int duration) {
		if (getGameStarted() && gameLoopOkToRun()) {
			mShip.animate().setDuration(duration).translationX(mBoard.moveShip(1)).start();
		}
	}

	public void fireInvaderLaser() {
		if (getGameStarted() && mInvaderLaserAnim == null) {
			mInvaderLaserView = new InvaderLaserView(getContext());
			setViewSize(mInvaderLaserView, INVADER_LASER_DIMEN, INVADER_LASER_DIMEN);
			addView(mInvaderLaserView);
			Pair<Integer, Integer> laserStart = mBoard.getInvaderLaserOrigin(mGameTick, INVADER_LASER_DIMEN);
			if (laserStart != null) {
				setViewPosition(mInvaderLaserView, laserStart.second, laserStart.first);
				mInvaderLaserAnim = generateInvaderLaserAnimator(mInvaderLaserView, laserStart.first, getBottom(),
					INVDER_LASER_DURATION);
				mInvaderLaserAnim.start();
			}
		}
	}

	public void fireShipLaser() {
		if (getGameStarted()) {
			if (mShipLaserAnim == null) {
				int laserWidth = SHIP_LASER_DIMEN;
				int laserHeight = SHIP_LASER_DIMEN;

				//Create the laser view
				mShipLaserView = new LaserView(getContext());
				addView(mShipLaserView);
				setViewSize(mShipLaserView, laserWidth, laserHeight);
				setViewPosition(mShipLaserView, mBoard.getShipLaserTransX(laserWidth),
					mBoard.getShipLaserStartTransY(laserHeight));

				//Determine if we are going to hit anything
				int currentTurnHitY = mBoard.getShipLaserHitY(mGameTick + 1, laserWidth);
				int nextTurnHitY = mBoard.getShipLaserHitY(mGameTick + 1, laserWidth);
				Pair<Integer, Integer> currentTurnCoords = mBoard.getShipLaserInvaderHitIndex(mGameTick, laserWidth);
				Pair<Integer, Integer> nextTurnCoords = mBoard.getShipLaserInvaderHitIndex(mGameTick + 1, laserWidth);

				int laserStartY = mBoard.getShipLaserStartTransY(laserHeight);
				int laserStartX = mBoard.getShipLaserTransX(laserWidth);

				if (currentTurnHitY >= 0 || nextTurnHitY >= 0) {
					//A hit is possible
					//int laserDistanceToTop = mBoard.getBoardHeight() - laserStartY;
					long timeToNextTurn = Math.max(mTickDuration - (System.currentTimeMillis() - mLastTickTimeMs), 0);
					int distanceByNextTurn = (int) ((timeToNextTurn / (float) mTickDuration) * laserStartY);

					int destY = -1;
					Pair<Integer, Integer> destCoords = null;
					if (currentTurnHitY >= 0 && laserStartY - distanceByNextTurn < currentTurnHitY) {
						destY = currentTurnHitY;
						destCoords = currentTurnCoords;
					}
					else if (nextTurnHitY >= 0) {
						destY = nextTurnHitY;
						destCoords = nextTurnCoords;
					}

					if (destY >= 0 && destCoords != null) {
						mLaserDestY = destY;
						mLaserWillHitInvaderAt = destCoords;
						int distanceToDest = laserStartY - mLaserDestY;
						float distanceRatio = distanceToDest / (float) laserStartY;
						int duration = (int) (mTickDuration * distanceRatio);
						Log.d("fireShipLaser mLaserDestY:" + mLaserDestY + " distanceToDest:" + distanceToDest
							+ " laserStartY:" + laserStartY + " distanceRatio:" + distanceRatio + " duration:"
							+ duration + " tickDuration:" + mTickDuration);
						if (duration > 0) {
							mShipLaserAnim = generateShipLaserAnimator(mShipLaserView, laserStartY, mLaserDestY,
								duration);
						}
					}
					else {
						mShipLaserAnim = generateShipLaserAnimator(mShipLaserView, laserStartY, destY, mTickDuration);
					}
				}
				else {
					//No hit is going to happen (so we are just going to shoot the top of the screen)
					mShipLaserAnim = generateShipLaserAnimator(mShipLaserView, laserStartY, -1, mTickDuration);
				}
				if (mShipLaserAnim != null) {
					mShipLaserAnim.start();
				}
			}
		}
	}


	private ValueAnimator generateShipLaserAnimator(LaserView view, int startY, int endY, int duration) {
		ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationY", startY, endY).setDuration(duration);
		anim.setInterpolator(new LinearInterpolator());
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				cleanUpAfterShipLaserAnimation();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				cleanUpAfterShipLaserAnimation();
			}
		});
		return anim;
	}

	private ValueAnimator generateInvaderLaserAnimator(final LaserView view, int startY, int endY, int duration) {
		ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationY", startY, endY).setDuration(duration);
		anim.setInterpolator(new LinearInterpolator());
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				cleanUpAfterInvaderLaserAnimation();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				cleanUpAfterInvaderLaserAnimation();
			}
		});
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				if (gameLoopOkToRun() && !mGamePaused) {

					Rect laserRect = ScreenPositionUtils.getGlobalScreenPosition(mInvaderLaserView);
					Rect shipRect = ScreenPositionUtils.getGlobalScreenPosition(mShip);

					Log.d("onAnimationUpdate laserRect:" + laserRect.toShortString() + " shipRect:" + shipRect
						.toShortString());

					if (laserRect.intersects(shipRect.left, shipRect.top, shipRect.right, shipRect.bottom)) {
						//OUR SHIP HAS BEEN HIT

						//Cancel the laser animation
						mInvaderLaserAnim.cancel();

						//Pause the game
						pauseGame(false);

						//Remove lives
						mLivesRemaining--;

						if (mLivesRemaining > 0) {
							ObjectAnimator flashAnim = ObjectAnimator.ofFloat(mShip, "alpha", 0f, 1f, 0f, 1f, 0f, 1f);
							flashAnim.setDuration(SHIP_HIT_FLASH_ANIM_DURATION);
							flashAnim.addListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationCancel(Animator animation) {
									resumeGame();
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									resumeGame();
								}
							});
							flashAnim.start();

						}

						//Notify listener of lives lost, and potential game loss...
						if (mListener != null) {
							mListener.onAvailableLivesUpdate(mLivesRemaining);
							if (mLivesRemaining <= 0) {
								mListener.onGameLost();
							}
						}
					}
				}
			}
		});
		return anim;
	}

	private void cleanUpAfterInvaderLaserAnimation() {
		mInvaderLaserAnim = null;
		removeView(mInvaderLaserView);
		mInvaderLaserView = null;
	}


	private void cleanUpAfterShipLaserAnimation() {
		if (mLaserWillHitInvaderAt != null) {
			mBoard.setInvaderExploded(mLaserWillHitInvaderAt.first, mLaserWillHitInvaderAt.second);
			InvaderView invader = mInvaders[mLaserWillHitInvaderAt.first][mLaserWillHitInvaderAt.second];
			setViewVisibility(invader, false);

			//This should probably be moved...
			mGamePoints += invader.getKillPoints();
			if (mListener != null) {
				mListener.onScoreUpdate(mGamePoints);
			}
		}
		mLaserWillHitInvaderAt = null;
		mLaserDestY = -1;
		removeView(mShipLaserView);
		mShipLaserAnim = null;
	}


	private void gameLoop() {
		if (mGameStarted && !mGamePaused && !mGameLoss && !mGameVictory) {
			if (mGameTick % 10 == 0) {
				Log.d("gameLoop mGameTick:" + mGameTick);
			}
			mGameTick++;
			updateInvaders(mGameTick);

			if (mGameTick > 10) {
				fireInvaderLaser();
			}

			if (mBoard.areAllInvadersExploded(mGameTick)) {
				mGameVictory = true;
				if (mListener != null) {
					mListener.onGameWon();
				}
			}

			if (mBoard.isShipHittingInvader(mGameTick)) {
				mGameLoss = true;
				if (mListener != null) {
					mListener.onGameLost();
				}
			}

			if (mBoard.areInvadersHittingTheGround(mGameTick)) {
				mGameLoss = true;
				if (mListener != null) {
					mListener.onGameLost();
				}
			}

			int destroyedRows = mBoard.getNumberOfRowsDestroyed();
			mTickDuration = DEFAULT_TICK_DURATION - destroyedRows * ROW_DESTROYED_TICK_SHORTEN;

			mLastTickTimeMs = System.currentTimeMillis();
		}
	}

	private boolean gameLoopOkToRun() {
		return mGameStarted && !mGameLoss && !mGameVictory && getContext() != null;
	}

	private void initInvadersFromBoard(InvaderGameBoard board) {
		int cols = board.getInvaderCols();
		int rows = board.getInvaderRows();
		mInvaders = new InvaderView[cols][rows];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				InvaderView view;
				if (row == 0) {
					view = new InvaderViewJoel(getContext());
				}
				else if (row == 2) {
					view = new InvaderViewDan(getContext());
				}
				else if (row == 4) {
					view = new InvaderViewPar(getContext());
				}
				else {
					view = new InvaderViewSeb(getContext());
				}
				mInvaders[col][row] = view;
				addView(view);
				setViewSize(view, mBoard.getInvaderWidth(), mBoard.getInvaderHeight());
			}
		}
	}

	private void initShipFromBoard(InvaderGameBoard board) {
		mShip = new ShipView(getContext());
		setViewSize(mShip, board.getShipWidth(), board.getShipHeight());
		addView(mShip);
	}

	private void updateInvaders(int gameTick) {
		for (int col = 0; col < mInvaders.length; col++) {
			int transX = mBoard.getInvaderTransX(gameTick, col);
			for (int row = 0; row < mInvaders[col].length; row++) {
				int transY = mBoard.getInvaderTransY(gameTick, row);
				InvaderView invader = mInvaders[col][row];
				setViewPosition(invader, transX, transY);
				setViewVisibility(invader, !mBoard.getInvaderExploded(col, row));

				//Flip those dudes sometimes
				invader.flip(row % 2 == gameTick % 2);

			}
		}
	}

	private void updateShip() {
		setViewPosition(mShip, mBoard.getShipTransX(), mBoard.getShipTransY());
	}


	/**
	 * HELPERS
	 */

	private View setViewSize(View view, int width, int height) {
		LayoutParams params = generateDefaultLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
		return view;
	}

	private View setViewPosition(View view, int transX, int transY) {
		view.setTranslationX(transX);
		view.setTranslationY(transY);
		return view;
	}

	private View setViewVisibility(View view, boolean visible) {
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
		return view;
	}


	private class GameRunner implements Runnable {

		@Override
		public void run() {
			if (mGameRunner == this && gameLoopOkToRun()) {
				gameLoop();
				postDelayed(this, mTickDuration);
			}
		}
	}
}
