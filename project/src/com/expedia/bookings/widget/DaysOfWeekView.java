package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.utils.JodaUtils;

/**
 * Shows the days of the week in a row: MON TUE WED etc.
 * 
 * This View is part of CalendarPicker and should not be used outside
 * of it.  It does not save its own state; it depends on CalendarPicker
 * for that.
 * 
 * It splits itself into seven equal parts, then sizes the text based
 * on the largest size where all text fits (with reasonable padding
 * between each day).
 * 
 * This class should be modified in conjunction with MonthView, which
 * should measure itself in a similar manner.
 *
 * This View assumes it has a width of match_parent and a height of
 * wrap_content (for ease of measurement).  It scales everything else.
 */
public class DaysOfWeekView extends View {

	private static final int NUM_DAYS = 7;

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .10f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	private String[] mDaysOfWeek;

	private TextPaint mTextPaint;
	private float mMaxTextSize;

	// Cached, used for measuring
	private Rect mTextBounds;

	public DaysOfWeekView(Context context) {
		this(context, null);
	}

	public DaysOfWeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DaysOfWeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextAlign(Align.CENTER);
		mMaxTextSize = mTextPaint.getTextSize();

		mTextBounds = new Rect();

		// Pre-load the days of the week in the current locale
		mDaysOfWeek = new String[NUM_DAYS];
		LocalDate firstDayOfWeek = LocalDate.now().withDayOfWeek(JodaUtils.getFirstDayOfWeek());
		for (int a = 0; a < NUM_DAYS; a++) {
			mDaysOfWeek[a] = firstDayOfWeek.plusDays(a).dayOfWeek().getAsShortText().toUpperCase();
		}
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Do a normal measurement first
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Keep measuring with smaller textSizes until we're small enough to fit
		int measuredWidth = getMeasuredWidth();
		float colWidth = (float) measuredWidth / NUM_DAYS;
		float maxTextWidth = colWidth * (1 - PADDING_PERCENT);

		mTextPaint.setTextSize(mMaxTextSize);
		boolean tooBig;
		do {
			tooBig = false;
			for (String dayOfWeek : mDaysOfWeek) {
				mTextBounds.setEmpty();
				mTextPaint.getTextBounds(dayOfWeek, 0, dayOfWeek.length(), mTextBounds);

				if (mTextBounds.width() > maxTextWidth) {
					tooBig = true;
				}
			}

			if (tooBig) {
				mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
			}
		}
		while (tooBig);

		// Use text paint's total height to determine measured height
		int measuredHeight = (int) Math.ceil(mTextPaint.descent() - mTextPaint.ascent());
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float colWidth = (float) getWidth() / NUM_DAYS;
		float halfColWidth = colWidth / 2.0f;
		float y = getHeight() - mTextPaint.descent();

		for (int a = 0; a < NUM_DAYS; a++) {
			float centerOfCol = (colWidth * a) + halfColWidth;
			canvas.drawText(mDaysOfWeek[a], centerOfCol, y, mTextPaint);
		}
	}

}
