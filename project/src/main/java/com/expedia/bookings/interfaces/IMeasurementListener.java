package com.expedia.bookings.interfaces;

/**
 * This is implemented by consumers of IMeasurementProvider.updateContentSize
 */
public interface IMeasurementListener {
	/**
	 * Fired when the content size has changed.
	 *
	 * @param totalWidth
	 * @param totalHeight
	 */
	void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape);
}
