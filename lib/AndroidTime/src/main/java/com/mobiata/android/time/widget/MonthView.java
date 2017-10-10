package com.mobiata.android.time.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.mobiata.android.Log;
import com.mobiata.android.time.R;
import com.mobiata.android.time.util.JodaUtils;
import com.mobiata.android.time.widget.CalendarPicker.CalendarState;
import com.squareup.phrase.Phrase;

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
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MonthView extends View {

	// Enable to add some helpful debugging drawing
	private static final boolean DEBUG_DRAW = false;

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .5f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	// The alpha of the highlight "shade" (between selected range)
	private static final int SELECTION_SHADE_ALPHA = 30;

	// The percentage of a cell that the selection should take up
	private static final float SELECTION_PERCENT = .8f;

	private static final int ROWS = 6;
	private static final int COLS = 7;

	private CalendarState mState;

	private LocalDate mFirstDayOfGrid;

	// Paints
	private float mMaxTextSize;
	private TextPaint mTextPaint;
	private TextPaint mTextSecondaryPaint;
	private TextPaint mTextInversePaint;
	private TextPaint mTextTodayPaint;
	private TextPaint mInvalidDayPaint;
	private TextPaint mTextEqualDatesPaint;
	private Paint mSelectionDayStrokePaint;
	private Paint mSelectionWeekStrokePaint;
	private Paint mSelectionWeekFillPaint;
	private Paint mSelectionDayFillPaint;

	// Touch events
	private GestureDetectorCompat mDetector;
	private LocalDate[][] mDays = new LocalDate[ROWS][COLS];

	// Animation variables
	private float mTranslationWeeks;

	// Accessibility
	private MonthTouchHelper mTouchHelper;

	// Variables that are cached for faster drawing
	private int mWidth;
	private int mHeight;
	private float[] mColCenters = new float[COLS];
	private float[] mRowCenters = new float[ROWS + 1];
	private float mCellHeight;
	private float mCellWidth;
	private float mCircleRadius;
	private List<RectF> mHighlightRows = new ArrayList<RectF>();
	private int mHighlightRowsIndex;
	private float mTextOffset;
	private int[] mStartCell = new int[2];
	private int[] mEndCell = new int[2];

	// Don't keep re-calculating days; only re-calculate when translation changes
	private int mLastWeekTranslationFloor = Integer.MAX_VALUE;
	private LocalDate[][] mDaysVisible = new LocalDate[ROWS + 1][COLS];

	// Profiling
	private static final int PROFILE_DRAW_STEP = 20;
	private long mTotalDrawTime = 0;
	private int mDrawTimes = 0;
	private boolean mSelectAllDates = false;

	private float startCenterX = 0;
	private float startCenterY = 0;
	private float endCenterX = 0;
	private float endCenterY = 0;

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

		mTextTodayPaint = new TextPaint(mTextPaint);

		mTextSecondaryPaint = new TextPaint(mTextPaint);

		mInvalidDayPaint = new TextPaint(mTextPaint);

		mTextEqualDatesPaint = new TextPaint(mTextPaint);

		mSelectionDayStrokePaint = new Paint();
		mSelectionDayStrokePaint.setAntiAlias(true);
		mSelectionDayStrokePaint.setStyle(Style.STROKE);
		mSelectionDayStrokePaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);

		mSelectionWeekStrokePaint = new Paint(mSelectionDayStrokePaint);
		mSelectionWeekStrokePaint.setStrokeCap(Cap.ROUND);

		mSelectionWeekFillPaint = new Paint(mSelectionDayStrokePaint);
		mSelectionWeekFillPaint.setStyle(Style.FILL);

		mSelectionDayFillPaint = new Paint();
		mSelectionDayFillPaint.setAntiAlias(true);
		mSelectionDayFillPaint.setStyle(Style.FILL);

		// Accessibility
		mTouchHelper = new MonthTouchHelper(this);
		ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
	}

	public void setDaysTypeface(Typeface typeface) {
		mTextPaint.setTypeface(typeface);
		mTextInversePaint.setTypeface(typeface);
		mTextTodayPaint.setTypeface(typeface);
		mTextSecondaryPaint.setTypeface(typeface);
		mInvalidDayPaint.setTypeface(typeface);
		mTextEqualDatesPaint.setTypeface(typeface);
	}

	public void setTodayTypeface(Typeface typeface) {
		mTextTodayPaint.setTypeface(typeface);
	}

	public void notifyDisplayYearMonthChanged() {
		mTranslationWeeks = 0;
		mLastWeekTranslationFloor = Integer.MAX_VALUE;
		precomputeGrid();
		invalidate();
	}

	public void notifySelectedDatesChanged() {
		// TODO: Only invalidate the cells of the dates that have changed?
		invalidate();
	}

	private void precomputeGrid() {
		mFirstDayOfGrid = mState.getDisplayYearMonth().toLocalDate(1);
		while (mFirstDayOfGrid.getDayOfWeek() != JodaUtils.getFirstDayOfWeek()) {
			mFirstDayOfGrid = mFirstDayOfGrid.minusDays(1);
		}

		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				mDays[week][dayOfWeek] = mFirstDayOfGrid.plusDays(week * COLS + dayOfWeek);
			}
		}
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

			float halfCellWidth = mCellWidth / 2;
			for (int a = 0; a < COLS; a++) {
				mColCenters[a] = (mCellWidth * a) + halfCellWidth;
			}

			mCircleRadius = Math.min(mCellHeight * SELECTION_PERCENT, mCellWidth * SELECTION_PERCENT) / 2;

			// Scale down the text size; I'm not too concerned about it being too wide, so
			// just use the TextPaint's height to determine if we're too large
			float cellMinSize = Math.min(mCellWidth, mCellHeight) * (1 - PADDING_PERCENT);
			mTextPaint.setTextSize(mMaxTextSize);
			while (cellMinSize < mTextPaint.descent() - mTextPaint.ascent()) {
				mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
			}

			// Make sure all other paints match size
			mTextSecondaryPaint.setTextSize(mTextPaint.getTextSize());
			mTextInversePaint.setTextSize(mTextPaint.getTextSize());
			mTextTodayPaint.setTextSize(mTextPaint.getTextSize());
			mInvalidDayPaint.setTextSize(mTextPaint.getTextSize());
			mTextEqualDatesPaint.setTextSize(mTextPaint.getTextSize());

			// Some cached vars for drawing
			float textHeight = mTextPaint.descent() - mTextPaint.ascent();
			float halfTextHeight = textHeight / 2;
			mTextOffset = halfTextHeight - mTextPaint.descent();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		long start = System.nanoTime();
		boolean datesAreEqual = false;
		LocalDate startDate = mState.getStartDate();
		LocalDate endDate = mState.getEndDate();

		// Draw from weekShiftFloor --> weekShiftFloor + ROWS
		int weekShiftFloor = (int) Math.floor(mTranslationWeeks);
		float weekShiftRemainder = mTranslationWeeks - weekShiftFloor;
		int numRowsToDraw = mTranslationWeeks == 0 ? ROWS : ROWS + 1;

		// Pre-calculate the center of each row, since we re-use it a lot
		float heightOffset = (mCellHeight / 2) - (mCellHeight * weekShiftRemainder);
		for (int week = 0; week < numRowsToDraw; week++) {
			mRowCenters[week] = heightOffset + (mCellHeight * week);
		}

		if (DEBUG_DRAW) {
			// Draw cell backgrounds alternating colors; may jump (don't care to fix really)
			RectF drawRect = new RectF();
			Paint first = new Paint();
			Paint second = new Paint();
			first.setColor(Color.RED);
			second.setColor(Color.BLUE);
			for (int week = 0; week < numRowsToDraw; week++) {
				for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
					drawRect.left = dayOfWeek * mCellWidth;
					drawRect.right = drawRect.left + mCellWidth;
					drawRect.top = (mCellHeight * week) - (mCellHeight * weekShiftRemainder);
					drawRect.bottom = drawRect.top + mCellHeight;
					canvas.drawRect(drawRect, ((week + weekShiftFloor + dayOfWeek) % 2 == 0) ? first : second);
				}
			}
		}

		// Re-calculate all visible days (so we don't have to re-calculate all days visible)
		if (mLastWeekTranslationFloor != weekShiftFloor) {
			for (int week = 0; week < ROWS + 1; week++) {
				for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
					mDaysVisible[week][dayOfWeek] = mFirstDayOfGrid.plusWeeks(week + weekShiftFloor)
						.plusDays(dayOfWeek);
				}
			}

			mLastWeekTranslationFloor = weekShiftFloor;
		}

		// Only try to draw selected days if there's any chance we could find them
		LocalDate firstVisibleDate = mDaysVisible[0][0];
		LocalDate lastVisibleDate = mDaysVisible[ROWS - 1][COLS - 1];

		startCenterX = 0;
		startCenterY = 0;
		endCenterX = 0;
		endCenterY = 0;

		int[] startCell = null;
		int[] endCell = null;
		if (startDate != null && !startDate.isAfter(lastVisibleDate)
			&& (endDate == null || !endDate.isBefore(firstVisibleDate))) {

			if (endDate != null && startDate.isEqual(endDate)) {
				datesAreEqual = true;
			}

			// Draw start/end date circles (if selected and visible)
			for (int week = 0; week < numRowsToDraw; week++) {
				for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
					LocalDate date = mDaysVisible[week][dayOfWeek];

					boolean equalsStartDate = startDate != null && date.equals(startDate);
					boolean equalsEndDate = endDate != null && date.equals(endDate);

					if (equalsStartDate) {
						startCenterX = mColCenters[dayOfWeek];
						startCenterY = mRowCenters[week];
						startCell = mStartCell;
						startCell[0] = week;
						startCell[1] = dayOfWeek;
					}
					if (equalsEndDate) {
						endCenterX = mColCenters[dayOfWeek];
						endCenterY = mRowCenters[week];
						endCell = mEndCell;
						endCell[0] = week;
						endCell[1] = dayOfWeek;
					}

				}
			}

			//Check do we have to select all dates as current dates lies between start date and end date
			mSelectAllDates = false;
			if (startDate != null && endDate != null && startDate.isBefore(firstVisibleDate) && endDate
				.isAfter(lastVisibleDate)) {
				startCell = mStartCell;
				startCell[0] = 0;
				startCell[1] = 0;
				endCell = mEndCell;
				endCell[0] = COLS - 1;
				endCell[1] = COLS - 1;
				mSelectAllDates = true;
			}

			// Draw selection if there is a range selected and we're displaying cells
			// that have some selected days in it.
			//
			// This is optimized to draw row-by-row, instead of trying to draw cell-by-cell.
			// It does this by creating a series of RectFs that define where the selections
			// should be drawn, then collates/draws them all at once.
			if (startDate != null && endDate != null && (startCell != null || endCell != null)) {
				int startRow = startCell != null ? startCell[0] : 0;
				int endRow = endCell != null ? endCell[0] : numRowsToDraw;
				mHighlightRowsIndex = 0;

				// Special case: startRow == endRow
				RectF rect;
				float halfCellWidth = mCellWidth / 2;
				if (startCell != null && endCell != null && startRow == endRow) {
					rect = getNextHighlightRect();
					rect.left = startCell[1] * mCellWidth + halfCellWidth;
					rect.right = endCell[1] * mCellWidth + halfCellWidth;
					rect.top = mRowCenters[startRow] - mCircleRadius;
					rect.bottom = mRowCenters[startRow] + mCircleRadius;
				}
				else {
					// Draw start date --> end of row
					if (startCell != null) {
						rect = getNextHighlightRect();
						if (mSelectAllDates && startDate.isBefore(firstVisibleDate)) {
							rect.left = 0;
						}
						else {
							rect.left = startCell[1] * mCellWidth + halfCellWidth;
						}
						rect.right = COLS * mCellWidth + mCellWidth;
						rect.top = mRowCenters[startRow] - mCircleRadius;
						rect.bottom = mRowCenters[startRow] + mCircleRadius;
					}

					// Draw any fully-selected rows in the middle
					for (int rowNum = startCell != null ? startRow + 1 : startRow; rowNum < endRow; rowNum++) {
						rect = getNextHighlightRect();
						rect.left = 0;
						rect.right = COLS * mCellWidth + mCellWidth;
						rect.top = mRowCenters[rowNum] - mCircleRadius;
						rect.bottom = mRowCenters[rowNum] + mCircleRadius;
					}

					// Draw start of row --> end date
					if (endCell != null) {
						rect = getNextHighlightRect();
						rect.left = 0;
						if (mSelectAllDates && endDate.isAfter(lastVisibleDate)) {
							rect.right = COLS * mCellWidth + mCellWidth;
						}
						else {
							rect.right = endCell[1] * mCellWidth + halfCellWidth;
						}
						rect.top = mRowCenters[endRow] - mCircleRadius;
						rect.bottom = mRowCenters[endRow] + mCircleRadius;
					}
				}

				// Draw all the highlighted row backgrounds
				for (int index = 0; index < mHighlightRowsIndex; index++) {
					rect = mHighlightRows.get(index);
					canvas.drawRect(rect, mSelectionWeekFillPaint);
				}

				// Draw all highlighted rows top/bottom lines
				// (Done separately from background for GPU optimization)
				float halfStrokeWidth = mSelectionWeekStrokePaint.getStrokeWidth() / 2.0f;
				for (int index = 0; index < mHighlightRowsIndex; index++) {
					rect = mHighlightRows.get(index);
					float top = rect.top + halfStrokeWidth;
					float bottom = rect.bottom - halfStrokeWidth;
					canvas.drawLine(rect.left, top, rect.right, top, mSelectionWeekStrokePaint);
					canvas.drawLine(rect.left, bottom, rect.right, bottom, mSelectionWeekStrokePaint);
				}
			}

			if (startCenterX != 0 && startCenterY != 0) {
				canvas.drawCircle(startCenterX, startCenterY, mCircleRadius, mSelectionDayFillPaint);
				canvas.drawCircle(startCenterX, startCenterY, mCircleRadius, mSelectionDayStrokePaint);
			}
			if (endCenterX != 0 && endCenterY != 0) {
				canvas.drawCircle(endCenterX, endCenterY, mCircleRadius, mSelectionDayFillPaint);
				canvas.drawCircle(endCenterX, endCenterY, mCircleRadius, mSelectionDayStrokePaint);
			}

			// Start and end dates are the same, fill with an inner circle
			if (datesAreEqual && !mState.isSingleDateMode()) {
				mSelectionDayStrokePaint.setStyle(Style.FILL);
				canvas.drawCircle(endCenterX, endCenterY, mCircleRadius - mSelectionDayStrokePaint.getStrokeWidth() * 2, mSelectionDayStrokePaint);
				mSelectionDayStrokePaint.setStyle(Style.STROKE);
			}

		}

		// Draw each number
		LocalDate today = LocalDate.now();
		Interval monthInterval = mState.getDisplayYearMonth().toInterval();
		LocalDate firstDayOfMonth = monthInterval.getStart().toLocalDate();
		LocalDate lastDayOfMonth = monthInterval.getEnd().toLocalDate().minusDays(1); // monthInterval.getEnd() actually returns the first of the next month
		boolean lookForToday = monthInterval.contains(today.toDateTimeAtStartOfDay());
		TextPaint paint;
		for (int week = 0; week < numRowsToDraw; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				LocalDate date = mDaysVisible[week][dayOfWeek];

				float centerX = mColCenters[dayOfWeek];
				float centerY = mRowCenters[week];

				if (datesAreEqual && (date.isEqual(startDate) || date.isEqual(endDate)) && !mState.isSingleDateMode()) {
					paint = mTextEqualDatesPaint;
				}
				else if (!mSelectAllDates && ((startCell != null && startCell[0] == week && startCell[1] == dayOfWeek)
					|| (endCell != null && endCell[0] == week && endCell[1] == dayOfWeek))) {
					// Invert colors on selected dates with circle behind them
					paint = mTextInversePaint;
				}
				else if (lookForToday && date.equals(today)) {
					paint = mTextTodayPaint;
				}
				else if (!mState.canSelectDate(date)) {
					paint = mInvalidDayPaint;
				}
				else if (date.isBefore(firstDayOfMonth) || date.isAfter(lastDayOfMonth)) {
					paint = mTextSecondaryPaint;
				}
				else {
					paint = mTextPaint;
				}

				canvas.drawText(Integer.toString(date.getDayOfMonth()), centerX, centerY + mTextOffset, paint);

			}
		}

		mTotalDrawTime += (System.nanoTime() - start);
		mDrawTimes++;
		if (mDrawTimes % PROFILE_DRAW_STEP == 0) {
			Log.d("MonthView.onDraw() avg draw time: " + (mTotalDrawTime / (mDrawTimes * 1000)) + " ns");
		}
	}

	private RectF getNextHighlightRect() {
		if (mHighlightRows.size() == mHighlightRowsIndex) {
			mHighlightRows.add(new RectF());
		}
		return mHighlightRows.get(mHighlightRowsIndex++);
	}

	//////////////////////////////////////////////////////////////////////////
	// Configuration

	public void setCalendarState(CalendarState state) {
		mState = state;
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setTextSecondaryColor(int color) {
		mTextSecondaryPaint.setColor(color);
	}

	public void setTextEqualDatesColor(int color) {
		mTextEqualDatesPaint.setColor(color);
	}

	/**
	 * Sets the color of the selected days and week highlight color
	 * Should be used alone since it overwrites the colors
	 * set from setSelectionDayColor() & setSelectionWeekHighlightColor()
	 * @param color
	 */
	public void setHighlightColor(int color) {
		// Default implementation, all these colors are the same
		mSelectionDayStrokePaint.setColor(color);
		setSelectionDayColor(color);
		setSelectionWeekHighlightColor(color);
	}

	/**
	 * Sets the fill color of the selected days and stroke color of the highlighted week
	 * @param color
	 */
	public void setSelectionDayColor(int color) {
		mSelectionWeekStrokePaint.setColor(color);
		mSelectionDayFillPaint.setColor(color);
	}

	/**
	 * Sets the fill color of the highlighted week
	 * @param color
	 */
	public void setSelectionWeekHighlightColor(int color) {
		mSelectionWeekFillPaint.setColor(color);
		mSelectionWeekFillPaint.setAlpha(SELECTION_SHADE_ALPHA);
	}

	public void setHighlightInverseColor(int color) {
		mTextInversePaint.setColor(color);
		setTextEqualDatesColor(color);
	}

	public void setTodayColor(int color) {
		mTextTodayPaint.setColor(color);
	}

	public void setInvalidDayColor(int color) {
		mInvalidDayPaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	public static final Property<MonthView, Float> TRANSLATE_WEEKS = new Property<MonthView, Float>(Float.class, "translationWeeks") {

		@Override
		public void set(MonthView monthView, Float value) {
			monthView.setTranslationWeeks(value);
		}

		@Override
		public Float get(MonthView monthView) {
			return monthView.getTranslationWeeks();
		}

	};

	public void setTranslationWeeks(float translationWeeks) {
		mTranslationWeeks = translationWeeks;
		invalidate();
	}

	public float getTranslationWeeks() {
		return mTranslationWeeks;
	}

	//////////////////////////////////////////////////////////////////////////
	// Touch events
	//
	// Note that all of this is based off of a non-animated MonthView;
	// if we want these to work when the MonthView is animating it will
	// require some additional work.

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = mDetector.onTouchEvent(event);
		if (!consumed) {
			consumed = super.onTouchEvent(event);
		}
		return consumed;
	}

	private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

		private boolean mIsScrolling;
		private LocalDate mInitialDate;
		private LocalDate mAnchorDate;

		@Override
		public boolean onDown(MotionEvent e) {
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			playSoundEffect(SoundEffectConstants.CLICK);

			int[] cell = getCell(e);
			mInitialDate = mDays[cell[0]][cell[1]];
			mIsScrolling = false;
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int[] cell = getCell(e);

			if (cell == null) {
				return false;
			}

			return onDateClicked(cell[0], cell[1]);
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
				// Don't start a "scroll" until the user has rolled over a valid date once
				if (!mState.canSelectDate(scrolledDate)) {
					return false;
				}

				// If we haven't started a scroll yet, initialize anchors and what have you
				// Code is purposefully a bit wordy to make it easier to understand
				LocalDate startDate = mState.getStartDate();
				LocalDate endDate = mState.getEndDate();
				if (startDate != null && endDate != null) {
					if (mInitialDate.equals(startDate)) {
						// Move START, anchor END
						mAnchorDate = endDate;
					}
					else if (mInitialDate.equals(endDate)) {
						// Move END, anchor START
						mAnchorDate = startDate;
					}
					else {
						// Start a NEW drag
						mAnchorDate = null;
					}
				}
				else if (startDate != null) {
					if (mInitialDate.isAfter(startDate)) {
						// New RANGE, anchor START
						mAnchorDate = startDate;
					}
					else {
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

			// You can technically scroll outside the valid selectable range boudns;
			// we let the CalendarState verify incorrect input, we don't it here.
			if (mAnchorDate == null) {
				mState.setSelectedDates(scrolledDate, null);

				// If we're not in single date mode, set this first date as the start, and begin
				// a drag with the end date.
				if (!mState.isSingleDateMode()) {
					mAnchorDate = scrolledDate;
				}
			}
			else {
				// If END is before START, swap them so it always makes sense
				if (mAnchorDate.isAfter(scrolledDate)) {
					mState.setSelectedDates(scrolledDate, mAnchorDate);
				}
				else {
					mState.setSelectedDates(mAnchorDate, scrolledDate);
				}
			}

			return true;
		}

	};

	private boolean onDateClicked(int row, int col) {
		LocalDate clickedDate = mDays[row][col];

		// If the user clicked on an illegal date, don't even try to use it
		if (!mState.canSelectDate(clickedDate)) {
			return false;
		}

		LocalDate startDate = mState.getStartDate();
		LocalDate endDate = mState.getEndDate();
		if (startDate == null || mState.isSingleDateMode()) {
			// If no START, select start
			mState.setSelectedDates(clickedDate, null);
		}
		else if (endDate == null) {
			if (clickedDate.isBefore(startDate)) {
				// If clicked BEFORE start date, re-select start date
				mState.setSelectedDates(clickedDate, null);
			}
			else {
				// Else create RANGE
				mState.setSelectedDates(startDate, clickedDate);
			}
		}
		else if (!clickedDate.equals(startDate) && !clickedDate.equals(endDate)) {
			// If clicked is not START or END, reset
			mState.setSelectedDates(clickedDate, null);
		}

		// Invalidate this cell so it gets re-read
		int virtualViewId = mTouchHelper.cellToVirtualViewId(row, col);
		mTouchHelper.invalidateVirtualView(virtualViewId);

		// Send accessibility event for this action
		mTouchHelper.sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);

		return true;
	}

	// Returns int[row][col] for a given motion event
	private int[] getCell(MotionEvent e) {
		return getCell(e.getX(), e.getY());
	}

	private int[] getCell(float x, float y) {
		// Sanity check - if it's outside of the current view, don't use it
		if (x < 0 || y < 0 || x > mWidth || y > mHeight) {
			return null;
		}

		return new int[] {
			(int) Math.min(Math.floor(y / mCellHeight), ROWS - 1),
			(int) Math.min(Math.floor(x / mCellWidth), COLS - 1),
		};
	}

	//////////////////////////////////////////////////////////////////////////
	// Accessibility

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean dispatchHoverEvent(MotionEvent event) {
		// Always attempt to dispatch hover events to accessibility first.
		if (mTouchHelper.dispatchHoverEvent(event)) {
			return true;
		}

		return super.dispatchHoverEvent(event);
	}

	private final class MonthTouchHelper extends ExploreByTouchHelper {

		public MonthTouchHelper(View forView) {
			super(forView);
		}

		private int cellToVirtualViewId(int row, int col) {
			return row * COLS + col;
		}

		private int[] virtualViewIdToCell(int virtualViewId) {
			return new int[] {
				virtualViewId / COLS,
				virtualViewId % COLS,
			};
		}

		private String getDescriptionForVirtualViewId(int virtualViewId) {
			int[] cell = virtualViewIdToCell(virtualViewId);
			LocalDate date = mDays[cell[0]][cell[1]];
			return getDescriptionForDate(date);
		}

		private String getDescriptionForDate(LocalDate date) {
			String dateStr = JodaUtils.formatLocalDate(getContext(), date, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);

			LocalDate startDate = mState.getStartDate();
			LocalDate endDate = mState.getEndDate();

			// currentLocalDateStr used for comparison logic
			LocalDate currentLocalDate = LocalDate.now();
			String currentLocalDateStr = JodaUtils.formatLocalDate(getContext(), currentLocalDate, DateUtils.FORMAT_SHOW_DATE);

			// for getting today's date formatted to local instance
			java.text.DateFormat localDateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL, Locale.getDefault());
			String formattedCurrentLocalDateStr = localDateFormat.format(currentLocalDate.toDate());

			if (startDate != null && date.equals(startDate)) {
				return getContext().getString(R.string.cd_day_selected_start_TEMPLATE, dateStr);
			}
			else if (endDate != null && date.equals(endDate)) {
				return getContext().getString(R.string.cd_day_selected_end_TEMPLATE, dateStr);
			}
			else if (startDate != null && endDate != null && date.isAfter(startDate) && date.isBefore(endDate)) {
				return getContext().getString(R.string.cd_day_selected_TEMPLATE, dateStr);
			}
			else if (!mState.canSelectDate(date)) {
				return getContext().getString(R.string.cd_day_invalid_TEMPLATE, dateStr);
			}
			else if (!date.equals(startDate) && dateStr.equals(currentLocalDateStr)){
				return Phrase.from(getContext(), R.string.cd_day_selected_today_cont_desc_TEMPLATE).put("today", formattedCurrentLocalDateStr).format().toString();
			}
			else {
				return dateStr;
			}
		}

		@Override
		protected int getVirtualViewAt(float x, float y) {
			int[] cell = getCell(x, y);
			if (cell != null) {
				return cellToVirtualViewId(cell[0], cell[1]);
			}

			return INVALID_ID;
		}

		@Override
		protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					virtualViewIds.add(cellToVirtualViewId(row, col));
				}
			}
		}

		@Override
		protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
			int[] cell = virtualViewIdToCell(virtualViewId);
			LocalDate date = mDays[cell[0]][cell[1]];

			node.setContentDescription(getDescriptionForDate(date));

			if (mState.canSelectDate(date)) {
				node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
			}

			node.setBoundsInParent(
				new Rect((int) mCellWidth * cell[1],
					(int) mCellHeight * cell[0],
					(int) mCellWidth * (cell[1] + 1),
					(int) mCellHeight * (cell[0] + 1)));
		}

		@Override
		protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
			event.setContentDescription(getDescriptionForVirtualViewId(virtualViewId));
		}

		@Override
		protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
			switch (action) {
			case AccessibilityNodeInfoCompat.ACTION_CLICK:
				int[] cell = virtualViewIdToCell(virtualViewId);
				onDateClicked(cell[0], cell[1]);
				return true;
			}

			return false;
		}
	}

	// Exposed for drawing tooltips over the calendar

	public Point getStartDayCoordinates() {
		Point p = new Point();
		p.set((int) startCenterX, (int) startCenterY - (int) (mCircleRadius));
		return p;
	}

	public Point getEndDayCoordinates() {
		Point p = new Point();
		p.set((int) endCenterX, (int) endCenterY - (int) (mCircleRadius));
		return p;
	}

	public float getRadius() {
		return mCircleRadius;
	}
}
