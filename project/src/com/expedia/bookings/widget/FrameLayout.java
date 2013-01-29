package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.mobiata.android.Log;

public class FrameLayout extends android.widget.FrameLayout {
	public interface OnSizeChangedListener {
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}

	private OnSizeChangedListener mOnSizeChangedListener;

	public FrameLayout(Context context) {
		super(context);
	}

	public FrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Log.e("DOUG: onSizeChanged: " + w + "x" + h);

		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
		}
	}

	public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
		mOnSizeChangedListener = onSizeChangedListener;
	}
}
