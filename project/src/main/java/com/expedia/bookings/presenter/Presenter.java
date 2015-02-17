package com.expedia.bookings.presenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.expedia.bookings.animation.LooseLipsSinkShipsInterpolator;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;

import butterknife.ButterKnife;

/**
 * A FrameLayoutPresenter that maintains child presenters and animates
 * transitions between them.
 */

public class Presenter extends FrameLayout implements IPresenter<Object> {

	private Stack<Object> backstack;

	// Transition vars
	private Map<String, Map<String, Transition>> transitions = new HashMap<>();
	private String currentState;
	private String destinationState;

	// Animation vars
	private boolean acceptAnimationUpdates = false;
	private float lastAnimPercentage;
	private int frames;
	private float avgFrameDuration;

	// View lifecycle

	public Presenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBackStack();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	// IPresenter

	@Override
	public boolean back() {

		if (getBackStack().isEmpty()) {
			return false;
		}

		Object child = getBackStack().pop();
		boolean backPressHandled = child instanceof IPresenter && ((IPresenter) child).back();

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
			getBackStack().push(child);
			return true;
		}
	}

	// Backstack

	@Override
	public void initBackStack() {
		backstack = new Stack<>();
	}

	@Override
	public Stack<Object> getBackStack() {
		return backstack;
	}

	@Override
	public void clearBackStack() {
		while (getBackStack().size() > 0) {
			getBackStack().pop();
		}
	}

	public void show(Object newState) {
		show(newState, false);
	}

	public void show(Object newState, boolean clearBackStack) {
		Log.d("Presenter", "state: " + newState.getClass().getName());
		if (currentState == null) {
			currentState = newState.getClass().getName();
			getBackStack().push(newState);
			return;
		}
		if (currentState.equals(newState.getClass().getName())) {
			return;
		}

		if (clearBackStack) {
			clearBackStack();
		}
		getBackStack().push(newState);
		getStateAnimator(newState, true).start();
	}

	public void hide(Object undoState) {
		getStateAnimator(undoState, false).start();
	}

	public String getCurrentState() {
		return currentState;
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

	public Map<String, Map<String, Transition>> getTransitions() {
		return transitions;
	}

	// Animations – adapted from StateManager

	private void resetAnimStats() {
		lastAnimPercentage = 0;
		frames = 0;
		avgFrameDuration = 0;
	}

	private void logAnimStats() {
		float avgFramePercentageChange = lastAnimPercentage / frames;
		float animFrameRate = 1000f / avgFrameDuration;
		Log.d("StateManager.AnimationStats (" + currentState + "," + destinationState + ") FrameRate:" + animFrameRate
			+ "f/s. TotalFrames:" + frames + ". AverageFrameDuration:" + avgFrameDuration
			+ "ms. AverageFramePercentageChange:" + avgFramePercentageChange + ". LastPercentageFromAnimator:"
			+ lastAnimPercentage);
	}

	public ValueAnimator getStateAnimator(final Object state, boolean forward) {
		final boolean goForward = forward;
		final Transition transition = getTransition(currentState, state.getClass().getName());

		if (transition == null) {
			throw new RuntimeException("No Transition defined for "
				+ currentState + " to " + state.getClass().getName());
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
				if (acceptAnimationUpdates) {
					float totalDuration = arg0.getCurrentPlayTime() / wrappedInterpolator.getLastInput();
					long remainingDuration = (long) (totalDuration - arg0.getCurrentPlayTime());

					//Our animation percentage
					lastAnimPercentage = (Float) arg0.getAnimatedValue();

					//Update our stats
					frames++;
					avgFrameDuration = ((float) arg0.getCurrentPlayTime()) / frames;

					if (remainingDuration < avgFrameDuration || remainingDuration < 0) {
						//If we are nearing the final frame, and we haven't reached one of our maximum values, lets do
						//so now, preventing some draw glitches between now and onStateFinalized.
						lastAnimPercentage = Math.round(lastAnimPercentage);
						transition.updateTransition(lastAnimPercentage, goForward);
					}
					else {
						transition.updateTransition(lastAnimPercentage, goForward);
					}
				}
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				transition.startTransition(goForward);
				resetAnimStats();
				acceptAnimationUpdates = true;
				destinationState = state.getClass().getName();
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				acceptAnimationUpdates = false;
				logAnimStats();
				transition.endTransition(goForward);
				transition.finalizeTransition(goForward);
				currentState = goForward ? transition.state2 : transition.state1;
				destinationState = null;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				acceptAnimationUpdates = false;
				logAnimStats();
			}
		});
		return animator;
	}
}
