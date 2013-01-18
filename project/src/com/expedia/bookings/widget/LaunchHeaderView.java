package com.expedia.bookings.widget;

import com.expedia.bookings.R;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class LaunchHeaderView extends RelativeLayout {
	private int mOffset;

	public LaunchHeaderView(Context context) {
		this(context, null);
	}

	public LaunchHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		inflate(context, R.layout.widget_launch_header, this);
	}

	public void show() {
		ObjectAnimator.ofFloat(this, "translationY", 0).start();
	}

	public void setOffset() {
		setOffset(mOffset, true);
	}

	public void setOffset(int offset) {
		setOffset(offset, false);
	}

	public void setOffset(int offset, boolean animate) {
		if (offset < -getHeight()) {
			offset = -getHeight();
		}

		mOffset = offset;

		if (animate) {
			ObjectAnimator.ofFloat(this, "translationY", offset).start();
		}
		else {
			ViewHelper.setTranslationY(this, offset);
		}
	}
}