package com.expedia.bookings.utils;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

/**
 * Inspired by http://stackoverflow.com/questions/9618835/apply-two-different-font-styles-to-a-textview
 */

public class TypefaceSpan extends android.text.style.TypefaceSpan {

	private Typeface mTypeface;

	public TypefaceSpan(Typeface typeface) {
		super("");
		mTypeface = typeface;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		apply(ds, mTypeface);
	}

	@Override
	public void updateMeasureState(TextPaint paint) {
		apply(paint, mTypeface);
	}

	private static void apply(Paint paint, Typeface typeface) {
		paint.setTypeface(typeface);
	}

}
