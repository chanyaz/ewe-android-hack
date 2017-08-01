package com.expedia.bookings.presenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.expedia.bookings.animation.LooseLipsSinkShipsInterpolator;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.CompositeDisposable;

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

	protected Stack<Object> backstack = new Stack<>();

	// Transition vars
	private Map<String, Map<String, Transition>> transitions = new HashMap<>();
	private Transition toDefaultTransition;
	private String currentState;

	// Animation vars
	private boolean acceptAnimationUpdates = false;
	private ValueAnimator animator;

	private CompositeDisposable windowCompositeDisposable = new CompositeDisposable();
	private CompositeDisposable visibilityCompositeDisposable = new CompositeDisposable();

	public Presenter(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		windowCompositeDisposable.dispose();
		windowCompositeDisposable = new CompositeDisposable();
		addWindowSubscriptions();
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		unsubscribeWindowAtTeardown();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			visibilityCompositeDisposable.dispose();
			visibilityCompositeDisposable = new CompositeDisposable();
			addVisibilitySubscriptions();
		}
		else {
			unsubscribeVisibilityAtTeardown();
		}
	}

	boolean isHandlingBack = false;
	/*
	If a show(newState) command is requested while:
		1. Presenter is handling back() AND
		2. will eventually return true marking that back has been handled
	the backstack will go inconsistent because
		A. the UX will be displaying 'newState' whereas
		B. the backstack will be having 'currentChild' [see back(int flags) below] at the top of the stack.
	What should happen in the case 1) and 2) are True and a show() command is requested, is to delay the show() command
	till back() has been handled and post handling back(), the show() request should be honored.
	At one time, clients were handling it [see PR 3109 for example], by using delays [see explanation in PR #3024] which is not correct,
	as this is something which should be handled by Presenter level itself.
	*/
	//Show command Pair<New State To Be Shown, Flags>
	Pair<Object, Integer> showCommandRequestedDuringBackHandling = null;

	public boolean back() {
		return back(0);
	}

	public boolean back(int flags) {
		isHandlingBack = true;

		boolean didHandleBack;
		// If we're animating, ignore back presses.
		if (acceptAnimationUpdates) {
			didHandleBack = true;
		}
		else if (getBackStack().isEmpty()) {
			didHandleBack = false;
		}
		else {
			Object currentChild = getBackStack().pop();
			didHandleBack = handleBack(flags, currentChild);
		}

		isHandlingBack = false;
		/*
		If a show(newState) command was requested while:
			1. Presenter is handling back() AND
			2. will eventually return true marking that back has been handled
		we had delayed it for now, so honor it now!
		*/
		if (didHandleBack && showCommandRequestedDuringBackHandling != null) {
			show(showCommandRequestedDuringBackHandling.first, showCommandRequestedDuringBackHandling.second);
			showCommandRequestedDuringBackHandling = null;
		}

		return didHandleBack;
	}

	protected boolean handleBack(int flags, Object currentChild) {
		boolean didHandleBack;
		boolean backPressHandled = currentChild instanceof Presenter && ((Presenter) currentChild).back();

		// BackPress was not handled by the top child in the stack; handle it here.
		if (!backPressHandled) {
			if (getBackStack().isEmpty()) {
				currentState = null;
				didHandleBack = false;
			}
			else {
				Object previousState = getBackStack().pop();
				//Only test flags should go through!
				show(previousState, flags & TEST_FLAG_FORCE_NEW_STATE);
				didHandleBack = true;
			}
		}
		// BackPress has been handled by the top child in the stack.
		else {
			getBackStack().push(currentChild);
			didHandleBack = true;
		}
		return didHandleBack;
	}

	public Stack<Object> getBackStack() {
		return backstack;
	}

	public void clearBackStack() {
		while (getBackStack().size() > 0) {
			Object o = getBackStack().peek();
			if (o instanceof Presenter) {
				((Presenter) o).clearBackStack();
			}
			getBackStack().pop();
		}
		currentState = null;
	}

	public void show(Object newState) {
		show(newState, 0);
	}

	public void show(Object newState, int flags) {
		if (animator != null && animator.isRunning()) {
			animator.cancel(); // cancel running animations so we have correct current state (triggers: endAnimation())
		}
		handleNewState(newState, flags);
	}

	protected void handleNewState(Object newState, int flags) {
		if (isHandlingBack) {
			if (showCommandRequestedDuringBackHandling == null) {
				showCommandRequestedDuringBackHandling = new Pair(newState, flags);
				return;
			}
			else {
				throw new RuntimeException("Unexpected multiple show() commands while Presenter was handling back(). Expecting a max of 1 show() command while handling back().");
			}
		}

		Log.d(getClass().getSimpleName(), "show state: " + newState.getClass().getName());
		if (currentState == null) {
			// If we have a default transition added, execute it.
			if (toDefaultTransition != null && newState.getClass().getName().equals(toDefaultTransition.state2)) {
				toDefaultTransition.endTransition(true);
			}
			currentState = newState.getClass().getName();
			getBackStack().push(newState);
			return;
		}
		Log.d("Presenter", "Current state:" + currentState);
		// If we're already at a given state, or we are animating to a new state,
		// ignore any attempt to show a new state.
		if (currentState.equals(newState.getClass().getName()) || acceptAnimationUpdates) {
			return;
		}


		if ((flags & TEST_FLAG_FORCE_NEW_STATE) == 0) {
			animator = getStateAnimator(newState);
		}

		handleFlags(newState.getClass().getName(), flags);
		getBackStack().push(newState);

		if ((flags & TEST_FLAG_FORCE_NEW_STATE) == 0 && animator != null) {
			animator.start();
		}
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

	/**
	 * Note: This can be null if a transition has not explicitly been kicked off. In particular,
	 * when using the CLEAR flags, especially from one leaf state back to another leaf state, we
	 * do not keep track of "currentState", and this can be null. I would consider this a piece of
	 * the code that could me improved.
	 * @return tag for the current state
	 */
	@Nullable
	public String getCurrentState() {
		return currentState;
	}

	// Transition

	public static class Transition {
		public static final int DEFAULT_ANIMATION_DURATION = 300;

		public final String state1;
		public final String state2;
		public final Interpolator interpolator;
		public int duration;

		/**
		 * The empty transition can be used easily in the TransitionPortion class as part of a CompoundTransition.
		 * It doesn't need to know its state names because it is not handling the state progression.
		 */
		public Transition() {
			// Casting to string exists to inform the compiler which version of the constructor to use
			// In this case, that's the string one, so that we don't NPE
			this((String) null, null, null, DEFAULT_ANIMATION_DURATION);
		}

		public Transition(Class state1, Class state2) {
			this(state1.getName(), state2.getName());
		}

		public Transition(String state1, String state2) {
			this(state1, state2, null, DEFAULT_ANIMATION_DURATION);
		}

		public Transition(Class state1, Class state2, Interpolator interpolator, int duration) {
			this(state1.getName(), state2.getName(), interpolator, duration);
		}

		public Transition(String state1, String state2, Interpolator interpolator, int duration) {
			this.state1 = state1;
			this.state2 = state2;
			this.interpolator = interpolator;
			this.duration = duration;
		}

		public void setAnimationDuration(int duration) {
			this.duration = duration;
		}

		public int getAnimationDuration() {
			return duration;
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
			throw new RuntimeException("Transition already defined for " + transition.state1 + " to " + transition.state2);
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
		return transitions.containsKey(b) && transitions.get(b).containsKey(a);

	}

	public static abstract class DefaultTransition extends Transition {
		public DefaultTransition(String defaultState) {
			super(null, defaultState);
		}
	}

	public boolean hasDefaultTransition() {
		return (toDefaultTransition != null);
	}

	public Transition getDefaultTransition() {
		return toDefaultTransition;
	}

	public void addDefaultTransition(DefaultTransition transition) {
		if (toDefaultTransition != null && !toDefaultTransition.equals(transition)) {
			throw new RuntimeException("You can't have more than one default transition.");
		}
		toDefaultTransition = transition;
	}

	public ValueAnimator getStateAnimator(final Object state) {
		TransitionWrapper meta = getTransition(currentState, state.getClass().getName());

		if (meta == null || meta.transition == null) {
			throw new RuntimeException("No Transition defined for "
				+ currentState + " to " + state.getClass().getName());
		}

		return new TransitionAnimator(meta).animator();
	}

	private class TransitionAnimator implements AnimatorListener, AnimatorUpdateListener {
		private static final String TAG = "AnimationStats";

		private TransitionWrapper meta;
		private Transition transition;
		private LooseLipsSinkShipsInterpolator wrappedInterpolator;
		private String destinationState;

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
				wrappedInterpolator = new LooseLipsSinkShipsInterpolator(transition.interpolator);
			}
			else {
				wrappedInterpolator = new LooseLipsSinkShipsInterpolator(new AccelerateDecelerateInterpolator());
			}

			animator.setInterpolator(wrappedInterpolator);
			animator.addUpdateListener(this);
			animator.addListener(this);

			return animator;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			if (acceptAnimationUpdates) {
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
			acceptAnimationUpdates = true;
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			logAnimStats();
			transition.endTransition(meta.forward);
			currentState = meta.getDestination();
			acceptAnimationUpdates = false;
		}

		@Override
		public void onAnimationCancel(Animator animator) {
			logAnimStats();
			acceptAnimationUpdates = false;
		}

		@Override
		public void onAnimationRepeat(Animator animator) {
			// ignore
		}

		private void logAnimStats() {
			float avgFramePercentageChange = lastAnimPercentage / frames;
			float animFrameRate = 1000f / avgFrameDuration;
			Log.v(TAG, "Start: " + meta.getOrigin() + " --> " + meta.getDestination());
			Log.v(TAG, "  TotalFrames: " + frames);
		       	Log.v(TAG, "  FrameRate: " + animFrameRate + "f/s");
			Log.v(TAG, "  AverageFrameDuration: " + avgFrameDuration + "ms");
			Log.v(TAG, "  AverageFramePercentageChange: " + avgFramePercentageChange);
			Log.v(TAG, "  LastPercentageFromAnimator: " + lastAnimPercentage);
			Log.d(TAG, "End:   " + meta.getOrigin() + " --> " + meta.getDestination());
		}
	}

	//Subscriptions
	protected void addWindowSubscriptions() {

	}

	protected void addWindowSubscription(Disposable subscription) {
		windowCompositeDisposable.add(subscription);
	}

	@CallSuper
	protected void unsubscribeWindowAtTeardown() {
		windowCompositeDisposable.dispose();
	}

	protected void addVisibilitySubscriptions() {

	}

	protected void addVisibilitySubscription(Disposable subscription) {
		visibilityCompositeDisposable.add(subscription);
	}

	@CallSuper
	protected void unsubscribeVisibilityAtTeardown() {
		if (visibilityCompositeDisposable != null) {
			visibilityCompositeDisposable.dispose();
		}
	}
}
