package com.expedia.bookings.presenter;

import java.util.HashMap;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.expedia.bookings.animation.LooseLipsSinkShipsInterpolator;
import com.mobiata.android.Log;

/**
 * A FrameLayoutPresenter that maintains child presenters and animates
 * transitions between them.
 */

public abstract class TreePresenter extends FrameLayoutPresenter<IPresenter> {

	// Transition vars
	private Map<String, Map<String, Transition>> transitions = new HashMap<>();
	private String mCurrentState;
	private String mDestinationState;

	// Animation vars
	private boolean mAcceptAnimationUpdates = false;
	private float mLastAnimPercentage;
	private int mFrames;
	private float mAvgFrameDuration;

	// View lifecycle

	public TreePresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// IPresenter

	@Override
	public boolean back() {

		if (getBackStack().isEmpty()) {
			return false;
		}

		IPresenter childPresenter = getBackStack().pop();
		boolean backPressHandled = childPresenter.back();

		// BackPress was not handled by the child; handle it here.
		if (!backPressHandled) {

			if (getBackStack().isEmpty()) {
				return false;
			}

			hide(getBackStack().peek());
			return true;
		}

		// BackPress has been handled by the child.
		else {
			getBackStack().push(childPresenter);
			return true;
		}
	}

	@Override
	public void clearBackStack() {
		while (getBackStack().size() > 0) {
			getBackStack().pop();
		}
	}

	@Override
	public void show(IPresenter newState) {
		if (mCurrentState == null) {
			mCurrentState = newState.getClass().getName();
			getBackStack().push(newState);
			return;
		}
		if (mCurrentState.equals(newState.getClass().getName())) {
			return;
		}
		getBackStack().push(newState);
		getStateAnimator(newState, true).start();
	}

	@Override
	public void hide(IPresenter undoState) {
		getStateAnimator(undoState, false).start();
	}

	// Transition

	public static abstract class Transition {

		public static final int DEFAULT_ANIMATION_DURATION = 300;

		public final String state1;
		public final String state2;
		public final Interpolator interpolator;
		public final int duration;

		public Transition(String state1, String state2, Interpolator interpolator, int duration) {
			this.state1 = state1;
			this.state2 = state2;
			this.interpolator = interpolator;
			this.duration = duration;
		}

		public Transition(String state1, String state2) {
			this(state1, state2, null, DEFAULT_ANIMATION_DURATION);
		}

		public abstract void startTransition(boolean forward);

		public abstract void updateTransition(float f, boolean forward);

		public abstract void endTransition(boolean forward);

		public abstract void finalizeTransition(boolean forward);

	}

	public Transition getTransition(String a, String b) {
		if (transitions.containsKey(a) && transitions.get(a).containsKey(b)) {
			return transitions.get(a).get(b);
		}
		else if (transitions.containsKey(b) && transitions.get(b).containsKey(a)) {
			return transitions.get(b).get(a);
		}
		else {
			throw new RuntimeException("No Transition defined for " + a + " to " + b);
		}
	}

	public void addTransition(Transition transition) {
		if (!transitions.containsKey(transition.state1)) {
			transitions.put(transition.state1, new HashMap<String, Transition>());
		}
		transitions.get(transition.state1).put(transition.state2, transition);
	}

	// Animations – adapted from StateManager

	private void resetAnimStats() {
		mLastAnimPercentage = 0;
		mFrames = 0;
		mAvgFrameDuration = 0;
	}

	private void logAnimStats() {
		float avgFramePercentageChange = mLastAnimPercentage / mFrames;
		float animFrameRate = 1000f / mAvgFrameDuration;
		Log.d("StateManager.AnimationStats (" + mCurrentState + "," + mDestinationState + ") FrameRate:" + animFrameRate
			+ "f/s. TotalFrames:" + mFrames + ". AverageFrameDuration:" + mAvgFrameDuration
			+ "ms. AverageFramePercentageChange:" + avgFramePercentageChange + ". LastPercentageFromAnimator:"
			+ mLastAnimPercentage);
	}

	public ValueAnimator getStateAnimator(final IPresenter presenter, boolean forward) {
		final boolean goForward = forward;
		final Transition transition = getTransition(mCurrentState, presenter.getClass().getName());

		if (transition == null) {
			throw new RuntimeException("No Transition defined for "
				+ mCurrentState + " to " + presenter.getClass().getName());
		}

		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(transition.duration);

		final LooseLipsSinkShipsInterpolator wrappedInterpolator;
		if (transition.interpolator != null) {
			wrappedInterpolator = new LooseLipsSinkShipsInterpolator(transition.interpolator);
		}
		else {
			wrappedInterpolator = new LooseLipsSinkShipsInterpolator(new AccelerateDecelerateInterpolator());
		}

		animator.setInterpolator(wrappedInterpolator);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				if (mAcceptAnimationUpdates) {
					float totalDuration = arg0.getCurrentPlayTime() / wrappedInterpolator.getLastInput();
					long remainingDuration = (long) (totalDuration - arg0.getCurrentPlayTime());

					//Our animation percentage
					mLastAnimPercentage = (Float) arg0.getAnimatedValue();

					//Update our stats
					mFrames++;
					mAvgFrameDuration = ((float) arg0.getCurrentPlayTime()) / mFrames;

					if (remainingDuration < mAvgFrameDuration || remainingDuration < 0) {
						//If we are nearing the final frame, and we haven't reached one of our maximum values, lets do
						//so now, preventing some draw glitches between now and onStateFinalized.
						mLastAnimPercentage = Math.round(mLastAnimPercentage);
						transition.updateTransition(mLastAnimPercentage, goForward);
					}
					else {
						transition.updateTransition(mLastAnimPercentage, goForward);
					}
				}
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				transition.startTransition(goForward);
				resetAnimStats();
				mAcceptAnimationUpdates = true;
				mDestinationState = presenter.getClass().getName();
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAcceptAnimationUpdates = false;
				logAnimStats();
				transition.endTransition(goForward);
				transition.finalizeTransition(goForward);
				mCurrentState = goForward ? transition.state2 : transition.state1;
				mDestinationState = null;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				mAcceptAnimationUpdates = false;
				logAnimStats();
			}
		});
		return animator;
	}
}
