package com.expedia.bookings.widget;

import java.util.HashSet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollView extends android.widget.ScrollView {
	public interface OnScrollListener {
		void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy);
	}

	private final HashSet<OnScrollListener> mOnScrollListener = new HashSet<OnScrollListener>();

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
	public boolean onTouchEvent(MotionEvent ev) {
		if (isEnabled()) {
			return super.onTouchEvent(ev);
		}

		return false;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);

		for (OnScrollListener listener : mOnScrollListener) {
			listener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public void addOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener.add(onScrollListener);
	}
}
