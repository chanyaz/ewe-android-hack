package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RadioButton;

// A RadioButton that just renders the given drawable without all the hassle of
// trying to center an image with a seperate background in a RadioButton
public class ImageRadioButton extends RadioButton {

	private Drawable mDrawable;

	public ImageRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the background and stuff
		super.onDraw(canvas);

		if (mDrawable != null) {
			// CompoundButton
			mDrawable.setBounds(0, 0, getWidth(), getHeight());
			mDrawable.draw(canvas);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mDrawable != null) {
			int[] state = getDrawableState();
			mDrawable.setState(state);
			invalidate();
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || who == mDrawable;
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mDrawable != null) {
			mDrawable.jumpToCurrentState();
		}
	}

	public void setDrawable(Drawable mDrawable) {
		this.mDrawable = mDrawable;
		invalidate();
	}
}
