package com.mobiata.android.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewUtils {

	private static final Canvas sTextResizeCanvas = new Canvas();

	/**
	 * Figures out a text size (in sp) for a specific string fitting in a particular width View.
	 *
	 * @param context the Context
	 * @param source the string that should fit inside the given width
	 * @param textPaint the TextPaint defining how the source is rendered
	 * @param widthDp the maximum width of the rendered string, in dp
	 * @param maxLines the maximum number of lines that the string should wrap onto
	 * @param maxTextSizeSp the maximum size of the text, in sp
	 * @param minTextSizeSp the minimum size of the text, in sp
	 * @return the text size, in sp
	 */
	public static float getTextSizeForMaxLines(Context context, CharSequence source, TextPaint textPaint,
			float widthDp, int maxLines, float maxTextSizeSp, float minTextSizeSp) {
		if (textPaint == null) {
			textPaint = new TextPaint();
		}
		else {
			// Make a copy of the TextPaint so that we don't inadvertently modify it
			TextPaint tmp = textPaint;
			textPaint = new TextPaint();
			textPaint.set(tmp);
		}

		// Ensure that the density of the TextPaint is set properly.  Makes all the rest of the math easier.
		float density = context.getResources().getDisplayMetrics().density;
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		textPaint.density = scaledDensity;

		// Scale the target min/max/width size by the density
		float maxTextSizePx = maxTextSizeSp * scaledDensity;
		float minTextSizePx = minTextSizeSp * scaledDensity;
		int widthPx = (int) Math.floor(widthDp * density);

		for (float textSizePx = maxTextSizePx; textSizePx > minTextSizePx; textSizePx -= .5) {
			textPaint.setTextSize(textSizePx);
			StaticLayout layout = new StaticLayout(source, textPaint, widthPx, Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
			layout.draw(sTextResizeCanvas);
			if (layout.getLineCount() <= maxLines) {
				return textSizePx / scaledDensity;
			}
		}

		return minTextSizeSp;
	}

	/**
	 * DEPRECATED: Use tv.setAllCaps(boolean) instead.
	 */
	@Deprecated
	public static void setAllCaps(ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);
			if (child instanceof TextView) {
				((TextView) child).setTransformationMethod(sAllCapsTransformationMethod);
			}
		}
	}

	/**
	 * DEPRECATED: Use tv.setAllCaps(boolean) instead.
	 */
	@Deprecated
	public static void setAllCaps(TextView tv) {
		tv.setTransformationMethod(sAllCapsTransformationMethod);
	}

	/**
	 * DEPRECATED: This isn't necessary anymore.
	 */
	@Deprecated
	public static CharSequence toUpper(Context context, CharSequence seq) {
		return seq.toString().toUpperCase(context.getResources().getConfiguration().locale);
	}

	private static TransformationMethod sAllCapsTransformationMethod = new TransformationMethod() {
		@Override
		public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction,
				Rect previouslyFocusedRect) {
			// nothing needs to be done
		}

		@Override
		public CharSequence getTransformation(CharSequence source, View view) {
			return toUpper(view.getContext(), source);
		}
	};
}
