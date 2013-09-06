package com.expedia.bookings.activity;

import android.view.View;

import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;

/**
 * This interface was designed to be implemented by the various major playes in 
 * Tablet Results 2013. E.g. We create an ITabletResultsController that handles
 * all results fragments relating to HOTELS for example.
 */
public interface ITabletResultsController {
	/**
	 * When this is called, the global state of results is set. 
	 * ITabletResultsControllers are expected to set their visibility,
	 * availability, and touch handling accordingly.
	 * 
	 * @param state
	 */
	public void setGlobalResultsState(GlobalResultsState state);

	/**
	 * When this is called, it indicates we are moving towards a particular state.
	 * Thus if ITabletResultsControllers know they have views that will be displayed
	 * in the provided state, their visibility should be set accordingly.
	 * @param state
	 */
	public void setAnimatingTowardsVisibility(GlobalResultsState state);

	/**
	 * We need to be able to turn hardware layer rendering off and on
	 * for when we are moving to and from the FLIGHTS state.
	 */
	public void setHardwareLayerFlightsTransition(boolean useHardwareLayer);

	/**
	 * We need to be able to turn hardware layer rendering off and on
	 * for when we are moving to and from the HOTELS state.
	 */
	public void setHardwareLayerHotelsTransition(boolean useHardwareLayer);

	/**
	 * Sometimes other ITabletResultsControllers will want to control touches.
	 * setGlobalResultsState will be called sometime after this call.
	 * ITabletResultsControllers must ensure that their touchability state
	 * is updated in setGlobalResultsState as to not blockAllNewTouches forever.
	 * 
	 * @param requester - This view will be passed through the listeners, and typically
	 * the caller may not want to block touches for this particular view. implementers have the power.
	 */
	public void blockAllNewTouches(View requester);

	/**
	 * When tansitioning between DEFAULT and FLIGHTS modes, our various 
	 * ITabletResultsControllers need to know about it so they can animate their
	 * view correctly
	 * @param percentage
	 */
	public void animateToFlightsPercentage(float percentage);

	/**
	 * When tansitioning between DEFAULT and HOTELS modes, our various 
	 * ITabletResultsControllers need to know about it so they can animate their
	 * view correctly
	 * @param percentage
	 */
	public void animateToHotelsPercentage(float percentage);

	/**
	 * We want to make sure everyones columns are in sync with everyone else
	 * @param totalWidth
	 */
	public void updateColumnWidths(int totalWidth);
	
	
	/**
	 * The controllers should be in control of back presses too.
	 * 
	 * Note: Typically we only want to consume the back event if our controller
	 * is the current in control controller, if that makes sense at all
	 * 
	 * @return - return true if we consumed the back even, false otherwise
	 */
	public boolean handleBackPressed();
}
