package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.interfaces.ToolbarListener;

/**
 * Created by malnguyen on 2/28/15.
 */
public abstract class ExpandableCardView extends CardView implements View.OnFocusChangeListener {

	public ExpandableCardView(Context context) {
		super(context);
	}

	public ExpandableCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private EditText mCurrentEditText;
	public ToolbarListener mToolbarListener;
	private boolean isExpanded;

	public void setToolbarListener(ToolbarListener listener) {
		mToolbarListener = listener;
	}

	public boolean isExpanded() {
		return isExpanded;
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
			View v = mCurrentEditText.focusSearch(View.FOCUS_DOWN);
			v.requestFocus();
		}
	}

	public void setExpanded(boolean expand) {
		isExpanded = expand;
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

}
