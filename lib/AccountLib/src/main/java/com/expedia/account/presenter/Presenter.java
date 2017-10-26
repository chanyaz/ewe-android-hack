package com.expedia.account.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.expedia.account.util.Events;

import timber.log.Timber;

/**
 * A FrameLayoutPresenter that maintains child presenters and animates
 * transitions between them.
 */
public class Presenter extends FrameLayout {

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

	//Just do finalize() for each animation. Overlaps with the test_flag_force_new_state so that we don't do animation things.
	public static final int FLAG_SKIP_ANIMATION_TIME = 0x00000022;

	// An item in the backstack can be either:
	// 1. A String object, in which case we're cool
	// 2. A <? extends Presenter> object, in which case the state will be gleaned from .getClass()
	private Stack<Object> backstack = new Stack<>();

	// Transition vars
	private Map<String, Map<String, Transition>> transitions = new HashMap<>();
	private Transition toDefaultTransition;
	private String currentState;

	// Animation vars
	boolean isReadyToAnimate = true;

	// View lifecycle

	public Presenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
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

	public boolean back() {
		return back(0);
	}

	public boolean back(int flags) {
		// If we're animating, ignore back presses.
		if (!isReadyToAnimate) {
			return true;
		}

		if (backstack.isEmpty()) {
			return false;
		}

		Object currentChild = backstack.pop();
		boolean backPressHandled = currentChild instanceof Presenter && ((Presenter) currentChild).back();

		// BackPress was not handled by the top child in the stack; handle it here.
		if (!backPressHandled) {

			if (backstack.isEmpty()) {
				currentState = null;
				return false;
			}

			Object previousState = backstack.pop();
			//Only test flags should go through!
			show(previousState, flags & TEST_FLAG_FORCE_NEW_STATE);
			return true;
		}

		// BackPress has been handled by the top child in the stack.
		else {
			backstack.push(currentChild);
			return true;
		}
	}

	public Stack<Object> getBackStack() {
		return backstack;
	}

	public Transition getDefaultTransition() {
		return toDefaultTransition;
	}

	public void clearBackStack() {
		while (backstack.size() > 0) {
			Object o = backstack.peek();
			if (o instanceof Presenter) {
				((Presenter) o).clearBackStack();
			}
			backstack.pop();
		}
		currentState = null;
	}

	public void show(Object newState) {
		show(newState, 0);
	}

	public void show(Object newState, int flags) {
		String newStateAsString = (newState instanceof String) ? (String) newState : newState.getClass().getName();
		Timber.d("state: " + newStateAsString);
		if (currentState == null) {
			// If we have a default transition added, execute it.
			if (toDefaultTransition != null && newStateAsString.equals(toDefaultTransition.state2)) {
				toDefaultTransition.finalizeTransition(true);
			}
			currentState = newStateAsString;
			backstack.push(newStateAsString);
			return;
		}
		// If we're already at a given state, or we are animating to a new state,
		// ignore any attempt to show a new state.
		if (currentState.equals(newStateAsString) || !isReadyToAnimate) {
			return;
		}

		ValueAnimator animator = null;
		if ((flags & TEST_FLAG_FORCE_NEW_STATE) == 0) {
			animator = getStateAnimator(newStateAsString);
		}

		if ((flags & FLAG_SKIP_ANIMATION_TIME) != 0) {
			TransitionAnimator anim = getStateTransitionAnimator(newStateAsString);
			animator = anim.animator();
			anim.onAnimationStart(animator);
			anim.transition.updateTransition(1, anim.meta.forward);
			anim.onAnimationEnd(animator);
		}

		handleFlags(newState, flags);
		backstack.push(newStateAsString);

		if ((flags & TEST_FLAG_FORCE_NEW_STATE) == 0 && animator != null) {
			animator.start();
		}

	}

	protected void onTransitionComplete() {
		//Do nothing, let children do things.
	}

	private void handleFlags(Object newState, int flags) {
		String newStateAsString = newState instanceof String ? (String) newState : newState.getClass().getName();

		if ((flags & FLAG_CLEAR_BACKSTACK) == FLAG_CLEAR_BACKSTACK) {
			clearBackStack();
		}

		if ((flags & FLAG_CLEAR_TOP) == FLAG_CLEAR_TOP) {
			int index = -1;
			for (int i = 0; i < backstack.size(); i++) {
				Object item = backstack.get(i);
				String itemAsString = item instanceof String ? (String) item : item.getClass().getName();
				if (itemAsString.equals(newStateAsString)) {
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
			currentState = newStateAsString;
		}
	}

	/**
	 * Note: This can be null if a transition has not explicitly been kicked off. In particular,
	 * when using the CLEAR flags, especially from one leaf state back to another leaf state, we
	 * do not keep track of "currentState", and this can be null. I would consider this a piece of
	 * the code that could me improved.
	 *
	 * @return tag for the current state
	 */
	@Nullable
	public String getCurrentState() {
		return currentState == null ? null : currentState;
	}

	// Transition

	public static class Transition {
		public static final int DEFAULT_ANIMATION_DURATION = 300;

		public final String state1;
		public final String state2;
		public final Interpolator interpolator;
		public final int duration;

		public Transition() {
			this(null, null, null, DEFAULT_ANIMATION_DURATION);
		}

		public Transition(String state1, String state2) {
			this(state1, state2, null, DEFAULT_ANIMATION_DURATION);
		}

		public Transition(String state1, String state2, Interpolator interpolator) {
			this(state1, state2, interpolator, DEFAULT_ANIMATION_DURATION);
		}

		public Transition(String state1, String state2, Interpolator interpolator, int duration) {
			this.state1 = state1;
			this.state2 = state2;
			this.interpolator = interpolator;
			this.duration = duration;
		}

		public void startTransition(boolean forward) {
			// empty
		}

		public void updateTransition(float f, boolean forward) {
			// empty
		}

		public void endTransition(boolean forward) {
			// empty
		}

		public void finalizeTransition(boolean forward) {
			// empty
		}
	}

	public static class TransitionWrapper {
		public final Transition transition;
		public final boolean forward;

		public TransitionWrapper(Transition transition, boolean forward) {
			this.transition = transition;
			this.forward = forward;
		}

		public String getOrigin() {
			return forward ? transition.state1 : transition.state2;
		}

		public String getDestination() {
			return forward ? transition.state2 : transition.state1;
		}
	}

	public TransitionWrapper getTransition(String a, String b) {
		if (transitions.containsKey(a) && transitions.get(a).containsKey(b)) {
			return new TransitionWrapper(transitions.get(a).get(b), true);
		}
		else if (transitions.containsKey(b) && transitions.get(b).containsKey(a)) {
			return new TransitionWrapper(transitions.get(b).get(a), false);
		}

		throw new RuntimeException("No Transition defined for " + a + " to " + b);
	}

	public void addTransition(Transition transition) {
		if (exists(transition)) {
			throw new RuntimeException(
				"Transition already defined for " + transition.state1 + " to " + transition.state2);
		}

		if (!transitions.containsKey(transition.state1)) {
			transitions.put(transition.state1, new HashMap<String, Transition>());
		}
		transitions.get(transition.state1).put(transition.state2, transition);
	}

	private boolean exists(Transition transition) {
		return exists(transition.state1, transition.state2);
	}

	private boolean exists(String a, String b) {
		if (transitions.containsKey(a) && transitions.get(a).containsKey(b)) {
			return true;
		}
		if (transitions.containsKey(b) && transitions.get(b).containsKey(a)) {
			return true;
		}

		return false;
	}

	public static abstract class DefaultTransition extends Transition {
		public DefaultTransition(@NonNull String defaultState) {
			super(null, defaultState);
		}

		@Override
		public final void startTransition(boolean forward) {
			throw new RuntimeException("startTransition should never be called on a DefaultTransition");
			// empty
		}

		@Override
		public final void updateTransition(float f, boolean forward) {
			throw new RuntimeException("updateTransition should never be called on a DefaultTransition");
			// empty
		}

		@Override
		public final void endTransition(boolean forward) {
			throw new RuntimeException("endTransition should never be called on a DefaultTransition");
			// empty
		}
	}

	public static class DefaultCompoundTransition extends CompoundTransition {
		public DefaultCompoundTransition(@NonNull String defaultState, @NonNull Transition... transitions) {
			super(null, defaultState, transitions);
		}

		@Override
		public final void startTransition(boolean forward) {
			throw new RuntimeException("startTransition should never be called on a DefaultTransition");
			// empty
		}

		@Override
		public final void updateTransition(float f, boolean forward) {
			throw new RuntimeException("updateTransition should never be called on a DefaultTransition");
			// empty
		}

		@Override
		public final void endTransition(boolean forward) {
			throw new RuntimeException("endTransition should never be called on a DefaultTransition");
			// empty
		}
	}

	public void addDefaultTransition(Transition transition) {
		if (toDefaultTransition != null) {
			throw new RuntimeException("You can't have more than one default transition.");
		}
		toDefaultTransition = transition;
	}

	private TransitionAnimator getStateTransitionAnimator(final String state) {
		TransitionWrapper meta = getTransition(currentState, state);

		if (meta == null || meta.transition == null) {
			throw new RuntimeException("No Transition defined for "
				+ (currentState == null ? "null" : currentState) + " to " + state);
		}

		return new TransitionAnimator(meta);
	}

	public ValueAnimator getStateAnimator(final String state) {
		return getStateTransitionAnimator(state).animator();
	}

	private class TransitionAnimator implements AnimatorListener, AnimatorUpdateListener {
		private static final String TAG = "AnimationStats";

		private TransitionWrapper meta;
		private Transition transition;
		private ObservableInterpolator wrappedInterpolator;

		// Stats
		private float lastAnimPercentage = 0.0f;
		private int frames = 0;
		private float avgFrameDuration = 0.0f;

		public TransitionAnimator(TransitionWrapper meta) {
			this.meta = meta;
			this.transition = meta.transition;
		}

		public ValueAnimator animator() {
			ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
			animator.setDuration(transition.duration);

			if (transition.interpolator != null) {
				wrappedInterpolator = new ObservableInterpolator(transition.interpolator);
			}
			else {
				wrappedInterpolator = new ObservableInterpolator(new AccelerateDecelerateInterpolator());
			}

			animator.setInterpolator(wrappedInterpolator);
			animator.addUpdateListener(this);
			animator.addListener(this);

			return animator;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			if (!isReadyToAnimate) {
				float totalDuration = animator.getCurrentPlayTime() / wrappedInterpolator.getLastInput();
				long remainingDuration = (long) (totalDuration - animator.getCurrentPlayTime());

				//Our animation percentage
				lastAnimPercentage = (Float) animator.getAnimatedValue();

				//Update our stats
				frames++;
				avgFrameDuration = ((float) animator.getCurrentPlayTime()) / frames;

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

		@Override
		public void onAnimationStart(Animator animator) {
			transition.startTransition(meta.forward);
			isReadyToAnimate = false;
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			logAnimStats();
			transition.endTransition(meta.forward);
			transition.finalizeTransition(meta.forward);
			currentState = meta.getDestination();
			isReadyToAnimate = true;
			onTransitionComplete();
		}

		@Override
		public void onAnimationCancel(Animator animator) {
			logAnimStats();
			isReadyToAnimate = true;
		}

		@Override
		public void onAnimationRepeat(Animator animator) {
			// ignore
		}

		private void logAnimStats() {
			float avgFramePercentageChange = lastAnimPercentage / frames;
			float animFrameRate = 1000f / avgFrameDuration;
			Timber.v("Start: " + meta.getOrigin() + " --> " + meta.getDestination());
			Timber.v("  TotalFrames: " + frames);
			Timber.v("  FrameRate: " + animFrameRate + "f/s");
			Timber.v("  AverageFrameDuration: " + avgFrameDuration + "ms");
			Timber.v("  AverageFramePercentageChange: " + avgFramePercentageChange);
			Timber.v("  LastPercentageFromAnimator: " + lastAnimPercentage);
			Timber.d("End:   " + meta.getOrigin() + " --> " + meta.getDestination());
		}
	}

	//////////
	/// Saved State
	/////////


	protected void setCurrentState(String nextState) {
		currentState = nextState;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		Object[] stackArray = getBackStack().toArray();
		ArrayList<String> stringStates = new ArrayList<>();
		//We assume/hope each object is a string because in our specific presenter (AccountView) we don't use nested presenters.
		//So, y'know, if you move these changes elsewhere, please be aware of that.
		for (Object state : stackArray) {
			if (state instanceof String) {
				stringStates.add((String) state);
			}
		}

		bundle.putStringArrayList("stack", stringStates);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			final ArrayList<String> savedStates = bundle.getStringArrayList("stack");
			this.post(new Runnable() {
				@Override
				public void run() {
					restoreBackStack(savedStates);
				}
			});
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}

	protected boolean restoreBackStack(ArrayList<String> unfrozenBackStack) {
		if (unfrozenBackStack != null) {
			Stack<Object> backstack = getBackStack();
			backstack.clear();
			for (String nextState : unfrozenBackStack) {
				show(nextState, FLAG_SKIP_ANIMATION_TIME);
			}
			return true;
		}
		return false;
	}


}
