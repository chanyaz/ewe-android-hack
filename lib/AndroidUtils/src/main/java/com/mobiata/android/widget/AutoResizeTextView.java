package com.mobiata.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.mobiata.android.Log;
import com.mobiata.android.R;

/**
 * Text view that resizes text to fit a specific number of lines.
 *
 * Defaults maxLines to 1 unless otherwise specified.
 *
 * Heavily modified from original, found at stackoverflow:
 * http://stackoverflow.com/questions/5033012/auto-scale-textview-text-to-fit-within-bounds/5535672#5535672
 *
 * @author Chase Colburn (original)
 * @since Apr 4, 2011
 */
public class AutoResizeTextView extends TextView {

	// Minimum text size for this text view
	public static final float MIN_TEXT_SIZE = 1;

	// Off screen canvas for text size rendering
	private static final Canvas sTextResizeCanvas = new Canvas();

	// Flag for text and/or size changes to force a resize
	private boolean mNeedsResize = false;

	// Temporary upper bounds on the starting text size
	private float mMaxTextSize = 0;

	// Lower bounds for text size
	private float mMinTextSize = MIN_TEXT_SIZE;

	// Text view line spacing multiplier
	private float mSpacingMult = 1.0f;

	// Text view additional line spacing
	private float mSpacingAdd = 0.0f;

	// Internal storage for max lines
	private int mMaxLines;

	// Default constructor override
	public AutoResizeTextView(Context context) {
		this(context, null);
	}

	// Default constructor when inflating from XML file
	public AutoResizeTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	// Default constructor override
	public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AutoResizeTextView);
		setMaxTextSize(attributes.getDimension(R.styleable.AutoResizeTextView_maxTextSize, getTextSize()));
		setMinTextSize(attributes.getDimension(R.styleable.AutoResizeTextView_minTextSize, MIN_TEXT_SIZE));
		attributes.recycle();
	}

	@Override
	public void setMaxLines(int maxlines) {
		super.setMaxLines(maxlines);
		mMaxLines = maxlines;
	}

	/**
	 * When text changes, set the force resize flag to true and reset the text size.
	 */
	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		mNeedsResize = true;
	}

	/**
	 * If the text view size changed, set the force resize flag to true
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			mNeedsResize = true;
		}
	}

	/**
	 * Override the set line spacing to update our internal reference values
	 */
	@Override
	public void setLineSpacing(float add, float mult) {
		super.setLineSpacing(add, mult);
		mSpacingMult = mult;
		mSpacingAdd = add;
	}

	/**
	 * Set the upper text size limit and invalidate the view
	 * @param maxTextSize
	 */
	public void setMaxTextSize(float maxTextSize) {
		mMaxTextSize = maxTextSize;
		requestLayout();
		invalidate();
	}

	/**
	 * Return upper text size limit
	 * @return
	 */
	public float getMaxTextSize() {
		return mMaxTextSize;
	}

	/**
	 * Set the lower text size limit and invalidate the view
	 * @param minTextSize
	 */
	public void setMinTextSize(float minTextSize) {
		mMinTextSize = minTextSize;
		requestLayout();
		invalidate();
	}

	/**
	 * Return lower text size limit
	 * @return
	 */
	public float getMinTextSize() {
		return mMinTextSize;
	}

	/**
	 * Resize text after measuring
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed || mNeedsResize) {
			resizeText(right - left - getCompoundPaddingLeft() - getCompoundPaddingRight());
		}

		super.onLayout(changed, left, top, right, bottom);
	}

	/**
	 * Resize the text size with specified width and height
	 */
	public void resizeText(int widthPx) {
		CharSequence text = getText();
		if (getTransformationMethod() != null) {
			text = getTransformationMethod().getTransformation(text, this);
		}
		// Do not resize if the view does not have dimensions or there is no text
		if (text == null || text.length() == 0 || widthPx <= 0) {
			return;
		}

		// Get the text view's paint object (as a copy, so we don't modify it)
		TextPaint textPaint = new TextPaint();
		textPaint.set(getPaint());

		// If there is a max text size set, use that; otherwise, base the max text size
		// on the current text size.
		float targetTextSize = mMaxTextSize > 0 ? mMaxTextSize : textPaint.getTextSize();

		// Default to a single line for display
		int maxLines = mMaxLines > 0 ? mMaxLines : 1;

		int lineCount = getTextLineCount(text, textPaint, widthPx, targetTextSize);
		while (lineCount > maxLines && targetTextSize > mMinTextSize) {
			targetTextSize -= 1;
			lineCount = getTextLineCount(text, textPaint, widthPx, targetTextSize);
		}

		Log.v("Resizing TextView " + this.toString() + " to text size of " + targetTextSize + " so it fits on "
				+ maxLines + " line(s) - " + text);

		// Some devices try to auto adjust line spacing, so force default line spacing
		// and invalidate the layout as a side effect
		setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
		setLineSpacing(mSpacingAdd, mSpacingMult);

		// Reset force resize flag
		mNeedsResize = false;
	}

	private int getTextLineCount(CharSequence source, TextPaint paint, int widthPx, float textSize) {
		// Update the text paint object
		paint.setTextSize(textSize);

		// Draw using a static layout
		StaticLayout layout = new StaticLayout(source, paint, widthPx, Alignment.ALIGN_NORMAL, mSpacingMult,
				mSpacingAdd, true);
		layout.draw(sTextResizeCanvas);

		return layout.getLineCount();
	}

	/*
	 * StaticLayout study notes, 12/16/2011 (dlew)
	 *
	 * Thoughts on some params:
	 *
	 * paint - This determines how the text is painted into the Canvas.  It's worth noting that *density* is actually
	 *         stored in TextPaints, so it can have a large effect on how text renders into the same width (in pixels).
	 *
	 *         Also worth knowing that the text size itself is scaled to the density.  So if you have a text size of
	 *         14sp, it's 11 on ldpi, 14 on mdpi, 21 on hdpi, and 28 on xhdpi.
	 *
	 * width - This is the width in pixels.
	 *
	 * alignment - Goes completely unused in the actual source code, so its value *does not matter*.
	 */
}
