package com.expedia.bookings.widget;

import android.os.Parcelable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.mobiata.android.util.Ui;

public class BucketItemUndoController extends com.mobiata.android.widget.UndoBarController {

	View mUndoButtonView;

	public BucketItemUndoController(View undoBarView, UndoListener undoListener) {
		super(undoBarView, undoListener);
		mUndoButtonView = Ui.findView(mBarView, com.mobiata.android.R.id.undobar_button);
		mUndoButtonView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void showUndoBar(boolean immediate, CharSequence message, Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, mBarView.getResources().getInteger(com.mobiata.android.R.integer.undobar_hide_delay));

		if (mUndoButtonView == null) {
			mUndoButtonView = Ui.findView(mBarView, com.mobiata.android.R.id.undobar_button);
		}

		if (immediate) {
			mMessageView.setVisibility(View.VISIBLE);
			mUndoButtonView.setVisibility(View.VISIBLE);
		}
		else {
			if (mBarAnimation != null) {
				mBarAnimation.cancel();
				mBarView.clearAnimation();
			}

			mBarAnimation = new AlphaAnimation(0, 1);
			mBarAnimation.setDuration(mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime));
			mBarAnimation.setAnimationListener(null);

			mMessageView.setVisibility(View.VISIBLE);
			mUndoButtonView.setVisibility(View.VISIBLE);

			mMessageView.startAnimation(mBarAnimation);
			mUndoButtonView.startAnimation(mBarAnimation);
		}
	}

	@Override
	public void hideUndoBar(boolean immediate) {
		mHideHandler.removeCallbacks(mHideRunnable);

		if (mUndoButtonView == null) {
			mUndoButtonView = Ui.findView(mBarView, com.mobiata.android.R.id.undobar_button);
		}

		if (immediate) {
			mMessageView.setVisibility(View.INVISIBLE);
			mUndoButtonView.setVisibility(View.INVISIBLE);
			mUndoMessage = null;
			mUndoToken = null;

		}
		else {
			if (mBarAnimation != null) {
				mBarAnimation.cancel();
				mMessageView.clearAnimation();
				mUndoButtonView.clearAnimation();
			}
			mBarAnimation = new AlphaAnimation(1, 0);
			mBarAnimation.setDuration(mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime));
			mBarAnimation.setAnimationListener(new AnimationListenerAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					mBarView.clearAnimation();
					mUndoMessage = null;
					mUndoToken = null;
					if (mAnimationListenerAdapter != null) {
						mAnimationListenerAdapter.onAnimationEnd(animation);
					}
					mMessageView.setVisibility(View.INVISIBLE);
					mUndoButtonView.setVisibility(View.INVISIBLE);
				}
			});
			mMessageView.startAnimation(mBarAnimation);
			mUndoButtonView.startAnimation(mBarAnimation);
		}
	}
}
