package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This is an *extremely* specific custom TextView for the purposes
 * of drawing an "imprint" look from credit cards.
 * 
 * If we ever want to reuse this, it would need to be generalized quite
 * a bit more!
 */
public class CreditCardImprintTextView extends TextView {

	// Perhaps do this in dp at some point?
	private static final int BOTTOM_LAYER_TRANSLATION_PX = 4;
	private static final int TOP_LAYER_TRANSLATION_PX = 2;

	private static final int COLOR_BOTTOM = 0x3FFFFFFF;
	private static final int COLOR_MIDDLE = 0xFF8F8F8F;
	private static final int COLOR_TOP = 0xFFADADAD;

	public CreditCardImprintTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + BOTTOM_LAYER_TRANSLATION_PX);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Mirror the entire view horizontally
		canvas.save();
		canvas.scale(-1f, 1f, getWidth() / 2.0f, getHeight() / 2.0f);

		// Bottom layer
		canvas.save();
		canvas.translate(0, BOTTOM_LAYER_TRANSLATION_PX);
		setTextColor(COLOR_BOTTOM);
		super.onDraw(canvas);
		canvas.restore();

		// Middle layer
		setTextColor(COLOR_MIDDLE);
		super.onDraw(canvas);

		// Top layer
		canvas.save();
		canvas.translate(0, TOP_LAYER_TRANSLATION_PX);
		setTextColor(COLOR_TOP);
		super.onDraw(canvas);
		canvas.restore();

		canvas.restore();
	}
}
