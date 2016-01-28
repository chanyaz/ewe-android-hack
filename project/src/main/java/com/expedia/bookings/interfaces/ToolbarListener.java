package com.expedia.bookings.interfaces;

import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.widget.ExpandableCardView;

public interface ToolbarListener {
	void setActionBarTitle(String title);

	void onWidgetExpanded(ExpandableCardView cardView);

	void onWidgetClosed();

	void onEditingComplete();

	void setMenuLabel(String label);

	void showRightActionButton(boolean show);

	void setNavArrowBarParameter(ArrowXDrawableUtil.ArrowDrawableType arrowDrawableType);
}
