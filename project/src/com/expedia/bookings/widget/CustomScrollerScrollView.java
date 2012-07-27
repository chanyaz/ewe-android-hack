package com.expedia.bookings.widget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public abstract class CustomScrollerScrollView extends ScrollView {

	private Object mCustomScroller;

	public CustomScrollerScrollView(Context context) {
		this(context, null);
	}

	public CustomScrollerScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.scrollViewStyle);
	}

	public CustomScrollerScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Override the (already initialized in super()) scroller
		setScroller(initScroller());
	}

	/**
	 * Subclasses should return either a Scroller (for < API9) or OverScroller (for >= API9).
	 * @return
	 */
	public abstract Object initScroller();

	public boolean isScrollerFinished() {
		Object scroller = getScroller();
		try {
			Method method = scroller.getClass().getMethod("isFinished");
			return (Boolean) method.invoke(scroller);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public Object getScroller() {
		if (mCustomScroller == null) {
			try {
				Field field = ScrollView.class.getDeclaredField("mScroller");
				field.setAccessible(true);
				mCustomScroller = field.get(this);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return mCustomScroller;
	}

	private void setScroller(Object scroller) {
		try {
			Field field = ScrollView.class.getDeclaredField("mScroller");
			field.setAccessible(true);
			field.set(this, scroller);
			mCustomScroller = scroller;
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
