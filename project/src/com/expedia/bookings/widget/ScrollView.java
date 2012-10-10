package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ScrollView extends android.widget.ScrollView {
	public interface OnScrollListener {
		void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy);
	}

	private OnScrollListener mOnScrollListener = null;

	public ScrollView(Context context) {
		super(context);
	}

	public ScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}
}
