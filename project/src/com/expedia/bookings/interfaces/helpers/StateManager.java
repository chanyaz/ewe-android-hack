package com.expedia.bookings.interfaces.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;

import com.expedia.bookings.interfaces.IStateProvider;

/**
 * StateManager is designed as a transition helper for IStateProvider.
 * 
 * This class can handle maintaining the current, default, and transition states
 *
 * This class can provide transitions between states, both animated and unanimated.
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StateManager<T> {
	public static final int STATE_CHANGE_ANIMATION_DURATION = 300;

	private IStateProvider<T> mProvider;
	private ValueAnimator mAnimator;
	private T mDefaultState;
	private T mState;
	private T mDestinationState;
	private boolean mProviderIsFrag = false;
	private boolean mAcceptAnimationUpdates = false;

	/**
	 * Create a new StateManager
	 * 
	 * @param defaultState - the default state (what is returned from getState() if setState() is never called)
	 * @param provider - the provider control
	 */
	public StateManager(T defaultState, IStateProvider<T> provider) {
		setDefaultState(defaultState);
		setProvider(provider);
	}

	public T getState() {
		return mState != null ? mState : mDefaultState;
	}

	public T getDefaultState() {
		return mDefaultState;
	}

	public boolean isAnimating() {
		return mAnimator != null && mAnimator.isStarted();
	}

	private void setProvider(IStateProvider<T> provider) {
		mProvider = provider;
		if (provider instanceof Fragment) {
			mProviderIsFrag = true;
		}
	}

	public void setDefaultState(T defaultState) {
		mDefaultState = defaultState;
	}

	/**
	 * Set the state.
	 * 
	 * @param state - the state to move towards
	 * @param animate - do we want to animate
	 */
	public void setState(T state, boolean animate) {
		setState(state, animate ? STATE_CHANGE_ANIMATION_DURATION : 0);
	}

	/**
	 * Set the state.
	 * 
	 * @param state - the state to move towards
	 * @param duration - the duration of the animation ( if <= 0 no animation is performed)
	 */
	public void setState(T state, int duration) {
		if (duration <= 0) {
			if (isAnimating()) {
				mAnimator.cancel();
			}
			finalizeState(state, mProvider);
		}
		else {
			if (mAnimator == null) {
				mDestinationState = state;
				mAnimator = getTowardsStateAnimator(state, mProvider, duration);
				if (mAnimator == null) {
					finalizeState(state, mProvider);
				}
				else {
					mAnimator.start();
				}
			}
			else if (mDestinationState != state) {
				mDestinationState = state;
				mAnimator.reverse();
			}
		}
	}

	private void finalizeState(T state, IStateProvider<T> provider) {
		mState = state;
		mAnimator = null;
		mDestinationState = null;
		provider.finalizeState(state);
	}

	private boolean allowAnimationActions() {
		if (mProviderIsFrag) {
			return ((Fragment) mProvider).getActivity() != null;
		}
		return true;
	}

	private ValueAnimator getTowardsStateAnimator(final T state, final IStateProvider<T> provider, int duration) {
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				if (mAcceptAnimationUpdates) {
					provider.updateStateTransition(getState(), state, (Float) arg0.getAnimatedValue());
				}
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				if (allowAnimationActions()) {
					provider.startStateTransition(getState(), state);
				}
				mAcceptAnimationUpdates = true;
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAcceptAnimationUpdates = false;
				if (allowAnimationActions()) {
					provider.endStateTransition(getState(), state);
					finalizeState(mDestinationState, provider);
				}
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				mAcceptAnimationUpdates = false;
			}
		});
		return animator;
	}
}
