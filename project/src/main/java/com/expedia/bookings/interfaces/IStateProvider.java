package com.expedia.bookings.interfaces;

/**
 * Implementers of this interface should be controllers of a state machine.
 * <p/>
 * The finalizeState method is called to set the state machines state.
 * <p/>
 * The StateTransition methods help prepare/tell/cleanupafter our state transitions.
 *
 * @param <T> - The State type - this could be anything, but at the time of this writing
 *            makes the most sense to be a custom enum type.
 */
public interface IStateProvider<T> {

	/**
	 * This should be called at the very start of a transition between stateOne and stateTwo, and
	 * it should be propagated to the onStateTransitionStart() method of our listener collection.
	 * <p/>
	 * This is where listeners are likely to set things like animation starting visibilities/positions etc.
	 * <p/>
	 * CONTRACT:
	 * All calls to this method are expected to:
	 * 1) Be followed by n calls to setStateTransitionPercentage (where n can be 0)
	 * 2) Be followed by a single call to finalizeStateTransition (even if we never made it to stateTwo).
	 *
	 * @param stateOne - the origin state
	 * @param stateTwo - the destination state
	 */
	void startStateTransition(T stateOne, T stateTwo);

	/**
	 * This should be called when the percentage between states is changed, and it should propagate to
	 * the onStateTransitionUpdate method of our listener collection.
	 * <p/>
	 * NOTE: percentage does not indicate state. If percentage == 1f our view should appear to be in stateTwo,
	 * but we may be dragging between states, and we may leave this percentage just as easily, so NEVER assume
	 * that percentage == 1f suggests a complete state transition.
	 *
	 * @param stateOne   - the origin state
	 * @param stateTwo   - the destination state
	 * @param percentage - the percentage between the two states (0f - 1f)
	 */
	void updateStateTransition(T stateOne, T stateTwo, float percentage);

	/**
	 * This should be called when the state transition is completing, and
	 * it should be propagated to the onStateTransitionEnd() method of our listener collection.
	 * <p/>
	 * This DOES NOT mean we have entered stateTwo. This just means we have stopped transitioning
	 * between two states.
	 *
	 * @param stateOne - the origin state
	 * @param stateTwo - the destination state.
	 */
	void endStateTransition(T stateOne, T stateTwo);

	/**
	 * This should be called to definitively set the state of ourself and the listener collection (via onStateFinalized)
	 * <p/>
	 * This MUST be called to commit to a certain state. Calling this should work at any time. E.g.
	 * If someone calls finalizeState(State.STATE_ONE); we should be in STATE_ONE in all aspects -
	 * visibility, position, touchability, fragment attachment, favorite candy bar, etc.
	 *
	 * @param state
	 */
	void finalizeState(T state);

	/**
	 * Register a listener to receive state change/update/transition events
	 *
	 * @param listener
	 * @param fireFinalizeState - If true we should fire finalizeState on this listener for the current state.
	 */
	void registerStateListener(IStateListener<T> listener, boolean fireFinalizeState);

	/**
	 * Unregister a listener.
	 *
	 * @param listener
	 */
	void unRegisterStateListener(IStateListener<T> listener);
}
