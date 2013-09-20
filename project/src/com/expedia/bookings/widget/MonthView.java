package com.expedia.bookings.widget;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.expedia.bookings.widget.CalendarPicker.DateSelectionChangedListener;

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
 * 
 * TODO: Limit invalidate() to just cells that need it
 */
public class MonthView extends View {

	// Enable to add some helpful debugging drawing
	private static final boolean DEBUG_DRAW = false;

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .15f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	// TODO: Parameterize this eventually (for other locales)
	private static final int FIRST_DAY_OF_WEEK = DateTimeConstants.SUNDAY;

	private static final int ROWS = 6;
	private static final int COLS = 7;

	private GestureDetectorCompat mDetector;

	private DateSelectionChangedListener mListener;

	private YearMonth mDisplayYearMonth;

	private LocalDate[][] mDays = new LocalDate[ROWS][COLS];

	private TextPaint mTextPaint;
	private TextPaint mTextInversePaint;
	private float mMaxTextSize;

	private Paint mSelectionPaint;

	// Current selections; only here for drawing.  CalendarPicker should hold the state.
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	// Variables that are cached for faster drawing
	private float[] mRowCenters = new float[ROWS];
	private float[] mColCenters = new float[COLS];
	private float mCellHeight;
	private float mCellWidth;

	public MonthView(Context context) {
		this(context, null);
	}

	public MonthView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mDetector = new GestureDetectorCompat(getContext(), mGestureListener);

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextAlign(Align.CENTER);

		mTextInversePaint = new TextPaint(mTextPaint);

		mSelectionPaint = new Paint();
		mSelectionPaint.setAntiAlias(true);
	}

	public void setDateSelectionListener(DateSelectionChangedListener listener) {
		mListener = listener;
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setHighlightColor(int color) {
		mSelectionPaint.setColor(color);
	}

	public void setHighlightInverseColor(int color) {
		mTextInversePaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	public void setStartDate(LocalDate startDate) {
		if (startDate != mStartDate) {
			mStartDate = startDate;
			notifyDateSelectionChanged();
			invalidate(); // TODO: Only invalidate parts that are needed
		}
	}

	private void notifyDateSelectionChanged() {
		mListener.onDateSelectionChanged(mStartDate, mEndDate);
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

		if (changed) {
			// Pre-compute the center of each row/col
			int width = right - left;
			int height = bottom - top;
			mCellHeight = (float) height / ROWS;
			mCellWidth = (float) width / COLS;
			divideGridSize(width, mColCenters);
			divideGridSize(height, mRowCenters);

			// Scale down the text size; I'm not too concerned about it being too wide, so
			// just use the TextPaint's height to determine if we're too large
			float cellMinSize = Math.min((float) width / COLS, (float) height / ROWS) * (1 - PADDING_PERCENT);
			mTextPaint.setTextSize(mMaxTextSize);
			while (cellMinSize < mTextPaint.ascent() - mTextPaint.descent()) {
				mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
			}

			// Make sure all other paints match size
			mTextInversePaint.setTextSize(mTextPaint.getTextSize());
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

		if (DEBUG_DRAW) {
			// Draw cell backgrounds alternating colors
			RectF drawRect = new RectF();
			Paint red = new Paint();
			red.setColor(Color.RED);
			Paint blue = new Paint();
			blue.setColor(Color.BLUE);
			for (int week = 0; week < ROWS; week++) {
				for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
					drawRect.left = dayOfWeek * mCellWidth;
					drawRect.right = drawRect.left + mCellWidth;
					drawRect.top = week * mCellHeight;
					drawRect.bottom = drawRect.top + mCellHeight;
					canvas.drawRect(drawRect, ((week + dayOfWeek) % 2 == 0) ? red : blue);
				}
			}
		}

		// Draw the start date (if selected and visible)
		int[] startCell = getCell(mStartDate);
		if (startCell != null) {
			float centerX = mColCenters[startCell[1]];
			float centerY = mRowCenters[startCell[0]];
			canvas.drawCircle(centerX, centerY, Math.min(mCellHeight, mCellWidth) / 2, mSelectionPaint);
		}

		// Draw each number
		float textHeight = mTextPaint.descent() - mTextPaint.ascent();
		float halfTextHeight = textHeight / 2;
		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				LocalDate date = mDays[week][dayOfWeek];
				float centerX = mColCenters[dayOfWeek];
				float centerY = mRowCenters[week];

				// Invert colors on selected dates with circle behind them
				TextPaint paint;
				if (startCell != null && startCell[0] == week && startCell[1] == dayOfWeek) {
					paint = mTextInversePaint;
				}
				else {
					paint = mTextPaint;
				}

				canvas.drawText(Integer.toString(date.getDayOfMonth()), centerX,
						centerY + halfTextHeight - mTextPaint.descent(), paint);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Touch events

	private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int[] cell = getCell(e);

			LocalDate clickedDate = mDays[cell[0]][cell[1]];

			if (clickedDate != mStartDate) {
				setStartDate(clickedDate);
			}

			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// Consume all events
			return true;
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = mDetector.onTouchEvent(event);
		if (!consumed) {
			consumed = super.onTouchEvent(event);
		}
		return consumed;
	}

	// Returns int[row][col] for a given motion event
	private int[] getCell(MotionEvent e) {
		return new int[] {
			(int) Math.floor(e.getY() / mCellHeight),
			(int) Math.floor(e.getX() / mCellWidth)
		};
	}

	// Returns int[row][col] for a given date (currently being displayed), or null
	// if the start date is not visible on the current calendar.
	//
	// TODO: Should we cache this at some point, or is it so fast as to be totally unnecessary?
	private int[] getCell(LocalDate date) {
		if (date == null) {
			return null;
		}

		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				if (mDays[week][dayOfWeek].equals(date)) {
					return new int[] {
						week,
						dayOfWeek
					};
				}
			}
		}

		return null;
	}

}
