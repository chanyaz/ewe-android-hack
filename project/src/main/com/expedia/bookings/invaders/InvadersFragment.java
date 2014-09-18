package com.expedia.bookings.invaders;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

/**
 * TripBucketFragment: designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InvadersFragment extends Fragment implements IInvaderGameListener {

	private static final int BUTTON_REPEAT_DURATION = 25;

	private InvaderGameLayout mGameBoard;
	private TextView mScoreTv;
	private TextView mPlayPauseResumeBtn;
	private ViewGroup mLivesC;

	private ViewGroup mGameMessageC;
	private TextView mGameMessageBig;
	private TextView mGameMessageSmall;

	private boolean mLeftButtonDown;
	private boolean mRightButtonDown;

	private Runnable mLeftRunner;
	private Runnable mRightRunner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_invaders, null, false);
		mPlayPauseResumeBtn = Ui.findView(view, R.id.play_pause_resume_button);
		mLivesC = Ui.findView(view, R.id.lives_container);

		mGameMessageC = Ui.findView(view, R.id.game_message_container);
		mGameMessageBig = Ui.findView(view, R.id.game_message_big);
		mGameMessageSmall = Ui.findView(view, R.id.game_message_small);

		final View restartButton = Ui.findView(view, R.id.restart_button);
		final View fireButton = Ui.findView(view, R.id.fire_button);
		final View leftButton = Ui.findView(view, R.id.left_button);
		final View rightButton = Ui.findView(view, R.id.right_button);
		mScoreTv = Ui.findView(view, R.id.score);
		mGameBoard = Ui.findView(view, R.id.board);
		mGameBoard.setGameListener(this);

		restartButton.setVisibility(View.GONE);

		mPlayPauseResumeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mGameBoard.getGameStarted() && !mGameBoard.getGamePaused()) {
					mGameBoard.pauseGame(true);
					mPlayPauseResumeBtn.setText("RESUME");
				}
				else if (mGameBoard.getGamePaused()) {
					mGameBoard.resumeGame();
					mPlayPauseResumeBtn.setText("PAUSE");
				}else if (!mGameBoard.getGameStarted()) {
					mGameBoard.startGame();
					mPlayPauseResumeBtn.setText("PAUSE");
					restartButton.setVisibility(View.VISIBLE);
				}
			}
		});
		restartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mGameBoard.resetGame();
				mGameBoard.startGame();
			}
		});
		fireButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mGameBoard.fireShipLaser();
			}
		});


		leftButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getActionMasked();
				if (action == MotionEvent.ACTION_DOWN && mLeftRunner == null) {
					mLeftRunner = new Runnable() {
						@Override
						public void run() {
							if (mLeftRunner != this || !mGameBoard.getGameStarted() || mGameBoard.getGamePaused()) {
								return;
							}
							else {
								mGameBoard.moveShipLeft(BUTTON_REPEAT_DURATION);
								leftButton.postDelayed(this, BUTTON_REPEAT_DURATION);
							}
						}
					};
					leftButton.post(mLeftRunner);
					return true;
				}
				else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
					mLeftRunner = null;
					return true;
				}
				return false;
			}
		});

		rightButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getActionMasked();
				if (action == MotionEvent.ACTION_DOWN && mRightRunner == null) {
					mRightRunner = new Runnable() {
						@Override
						public void run() {
							if (mRightRunner != this || !mGameBoard.getGameStarted() || mGameBoard.getGamePaused()) {
								return;
							}
							else {
								mGameBoard.moveShipRight(BUTTON_REPEAT_DURATION);
								rightButton.postDelayed(this, BUTTON_REPEAT_DURATION);
							}
						}
					};
					rightButton.post(mRightRunner);
					return true;
				}
				else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
					mRightRunner = null;
					return true;
				}
				return false;
			}
		});

		setStartMessage();

		return view;
	}

	private void setStartMessage() {
		setGameMessage("Expedia Invaders",
			"In the beginning few were willing to join the resistance. The prophesy foretold a hero. This hero was to unite all Expedians against the onslaught. Expedians prayed day and night for their hero to arrive. You have been sent here by the travel gods to answer those prayers. Good luck Five Star Nipple Bear. The fate of Expedia rests in your hands.",
			true);
	}

	private void setVictoryMessage() {
		setGameMessage("Triumph!", "All the travel industry should be awed by this great victory! The invaders are vanquished! Expedia will experience an age of untold prosperity!", true);
	}

	private void setLossMessage() {
		setGameMessage("Game Over", "Many great star nipples were lost and shall never be forgotton.", true);
	}

	private void setGameMessage(String bigMessage, String littleMessage, boolean showing) {
		if (TextUtils.isEmpty(bigMessage)) {
			mGameMessageBig.setText("");
			mGameMessageBig.setVisibility(View.GONE);
		}
		else {
			mGameMessageBig.setText(bigMessage);
			mGameMessageBig.setVisibility(View.VISIBLE);
		}
		if (TextUtils.isEmpty(littleMessage)) {
			mGameMessageSmall.setText("");
			mGameMessageSmall.setVisibility(View.GONE);
		}
		else {
			mGameMessageSmall.setText(littleMessage);
			mGameMessageSmall.setVisibility(View.VISIBLE);
		}

		if (showing) {
			mGameMessageC.setVisibility(View.VISIBLE);
		}
		else {
			mGameMessageC.setVisibility(View.GONE);
		}
	}

	private void setLives(int lives) {
		if (mLivesC != null) {
			if (mLivesC.getChildCount() < lives) {
				while (mLivesC.getChildCount() < lives) {
					ImageView bear = new ImageView(getActivity());
					bear.setImageResource(R.drawable.bear_head);
					bear.setScaleType(ImageView.ScaleType.FIT_END);
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
					//bear.setBackgroundColor(mLivesC.getChildCount() % 2 == 0 ? Color.YELLOW : Color.CYAN);
					params.leftMargin = 16;
					bear.setAdjustViewBounds(true);
					mLivesC.addView(bear, params);
				}
			}
			else if (mLivesC.getChildCount() > lives) {
				while (mLivesC.getChildCount() > lives) {
					mLivesC.removeViewAt(mLivesC.getChildCount() - 1);
				}

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onScoreUpdate(int score) {
		mScoreTv.setText("" + score);
	}

	@Override
	public void onAvailableLivesUpdate(int livesRemaining) {
		setLives(livesRemaining);
	}

	@Override
	public void onGameWon() {
		setVictoryMessage();
	}

	@Override
	public void onGameLost() {
		setLossMessage();
	}

	@Override
	public void onGameStarted() {
		setGameMessage("", "", false);
	}

	@Override
	public void onGamePaused() {
		setGameMessage("Paused", "", true);

	}

	@Override
	public void onGameResumed() {
		setGameMessage("", "", false);
	}

	@Override
	public void onGameReset() {
		setStartMessage();
	}


}
