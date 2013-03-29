package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * This is a LinearLayout that wont get pressed state from its parent.
 * Things within it can still be pressed
 *
 * Inspired by this blog post:
 * http://cyrilmottier.com/2011/11/23/listview-tips-tricks-4-add-several-clickable-areas/
 */
public class NoPressLinearLayout extends LinearLayout {

	public NoPressLinearLayout(Context context) {
		super(context);
	}

	public NoPressLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("NewApi")
	public NoPressLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setPressed(boolean pressed) {
		if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}

}
