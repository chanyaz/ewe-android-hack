package com.expedia.bookings.interfaces;

import android.graphics.Rect;

public interface IAddToTripListener<T> {

	/**
	 * This indicates the user wants to add this item to the trip (button click).
	 * The dominant controller (HOTELS/FLIGHTS) will begin animating into its state
	 * and begin the trip add network call. This gives the TRIP controller a chance
	 * position its views such that when performTripHandoff happens, they will be where
	 * they need to be and the animation can begin immediately.
	 * 
	 * @param data - we can pass data with this call.
	 * @param globalCoordinates - where we are animating from.
	 * @param shadeColor - our trip add loading screens have an overlay of this color which also gets handed to the TRIP controller on performTripHandoff.
	 */
	public void beginAddToTrip(T data, Rect globalCoordinates, int shadeColor);

	/**
	 * This marks when we begin the transition back to the global DEFAULT state,
	 * and the trip being added will be flying towards the bucket. It is at this time
	 * that HOTELS/FLIGHTS controller hides its trip info, and the TRIP controller
	 * takes over.
	 */
	public void performTripHandoff();
}
