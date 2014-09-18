package com.expedia.bookings.interfaces.helpers;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class StateListenerHandHolder<T> extends StateListenerHelper<T> {

	@Override
	public void onStateTransitionStart(T stateOne, T stateTwo) {
		setTouchabilityForTransition(stateOne, stateTwo);
		setVisibilityForTransition(stateOne, stateTwo);
		setHardwareLayerForTransition(stateOne, stateTwo, View.LAYER_TYPE_HARDWARE);
	}

	@Override
	public void onStateTransitionEnd(T stateOne, T stateTwo) {
		setHardwareLayerForTransition(stateOne, stateTwo, View.LAYER_TYPE_NONE);
	}

	@Override
	public void onStateFinalized(T state) {
		setTouchabilityForState(state);
		setVisibilityForState(state);
		setFragmentsForState(state);
		setMiscForState(state);
	}

	/*
	 * TRANSITION HELPERS
	 */

	public abstract void setHardwareLayerForTransition(T stateOne, T stateTwo, int layerType);

	public abstract void setVisibilityForTransition(T stateOne, T stateTwo);

	public abstract void setTouchabilityForTransition(T stateOne, T stateTwo);

	/*
	 * FINALIZE STATE HELPERS
	 */

	public abstract void setTouchabilityForState(T state);

	public abstract void setVisibilityForState(T state);

	public abstract void setFragmentsForState(T state);

	public abstract void setMiscForState(T state);
}
