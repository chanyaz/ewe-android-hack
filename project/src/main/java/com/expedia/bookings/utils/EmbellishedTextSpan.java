package com.expedia.bookings.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

/**
 * This class represents a MetricAffectingSpan (actually, a ReplacementSpan) that embellishes some
 * text by allowing a prefix or suffix around the characters it contains. This prefix and/or suffix
 * will show up in a TextView, but will not appear in that TextView's toString().
 *
 * It's written to apply formatting to credit card and phone number EditText fields, and used with
 * NumberMaskEditText.
 *
 * Created by dmelton on 3/21/14.
 */
public class EmbellishedTextSpan extends ReplacementSpan {

	final String mTextBefore;
	final String mTextAfter;

	public EmbellishedTextSpan(String before, String after) {
		mTextBefore = before;
		mTextAfter = after;
	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
		int size = (int) paint.measureText(getEmbellishedText(text, start, end));
		return size;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
		String embellished = getEmbellishedText(text, start, end);
		canvas.drawText(embellished, 0, embellished.length(), x, y, paint);
	}

	private String getEmbellishedText(CharSequence text, int start, int end) {
		StringBuilder builder = new StringBuilder();
		if (!TextUtils.isEmpty(mTextBefore)) {
			builder.append(mTextBefore);
		}
		builder.append(text.subSequence(start, end));
		if (!TextUtils.isEmpty(mTextAfter)) {
			builder.append(mTextAfter);
		}
		return builder.toString();
	}

}
