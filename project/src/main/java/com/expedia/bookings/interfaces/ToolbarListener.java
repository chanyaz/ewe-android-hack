package com.expedia.bookings.interfaces;

import android.view.View;

import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.widget.ExpandableCardView;

public interface ToolbarListener {
	void setActionBarTitle(String title);

	void onWidgetExpanded(ExpandableCardView cardView);

	void onWidgetClosed();

	void onEditingComplete();

	void enableRightActionButton(boolean enable);

	void setMenuLabel(String label);

	void showRightActionButton(boolean show);

	void setCurrentViewFocus(View view);

	void setNavArrowBarParameter(ArrowXDrawableUtil.ArrowDrawableType arrowDrawableType);
}
