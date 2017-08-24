package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.ToolbarListener;

public abstract class ExpandableCardView extends FrameLayout
	implements View.OnFocusChangeListener, View.OnClickListener,
	View.OnTouchListener {

	public ExpandableCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(this);
		setOnTouchListener(this);
		setBackgroundResource(R.drawable.card_background);
	}

	private EditText mCurrentEditText;
	public ToolbarListener mToolbarListener;
	public final List<IExpandedListener> expandedListeners = new ArrayList();
	private boolean isExpanded;

	public void addExpandedListener(IExpandedListener listener) {
		expandedListeners.add(listener);
	}

	@Override
	public void onClick(View v) {
		if (!isExpanded) {
			setExpanded(true);
		}
	}

	public void setToolbarListener(ToolbarListener listener) {
		mToolbarListener = listener;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		mCurrentEditText = (EditText) v;

		if (mToolbarListener != null) {
			mToolbarListener.showRightActionButton(true);
			mToolbarListener.setCurrentViewFocus(mCurrentEditText);
		}

	}

	public void setExpanded(boolean expand) {
		setExpanded(expand, true);
	}

	public void setExpanded(boolean expand, boolean animate) {
		if (isExpanded == expand) {
			return;
		}
		isExpanded = expand;

		for (IExpandedListener listener : expandedListeners) {
			if (expand) {
				listener.expanded(this);
			}
			else {
				listener.collapsed(this);
			}
		}

		if (!animate) {
			return;
		}
		if (mToolbarListener != null) {
			if (expand) {
				mToolbarListener.onWidgetExpanded(this);
				mToolbarListener.showRightActionButton(false);
			}
			else {
				mToolbarListener.onWidgetClosed();
			}
		}
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	// Is the user focus on the last edittext?
	public abstract boolean getMenuDoneButtonFocus();

	// Title to set on the Done button
	public abstract String getMenuButtonTitle();

	// Title to set on the toolbar once the widget opens
	public abstract String getActionBarTitle();

	// Actions to perform once the user presses done on the toolbar
	public abstract void onMenuButtonPressed();

	// Actions to perform once the user has logged in
	public abstract void onLogin();

	// Actions to perform once the user has logged out
	public abstract void onLogout();

	// Is the status of the widget complete?
	public abstract boolean isComplete();

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return isExpanded;
	}

	public interface IExpandedListener {
		void expanded(ExpandableCardView view);

		void collapsed(ExpandableCardView view);
	}
}
