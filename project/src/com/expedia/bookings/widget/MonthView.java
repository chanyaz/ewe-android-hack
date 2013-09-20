package com.expedia.bookings.widget;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Displays the days of the month in a grid.  It is 7x6; seven for
 * the days of the week, six because that's the minimum rows needed
 * for displaying every possible month.
 * 
 * This View is part of CalendarPicker and should not be used outside
 * of it.  It does not save its own state; it depends on CalendarPicker
 * for that.
 * 
 * This class should be modified in conjunction with DaysOfWeekView, 
 * which should measure itself in a similar manner.
 *
 * This View assumes it has a well-defined width and height (e.g.,
 * match_parent or defined pixel amount).
 */
public class MonthView extends View {

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .15f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	// TODO: Parameterize this eventually (for other locales)
	private static final int FIRST_DAY_OF_WEEK = DateTimeConstants.SUNDAY;

	private static final int ROWS = 6;
	private static final int COLS = 7;

	private YearMonth mDisplayYearMonth;

	private LocalDate[][] mDays = new LocalDate[ROWS][COLS];

	private TextPaint mTextPaint;
	private float mMaxTextSize;

	// Cached for faster drawing; these are the centers of each grid tile
	private float[] mRowCenters = new float[ROWS];
	private float[] mColCenters = new float[COLS];

	public MonthView(Context context) {
		this(context, null);
	}

	public MonthView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextAlign(Align.CENTER);
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	// We depend on CalendarPicker calling this before we render
	public void setDisplayYearMonth(YearMonth yearMonth) {
		if (yearMonth != mDisplayYearMonth) {
			mDisplayYearMonth = yearMonth;
			precomputeGrid();
			invalidate();
		}
	}

	private void precomputeGrid() {
		LocalDate firstDayOfGrid = mDisplayYearMonth.toLocalDate(1);
		while (firstDayOfGrid.getDayOfWeek() != FIRST_DAY_OF_WEEK) {
			firstDayOfGrid = firstDayOfGrid.minusDays(1);
		}

		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				mDays[week][dayOfWeek] = firstDayOfGrid.plusDays(week * COLS + dayOfWeek);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// Pre-compute the center of each row/col
		int width = right - left;
		int height = bottom - top;
		divideGridSize(width, mColCenters);
		divideGridSize(height, mRowCenters);

		// Scale down the text size; I'm not too concerned about it being too wide, so
		// just use the TextPaint's height to determine if we're too large
		float cellMinSize = Math.min((float) width / COLS, (float) height / ROWS) * (1 - PADDING_PERCENT);
		mTextPaint.setTextSize(mMaxTextSize);
		while (cellMinSize < mTextPaint.ascent() - mTextPaint.descent()) {
			mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
		}
	}

	private void divideGridSize(int size, float[] result) {
		int len = result.length;
		float gridSize = (float) size / len;
		float halfGridSize = gridSize / 2;
		for (int a = 0; a < len; a++) {
			result[a] = (gridSize * a) + halfGridSize;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw each number
		float textHeight = mTextPaint.descent() - mTextPaint.ascent();
		float halfTextHeight = textHeight / 2;
		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				LocalDate date = mDays[week][dayOfWeek];
				float centerX = mColCenters[dayOfWeek];
				float centerY = mRowCenters[week];
				canvas.drawText(Integer.toString(date.getDayOfMonth()), centerX, centerY + halfTextHeight, mTextPaint);
			}
		}
	}

}
