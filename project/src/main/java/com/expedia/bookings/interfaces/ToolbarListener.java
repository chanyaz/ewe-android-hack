package com.expedia.bookings.interfaces;

import com.expedia.bookings.widget.ExpandableCardView;

/**
 * Created by malnguyen on 2/23/15.
 */
public interface ToolbarListener {
	public void setActionBarTitle(String title);

	public void onWidgetExpanded(ExpandableCardView cardView);

	public void onWidgetClosed();

	public void onEditingComplete();
}
