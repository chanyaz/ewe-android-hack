package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;

import com.expedia.bookings.R;

// A RadioButton that just renders the given drawable without all the hassle of
// trying to center an image with a seperate background in a RadioButton
public class ImageRadioButton extends RadioButton {

	private Drawable mDrawable;

	public ImageRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageRadioButton);
			mDrawable = a.getDrawable(R.styleable.ImageRadioButton_image);
			a.recycle();
		}

		if (mDrawable == null) {
			throw new RuntimeException("Must specify app:image=\"@drawable/mydrawable\" in xml");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the background and stuff
		super.onDraw(canvas);

		// CompoundButton
		mDrawable.setBounds(0, 0, getWidth(), getHeight());
		mDrawable.draw(canvas);
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
}
