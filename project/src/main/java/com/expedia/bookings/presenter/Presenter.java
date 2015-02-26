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

	// If set, and the object being added is already somewhere in the stack,
	// then instead of adding another instance of that object to the stack,
	// all of the other objects on top of it will be popped and this object
	// will be pushed to the top of the stack.
	public static final int FLAG_CLEAR_TOP = 0x04000000;
	// Remove all Objects on stack prior to pushing another Object on to it
	public static final int FLAG_CLEAR_BACKSTACK = 0x00008000;
	// ONLY USE IN TESTING - A flag to have the currentState be set without
	// the necessity of the StateAnimator getting to its end.
	public static final int TEST_FLAG_FORCE_NEW_STATE = 0x00000002;

	private Stack<Object> backstack;

	// Transition vars
	private Map<String, Map<String, Transition>> transitions = new HashMap<>();
	private Transition toDefaultTransition;
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

		// If we're animating, ignore back presses.
		if (acceptAnimationUpdates) {
			return true;
		}

		if (getBackStack().isEmpty()) {
			return false;
		}

		Object child = getBackStack().pop();
		boolean backPressHandled = child instanceof IPresenter && ((IPresenter) child).back();

		// BackPress was not handled by the child; handle it here.
		if (!backPressHandled) {

			if (getBackStack().isEmpty()) {
				currentState = null;
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
			Object o = getBackStack().peek();
			if (o instanceof IPresenter) {
				((IPresenter) o).clearBackStack();
			}
			getBackStack().pop();
		}
		currentState = null;
	}

	public void show(Object newState) {
		show(newState, 0);
	}

	public void show(Object newState, int flags) {
		Log.d("Presenter", "state: " + newState.getClass().getName());
		if (currentState == null) {
			// If we have a default transition added, execute it.
			if (toDefaultTransition != null && newState.getClass().getName().equals(toDefaultTransition.state2)) {
				toDefaultTransition.finalizeTransition(true);
			}
			currentState = newState.getClass().getName();
			getBackStack().push(newState);
			return;
		}
		// If we're already at a given state, or we are animating to a new state,
		// ignore any attempt to show a new state.
		if (currentState.equals(newState.getClass().getName()) || acceptAnimationUpdates) {
			return;
		}

		ValueAnimator animator = getStateAnimator(newState);
		handleFlags(newState.getClass().getName(), flags);
		getBackStack().push(newState);
		animator.start();
	}

	private void handleFlags(String newStateId, int flags) {
		if ((flags & FLAG_CLEAR_BACKSTACK) == FLAG_CLEAR_BACKSTACK) {
			clearBackStack();
		}

		if ((flags & FLAG_CLEAR_TOP) == FLAG_CLEAR_TOP) {
			int index = -1;
			for (int i = 0; i < backstack.size(); i++) {
				if (backstack.get(i).getClass().getName().equals(newStateId)) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				return;
			}
			while (backstack.size() > index) {
				backstack.pop();
			}
		}

		if ((flags & TEST_FLAG_FORCE_NEW_STATE) == TEST_FLAG_FORCE_NEW_STATE) {
			currentState = newStateId;
		}
	}

	public void hide(Object undoState) {
		getStateAnimator(undoState).start();
	}

	public String getCurrentState() {
		return currentState;
	}

	// Transition

	public static class Transition {

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

		public Transition(Class state1, Class state2, Interpolator interpolator, int duration) {
			this.state1 = state1.getName();
			this.state2 = state2.getName();
			this.interpolator = interpolator;
			this.duration = duration;
		}

		public Transition(Class state1, Class state2) {
			this(state1, state2, null, DEFAULT_ANIMATION_DURATION);
		}

		public void startTransition(boolean forward) {

		}

		public void updateTransition(float f, boolean forward) {

		}

		public void endTransition(boolean forward) {

		}

		public void finalizeTransition(boolean forward) {

		}

	}

	public static class TransitionWrapper {
		public final Transition transition;
		public final boolean forward;

		public TransitionWrapper(Transition transition, boolean forward) {
			this.transition = transition;
			this.forward = forward;
		}
	}

	public TransitionWrapper getTransition(String a, String b) {
		if (transitions.containsKey(a) && transitions.get(a).containsKey(b)) {
			return new TransitionWrapper(transitions.get(a).get(b), true);
		}
		else if (transitions.containsKey(b) && transitions.get(b).containsKey(a)) {
			return new TransitionWrapper(transitions.get(b).get(a), false);
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

	public static abstract class DefaultTransition extends Transition {
		public DefaultTransition(String defaultState) {
			super(null, defaultState);
		}

		public void startTransition(boolean forward) {
		}

		public void updateTransition(float f, boolean forward) {
		}

		public void endTransition(boolean forward) {
		}
	}

	public void addDefaultTransition(DefaultTransition transition) {
		if (toDefaultTransition != null) {
			throw new RuntimeException("You can't have more than one default transition.");
		}
		toDefaultTransition = transition;
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

	public ValueAnimator getStateAnimator(final Object state) {
		final TransitionWrapper meta = getTransition(currentState, state.getClass().getName());
		final Transition transition = meta.transition;

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
						transition.updateTransition(lastAnimPercentage, meta.forward);
					}
					else {
						transition.updateTransition(lastAnimPercentage, meta.forward);
					}
				}
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				transition.startTransition(meta.forward);
				resetAnimStats();
				acceptAnimationUpdates = true;
				destinationState = state.getClass().getName();
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				logAnimStats();
				transition.endTransition(meta.forward);
				transition.finalizeTransition(meta.forward);
				acceptAnimationUpdates = false;
				currentState = meta.forward ? transition.state2 : transition.state1;
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
