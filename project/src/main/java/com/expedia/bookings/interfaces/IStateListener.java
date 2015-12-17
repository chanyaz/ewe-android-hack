package com.expedia.bookings.interfaces;

/**
 * This class is designed to listen to events fired from an IStateProvider<T>
 *
 * @param <T> - The State type - this could be anything, but at the time of this writing
 *            makes the most sense to be a custom enum type.
 */
public interface IStateListener<T> {
	/**
	 * This gets called at the beginning of a state transition.
	 * In this method we are expected to set up view visibility,
	 * layerType, position, and anything else required to start
	 * transitioning between the provided states.
	 *
	 * @param stateOne - the origin state
	 * @param stateTwo - the destination state.
	 */
	void onStateTransitionStart(T stateOne, T stateTwo);

	/**
	 * This gets called to indicate updated progress in a transition.
	 * <p/>
	 * percentage == 1f DOES NOT mean we have entered stateTwo. If we are dragging
	 * between states for instance, we may hit percentage == 1f, and then drag right
	 * back down to 0f.
	 *
	 * @param stateOne   - the origin state
	 * @param stateTwo   - the destination state
	 * @param percentage - between 0f and 1f.
	 */
	void onStateTransitionUpdate(T stateOne, T stateTwo, float percentage);

	/**
	 * This gets called when the state transition is completing.
	 * In this method we are expected to unset certain things that got set
	 * in onStateTransitionStart, such as hardware layers.
	 * <p/>
	 * This DOES NOT mean we have entered stateTwo. This just means we have stopped transitioning
	 * between two states. E.g. we may be dragging between two states, this method should not care
	 * where we end up, because if the state changed onStateFinalized SHOULD ALWAYS be called.
	 *
	 * @param stateOne - the origin state
	 * @param stateTwo - the destination state.
	 */
	void onStateTransitionEnd(T stateOne, T stateTwo);

	/**
	 * This gets called to set the state, and should set everything to its
	 * final position/visibility/touchability/etc. for the provided state.
	 *
	 * @param state
	 */
	void onStateFinalized(T state);
}
