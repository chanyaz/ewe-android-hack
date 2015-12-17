package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/*
 * Pre-JB, SeekBar did not implement getThumb(), so here we're
 * taking matters into our own hands.
 */

public class SeekBar extends android.widget.SeekBar {

	Drawable mThumb;

	public SeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		mThumb = thumb;
	}

	@Override
	public Drawable getThumb() {
		return mThumb;
	}
}
