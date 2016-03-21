package com.expedia.bookings.interfaces;

import android.widget.EditText;

import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.widget.ExpandableCardView;

public interface ToolbarListener {
	void setActionBarTitle(String title);

	void onWidgetExpanded(ExpandableCardView cardView);

	void onWidgetClosed();

	void onEditingComplete();

	void setMenuLabel(String label);

	void showRightActionButton(boolean show);

	void editTextFocus(EditText editText);

	void setNavArrowBarParameter(ArrowXDrawableUtil.ArrowDrawableType arrowDrawableType);
}
