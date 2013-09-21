package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
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
 * TODO: Optimize onDraw() (reduce object creation, calculations)
 */
public class MonthView extends View {

	// Enable to add some helpful debugging drawing
	private static final boolean DEBUG_DRAW = false;

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .15f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	// The alpha of the highlight "shade" (between selected range)
	private static final int SELECTION_SHADE_ALPHA = 30;

	// The percentage of a cell that the selection should take up
	private static final float SELECTION_PERCENT = .8f;

	// TODO: Parameterize this eventually (for other locales)
	private static final int FIRST_DAY_OF_WEEK = DateTimeConstants.SUNDAY;

	private static final int ROWS = 6;
	private static final int COLS = 7;

	private GestureDetectorCompat mDetector;

	private DateSelectionChangedListener mListener;

	private YearMonth mDisplayYearMonth;

	private LocalDate[][] mDays = new LocalDate[ROWS][COLS];
	private Interval mDayInterval;

	private TextPaint mTextPaint;
	private TextPaint mTextInversePaint;
	private float mMaxTextSize;

	private Paint mSelectionPaint;
	private Paint mSelectionLinePaint;
	private Paint mSelectionAlphaPaint;

	// Current selections; only here for drawing.  CalendarPicker should hold the state.
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	// Variables that are cached for faster drawing
	private int mWidth;
	private int mHeight;
	private float[] mRowCenters = new float[ROWS];
	private float[] mColCenters = new float[COLS];
	private float mCellHeight;
	private float mCellWidth;
	private float mCellSelectionHeight;
	private float mCellSelectionWidth;
	private float mCircleRadius;
	private List<RectF> mHighlightRows = new ArrayList<RectF>();
	private int mHighlightRowsIndex;

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

		mSelectionLinePaint = new Paint(mSelectionPaint);
		mSelectionLinePaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
		mSelectionLinePaint.setStyle(Style.STROKE);

		mSelectionAlphaPaint = new Paint(mSelectionPaint);
	}

	public void setDateSelectionListener(DateSelectionChangedListener listener) {
		mListener = listener;
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setHighlightColor(int color) {
		mSelectionPaint.setColor(color);
		mSelectionLinePaint.setColor(color);
		mSelectionAlphaPaint.setColor(color);
		mSelectionAlphaPaint.setAlpha(SELECTION_SHADE_ALPHA);
	}

	public void setHighlightInverseColor(int color) {
		mTextInversePaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	public void setDateSelection(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			// Swap them so it always makes sense
			LocalDate tmpDate = startDate;
			startDate = endDate;
			endDate = tmpDate;
		}

		if ((startDate != null && !startDate.equals(mStartDate)) || (endDate != null && !endDate.equals(mEndDate))) {
			mStartDate = startDate;
			mEndDate = endDate;
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

		mDayInterval = new Interval(mDays[0][0].toDateTimeAtStartOfDay(),
				mDays[ROWS - 1][COLS - 1].toDateTimeAtStartOfDay());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) {
			// Pre-compute the center of each row/col
			mWidth = right - left;
			mHeight = bottom - top;
			mCellHeight = (float) mHeight / ROWS;
			mCellWidth = (float) mWidth / COLS;
			mCellSelectionHeight = mCellHeight * SELECTION_PERCENT;
			mCellSelectionWidth = mCellWidth * SELECTION_PERCENT;
			divideGridSize(mWidth, mColCenters);
			divideGridSize(mHeight, mRowCenters);

			mCircleRadius = Math.min(mCellSelectionHeight, mCellSelectionWidth) / 2;

			// Scale down the text size; I'm not too concerned about it being too wide, so
			// just use the TextPaint's height to determine if we're too large
			float cellMinSize = Math.min((float) mWidth / COLS, (float) mHeight / ROWS) * (1 - PADDING_PERCENT);
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
			canvas.drawCircle(centerX, centerY, mCircleRadius, mSelectionPaint);
		}

		// Draw end date (if selected and visible)
		int[] endCell = getCell(mEndDate);
		if (endCell != null) {
			float centerX = mColCenters[endCell[1]];
			float centerY = mRowCenters[endCell[0]];
			canvas.drawCircle(centerX, centerY, mCircleRadius, mSelectionPaint);
		}

		// Draw selection if there is a range selected and we're displaying cells
		// that have some selected days in it.
		//
		// This is optimized to draw row-by-row, instead of trying to draw cell-by-cell.
		// It does this by creating a series of RectFs that define where the selections
		// should be drawn, then collates/draws them all at once.
		if (mStartDate != null && mEndDate != null
				&& (mDayInterval.contains(mStartDate.toDateTimeAtStartOfDay())
				|| mDayInterval.contains(mEndDate.toDateTimeAtStartOfDay()))) {

			int startRow = startCell != null ? startCell[0] : 0;
			int endRow = endCell != null ? endCell[0] : COLS - 1;
			mHighlightRowsIndex = 0;

			// Special case: startRow == endRow
			RectF rect;
			float halfCellWidth = mCellWidth / 2;
			float halfCellSelectionHeight = mCellSelectionHeight / 2;
			if (startCell != null && endCell != null && startRow == endRow) {
				rect = getNextHighlightRect();
				rect.left = startCell[1] * mCellWidth + halfCellWidth;
				rect.right = endCell[1] * mCellWidth + halfCellWidth;
				rect.top = mRowCenters[startRow] - halfCellSelectionHeight;
				rect.bottom = mRowCenters[startRow] + halfCellSelectionHeight;
			}
			else {
				// Draw start date --> end of row
				if (startCell != null) {
					rect = getNextHighlightRect();
					rect.left = startCell[1] * mCellWidth + halfCellWidth;
					rect.right = COLS * mCellWidth + mCellWidth;
					rect.top = mRowCenters[startRow] - halfCellSelectionHeight;
					rect.bottom = mRowCenters[startRow] + halfCellSelectionHeight;
				}

				// Draw any fully-selected rows in the middle
				for (int rowNum = startCell != null ? startRow + 1 : startRow; rowNum < endRow; rowNum++) {
					rect = getNextHighlightRect();
					rect.left = 0;
					rect.right = COLS * mCellWidth + mCellWidth;
					rect.top = mRowCenters[rowNum] - halfCellSelectionHeight;
					rect.bottom = mRowCenters[rowNum] + halfCellSelectionHeight;
				}

				// Draw start of row --> end date
				if (endCell != null) {
					rect = getNextHighlightRect();
					rect.left = 0;
					rect.right = endCell[1] * mCellWidth + halfCellWidth;
					rect.top = mRowCenters[endRow] - halfCellSelectionHeight;
					rect.bottom = mRowCenters[endRow] + halfCellSelectionHeight;
				}
			}

			// Draw all the highlighted row backgrounds
			for (int index = 0; index < mHighlightRowsIndex; index++) {
				rect = mHighlightRows.get(index);
				canvas.drawRect(rect, mSelectionAlphaPaint);
			}

			// Draw all highlighted rows top/bottom lines
			// (Done separately from background for GPU optimization)
			float halfStrokeWidth = mSelectionLinePaint.getStrokeWidth() / 2.0f;
			for (int index = 0; index < mHighlightRowsIndex; index++) {
				rect = mHighlightRows.get(index);
				float top = rect.top + halfStrokeWidth;
				float bottom = rect.bottom - halfStrokeWidth;
				canvas.drawLine(rect.left, top, rect.right, top, mSelectionLinePaint);
				canvas.drawLine(rect.left, bottom, rect.right, bottom, mSelectionLinePaint);
			}
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
				if ((startCell != null && startCell[0] == week && startCell[1] == dayOfWeek)
						|| (endCell != null && endCell[0] == week && endCell[1] == dayOfWeek)) {
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

	private RectF getNextHighlightRect() {
		if (mHighlightRows.size() == mHighlightRowsIndex) {
			mHighlightRows.add(new RectF());
		}
		return mHighlightRows.get(mHighlightRowsIndex++);
	}

	//////////////////////////////////////////////////////////////////////////
	// Touch events

	private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

		private boolean mIsScrolling;
		private LocalDate mInitialDate;
		private LocalDate mAnchorDate;

		@Override
		public boolean onDown(MotionEvent e) {
			int[] cell = getCell(e);
			mInitialDate = mDays[cell[0]][cell[1]];
			mIsScrolling = false;
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int[] cell = getCell(e);
			LocalDate clickedDate = mDays[cell[0]][cell[1]];

			if (mStartDate == null) {
				// If no START, select start
				setDateSelection(clickedDate, null);
			}
			else if (mEndDate == null) {
				if (clickedDate.isBefore(mStartDate)) {
					// If clicked BEFORE start date, re-select start date
					setDateSelection(clickedDate, null);
				}
				else {
					// Else create RANGE
					setDateSelection(mStartDate, clickedDate);
				}
			}
			else if (!clickedDate.equals(mStartDate) && !clickedDate.equals(mEndDate)) {
				// If clicked is not START or END, reset
				setDateSelection(clickedDate, null);
			}

			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			int[] cell = getCell(e2);

			// You can scroll outside of the current view; ignore
			if (cell == null) {
				return false;
			}

			LocalDate scrolledDate = mDays[cell[0]][cell[1]];

			if (!mIsScrolling) {
				// If we haven't started a scroll yet, initialize anchors and what have you
				// Code is purposefully a bit wordy to make it easier to understand
				if (mStartDate != null && mEndDate != null) {
					if (mInitialDate.equals(mStartDate)) {
						// Move START, anchor END
						mAnchorDate = mEndDate;
					}
					else if (mInitialDate.equals(mEndDate)) {
						// Move END, anchor START
						mAnchorDate = mStartDate;
					}
					else {
						// Start a NEW drag
						mAnchorDate = null;
					}
				}
				else if (mStartDate != null) {
					if (mInitialDate.equals(mStartDate) || mInitialDate.isAfter(mStartDate)) {
						// New RANGE, anchor START
						mAnchorDate = mStartDate;
					}
					else if (mInitialDate.isBefore(mStartDate)) {
						// Start a NEW drag
						mAnchorDate = null;
					}
				}
				else {
					// Start a NEW drag
					mAnchorDate = null;
				}

				mIsScrolling = true;
			}

			if (mAnchorDate == null) {
				setDateSelection(scrolledDate, null);
			}
			else {
				setDateSelection(mAnchorDate, scrolledDate);
			}

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
		float x = e.getX();
		float y = e.getY();

		// Sanity check - if it's outside of the current view, don't use it
		if (x < 0 || y < 0 || x > mWidth || y > mHeight) {
			return null;
		}

		return new int[] {
			(int) Math.floor(y / mCellHeight),
			(int) Math.floor(x / mCellWidth)
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
