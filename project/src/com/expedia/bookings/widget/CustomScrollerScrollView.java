package com.expedia.bookings.widget;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;
import android.widget.ScrollView;

public class CustomScrollerScrollView extends ScrollView {

	private OverScroller mOverriddenScroller;
	
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

	public OverScroller initScroller() {
		return new OverScroller(getContext());
	}

	public OverScroller getScroller() {
		if (mOverriddenScroller == null) {
			try {
				Field field = ScrollView.class.getDeclaredField("mScroller");
				field.setAccessible(true);
				mOverriddenScroller = (OverScroller) field.get(this);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
			
		}
		return mOverriddenScroller;
	}

	private void setScroller(OverScroller scroller) {
		try {
			Field field = ScrollView.class.getDeclaredField("mScroller");
			field.setAccessible(true);
			field.set(this, scroller);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
