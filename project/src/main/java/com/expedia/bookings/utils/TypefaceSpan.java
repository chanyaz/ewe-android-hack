package com.expedia.bookings.utils;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {
	private final Typeface mTypeface;

	public TypefaceSpan(Typeface typeface) {
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

		// Note: This flag is required for proper typeface rendering
		paint.setFlags(paint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}

}
