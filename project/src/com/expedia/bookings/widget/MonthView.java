package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.CalendarPicker.CalendarState;
import com.mobiata.android.Log;

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

	private static final int ROWS = 6;
	private static final int COLS = 7;

	private GestureDetectorCompat mDetector;

	private MonthTouchHelper mTouchHelper;

	private CalendarState mState;

	// We need to know where we are shifting FROM, so we keep our own copy
	// of the year month we're anchored in.
	private YearMonth mAnchorYearMonth;

	private LocalDate mFirstDayOfGrid;
	private LocalDate[][] mDays = new LocalDate[ROWS][COLS];
	private Interval mDayInterval;

	private TextPaint mTextPaint;
	private TextPaint mTextSecondaryPaint;
	private TextPaint mTextInversePaint;
	private TextPaint mTextTodayPaint;
	private TextPaint mInvalidDayPaint;
	private float mMaxTextSize;

	private Paint mSelectionPaint;
	private Paint mSelectionLinePaint;
	private Paint mSelectionAlphaPaint;

	// Animation variables
	private float mTranslationWeeks;
	//	private float mMonthShiftPercent;
	//	private int mNumWeeksShift;

	// Variables that are cached for faster drawing
	private int mWidth;
	private int mHeight;
	private float mFirstRowCenter;
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

		mTextTodayPaint = new TextPaint(mTextPaint);

		mTextSecondaryPaint = new TextPaint(mTextPaint);

		mInvalidDayPaint = new TextPaint(mTextPaint);

		mSelectionPaint = new Paint();
		mSelectionPaint.setAntiAlias(true);

		mSelectionLinePaint = new Paint(mSelectionPaint);
		mSelectionLinePaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
		mSelectionLinePaint.setStyle(Style.STROKE);

		mSelectionAlphaPaint = new Paint(mSelectionPaint);

		// Accessibility
		mTouchHelper = new MonthTouchHelper(this);
		ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean dispatchHoverEvent(MotionEvent event) {
		// Always attempt to dispatch hover events to accessibility first.
		if (mTouchHelper.dispatchHoverEvent(event)) {
			return true;
		}

		return super.dispatchHoverEvent(event);
	}

	public void setCalendarState(CalendarState state) {
		mState = state;
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setTextSecondaryColor(int color) {
		mTextSecondaryPaint.setColor(color);
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

	public void setTodayColor(int color) {
		mTextTodayPaint.setColor(color);
	}

	public void setInvalidDayColor(int color) {
		mInvalidDayPaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	public void setTranslationWeeks(float translationWeeks) {
		mTranslationWeeks = translationWeeks;
		invalidate();
	}

	public float getTranslationWeeks() {
		return mTranslationWeeks;
	}

	public void notifyDisplayYearMonthChanged() {
		mAnchorYearMonth = mState.getDisplayYearMonth();
		mTranslationWeeks = 0;
		precomputeGrid();
		invalidate();
	}

	public void notifySelectedDatesChanged() {
		// TODO: Only invalidate the cells of the dates that have changed?
		invalidate();
	}

	private void precomputeGrid() {
		mFirstDayOfGrid = mAnchorYearMonth.toLocalDate(1);
		while (mFirstDayOfGrid.getDayOfWeek() != JodaUtils.getFirstDayOfWeek()) {
			mFirstDayOfGrid = mFirstDayOfGrid.minusDays(1);
		}

		for (int week = 0; week < ROWS; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				mDays[week][dayOfWeek] = mFirstDayOfGrid.plusDays(week * COLS + dayOfWeek);
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
			mFirstRowCenter = mRowCenters[0];

			mCircleRadius = Math.min(mCellSelectionHeight, mCellSelectionWidth) / 2;

			// Scale down the text size; I'm not too concerned about it being too wide, so
			// just use the TextPaint's height to determine if we're too large
			float cellMinSize = Math.min((float) mWidth / COLS, (float) mHeight / ROWS) * (1 - PADDING_PERCENT);
			mTextPaint.setTextSize(mMaxTextSize);
			while (cellMinSize < mTextPaint.ascent() - mTextPaint.descent()) {
				mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
			}

			// Make sure all other paints match size
			mTextSecondaryPaint.setTextSize(mTextPaint.getTextSize());
			mTextInversePaint.setTextSize(mTextPaint.getTextSize());
			mTextTodayPaint.setTextSize(mTextPaint.getTextSize());
			mInvalidDayPaint.setTextSize(mTextPaint.getTextSize());
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

		long start = System.nanoTime();

		/*

		LocalDate startDate = mState.getStartDate();
		LocalDate endDate = mState.getEndDate();

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
		int[] startCell = getCell(startDate);
		if (startCell != null) {
			float centerX = mColCenters[startCell[1]];
			float centerY = mRowCenters[startCell[0]];
			canvas.drawCircle(centerX, centerY, mCircleRadius, mSelectionPaint);
		}

		// Draw end date (if selected and visible)
		int[] endCell = getCell(endDate);
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
		if (startDate != null && endDate != null
				&& (mDayInterval.contains(startDate.toDateTimeAtStartOfDay())
				|| mDayInterval.contains(endDate.toDateTimeAtStartOfDay()))) {

			int startRow = startCell != null ? startCell[0] : 0;
			int endRow = endCell != null ? endCell[0] : COLS - 1;
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
					rect.left = startCell[1] * mCellWidth + halfCellWidth;
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
					rect.right = endCell[1] * mCellWidth + halfCellWidth;
					rect.top = mRowCenters[endRow] - mCircleRadius;
					rect.bottom = mRowCenters[endRow] + mCircleRadius;
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
		LocalDate today = LocalDate.now();
		Interval monthInterval = mState.getDisplayYearMonth().toInterval();
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
				else if (date.equals(today)) {
					paint = mTextTodayPaint;
				}
				else if (!mState.canSelectDate(date)) {
					paint = mInvalidDayPaint;
				}
				else if (!monthInterval.contains(date.toDateTimeAtStartOfDay())) {
					paint = mTextSecondaryPaint;
				}
				else {
					paint = mTextPaint;
				}

				canvas.drawText(Integer.toString(date.getDayOfMonth()), centerX,
						centerY + halfTextHeight - mTextPaint.descent(), paint);
			}
		}

		*/

		int weekShiftFloor = (int) Math.floor(mTranslationWeeks);
		float weekShiftRemainder = mTranslationWeeks - weekShiftFloor;

		// Draw from weekShiftFloor --> weekShiftFloor + ROWS
		int numRowsToDraw = mTranslationWeeks == 0 ? ROWS : ROWS + 1;
		float textHeight = mTextPaint.descent() - mTextPaint.ascent();
		float halfTextHeight = textHeight / 2;
		for (int week = 0; week < numRowsToDraw; week++) {
			for (int dayOfWeek = 0; dayOfWeek < COLS; dayOfWeek++) {
				LocalDate date = mFirstDayOfGrid.plusWeeks(week + weekShiftFloor).plusDays(dayOfWeek);

				float centerX = mColCenters[dayOfWeek];
				float centerY = mFirstRowCenter + (mCellHeight * week) - (mCellHeight * weekShiftRemainder);

				canvas.drawText(Integer.toString(date.getDayOfMonth()), centerX,
						centerY + halfTextHeight - mTextPaint.descent(), mTextPaint);
			}
		}

		Log.v("MonthView.onDraw() time: " + ((System.nanoTime() - start) / 1000000));
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = mDetector.onTouchEvent(event);
		if (!consumed) {
			consumed = super.onTouchEvent(event);
		}
		return consumed;
	}

	private boolean onDateClicked(int row, int col) {
		LocalDate clickedDate = mDays[row][col];

		// If the user clicked on an illegal date, don't even try to use it
		if (!mState.canSelectDate(clickedDate)) {
			return false;
		}

		LocalDate startDate = mState.getStartDate();
		LocalDate endDate = mState.getEndDate();
		if (startDate == null) {
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

	//////////////////////////////////////////////////////////////////////////
	// Accessibility

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
				virtualViewId % COLS
			};
		}

		private String getDescriptionForVirtualViewId(int virtualViewId) {
			int[] cell = virtualViewIdToCell(virtualViewId);
			LocalDate date = mDays[cell[0]][cell[1]];
			return getDescriptionForDate(date);
		}

		private String getDescriptionForDate(LocalDate date) {
			String dateStr = JodaUtils.formatLocalDate(getContext(), date, DateUtils.FORMAT_SHOW_DATE);

			LocalDate startDate = mState.getStartDate();
			LocalDate endDate = mState.getEndDate();
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
}
