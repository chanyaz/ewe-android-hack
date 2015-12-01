package com.expedia.bookings.widget;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;
import android.widget.ScrollView;

public abstract class CustomScrollerScrollView extends com.expedia.bookings.widget.ScrollView {

	private OverScroller mCustomScroller;

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
	 * Subclasses should return OverScroller (for >= API9).
	 * @return
	 */
	protected abstract OverScroller initScroller();

	public boolean isScrollerFinished() {
		return getScroller().isFinished();
	}

	public OverScroller getScroller() {
		return mCustomScroller;
	}

	private void setScroller(OverScroller scroller) {
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
