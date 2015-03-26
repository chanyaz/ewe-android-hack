package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.interfaces.ToolbarListener;

/**
 * Created by malnguyen on 2/28/15.
 */
public abstract class ExpandableCardView extends CardView implements View.OnFocusChangeListener, View.OnClickListener,
	View.OnTouchListener {

	public ExpandableCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(this);
		setOnTouchListener(this);
	}

	private EditText mCurrentEditText;
	public ToolbarListener mToolbarListener;
	private boolean isExpanded;

	@Override
	public void onClick(View v) {
		if (!isExpanded) {
			setExpanded(true);
		}
	}

	public void setToolbarListener(ToolbarListener listener) {
		mToolbarListener = listener;
	}

	public EditText getFocusedEditText() {
		return mCurrentEditText;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		mCurrentEditText = (EditText) v;

		if (getDoneButtonFocus() && mToolbarListener != null) {
			mToolbarListener.onEditingComplete();
		}

	}

	public void setNextFocus() {
		if (mCurrentEditText != null) {
			View v = mCurrentEditText.focusSearch(View.FOCUS_RIGHT);
			if (v == null) {
				v = mCurrentEditText.focusSearch(View.FOCUS_DOWN);
			}
			if (v != null)  {
				v.requestFocus();
			}
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
		if (!animate) {
			return;
		}
		if (mToolbarListener != null) {
			if (expand) {
				mToolbarListener.onWidgetExpanded(this);
			}
			else {
				mToolbarListener.onWidgetClosed();
			}
		}
	}

	// Is the user focus on the last edittext?
	public abstract boolean getDoneButtonFocus();

	// Title to set on the toolbar once the widget opens
	public abstract String getActionBarTitle();

	// Actions to perform once the user presses done on the toolbar
	public abstract void onDonePressed();

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
}
