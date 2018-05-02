package com.mobiata.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class SpinnerWithCloseListener extends Spinner {

	private OnSpinnerCloseListener mListener;
	private boolean mOpenInitiated = false;

	public SpinnerWithCloseListener(Context context) {
		super(context);
	}

	public SpinnerWithCloseListener(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SpinnerWithCloseListener(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean performClick() {
		mOpenInitiated = true;
		return super.performClick();
	}

	public void setOnSpinnerCloseListener(OnSpinnerCloseListener listener) {
		mListener = listener;
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if (mOpenInitiated && hasFocus) {
			performClosedEvent();
		}
	}

	private void performClosedEvent() {
		mOpenInitiated = false;
		if (mListener != null) {
			mListener.onSpinnerClosed(this);
		}
	}

	public interface OnSpinnerCloseListener {
		void onSpinnerClosed(Spinner spinner);
	}
}
