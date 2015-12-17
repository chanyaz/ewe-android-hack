package com.expedia.bookings.interfaces;

/**
 * This is to be implemented by a class (probably an Activity) that knows about current
 * dimensions.
 * <p/>
 * It is typical when working with fragments, that they need to measure when they are attached.
 * This allows us to tell the fragments the kind of space they are dealing with so they can perform sizing/placement/etc
 * before drawing/measuring.
 */
public interface IMeasurementProvider {
	/**
	 * Update any local size related values, and then tell the listeners of the new size.
	 *
	 * @param totalWidth
	 * @param totalHeight
	 */
	void updateContentSize(int totalWidth, int totalHeight);

	/**
	 * Register an IMeasurementListener to receive onContentSizeUpdated events
	 *
	 * @param listener
	 * @param fireImmediately - If we have measured in the past, we will pass those values to the listener immediately.
	 */
	void registerMeasurementListener(IMeasurementListener listener, boolean fireImmediately);

	/**
	 * Unregister an IMeasurementListener
	 *
	 * @param listener
	 */
	void unRegisterMeasurementListener(IMeasurementListener listener);
}
