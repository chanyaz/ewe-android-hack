package com.expedia.bookings.interfaces;

/**
 * Created by malnguyen on 2/23/15.
 */
public interface ToolbarListener {
	public void setActionBarTitle(String title);

	public void onWidgetExpanded();

	public void onWidgetClosed();
}
