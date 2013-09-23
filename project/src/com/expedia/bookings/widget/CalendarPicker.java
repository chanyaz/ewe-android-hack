package com.expedia.bookings.widget;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.graphics.CaretDrawable;
import com.expedia.bookings.graphics.CaretDrawable.Direction;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * A calendar date picker.
 * 
 * A quick guide to usage:
 * - Use setDateChangedListener() to listen to date changes.
 * - Use setSelectedDates() to change which dates are currently selected
 * - Use setSelectableDateRange() to select the minimum/maximum selectable dates.
 * - Use a style derived from "V2.Widget.CalendarPicker" to theme it. 
 * 
 * Notes:
 * - CalendarPicker does not work with layout_width="wrap_content" because it has no
 *   minimum width.  Either use "match_parent" or specify a pixel width.
 * 
 * TODO: Scale all views based on size of CalendarPicker itself
 * TODO: Make CalendarState dirty detection smarter
 */
public class CalendarPicker extends LinearLayout {

	// State
	private CalendarState mState = new CalendarState();

	// Styles - loaded at start, not modifiable
	private int mBaseColor;
	private int mSecondaryColor;
	private int mHighlightColor;
	private int mHighlightInverseColor;
	private int mTodayColor;

	// Subviews
	private TextView mPreviousMonthTextView;
	private TextView mCurrentMonthTextView;
	private TextView mNextMonthTextView;
	private DaysOfWeekView mDaysOfWeekView;
	private MonthView mMonthView;

	// Drawables
	private CaretDrawable mPreviousMonthCaret;
	private CaretDrawable mNextMonthCaret;

	// Listener
	private DateSelectionChangedListener mListener;

	public CalendarPicker(Context context) {
		this(context, null);
	}

	public CalendarPicker(Context context, AttributeSet attrs) {
		this(context, attrs, R.style.V2_Widget_CalendarPicker);
	}

	public CalendarPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Load attributes
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalendarPicker, 0, defStyle);
		int n = ta.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);

			switch (attr) {
			case R.styleable.CalendarPicker_calendarBaseColor:
				mBaseColor = ta.getColor(attr, mBaseColor);
				break;

			case R.styleable.CalendarPicker_calendarSecondaryColor:
				mSecondaryColor = ta.getColor(attr, mSecondaryColor);
				break;

			case R.styleable.CalendarPicker_calendarHighlightColor:
				mHighlightColor = ta.getColor(attr, mHighlightColor);
				break;

			case R.styleable.CalendarPicker_calendarHighlightInverseColor:
				mHighlightInverseColor = ta.getColor(attr, mHighlightInverseColor);
				break;

			case R.styleable.CalendarPicker_calendarTodayColor:
				mTodayColor = ta.getColor(attr, mTodayColor);
				break;
			}
		}
		ta.recycle();

		// Configure some layout params (so you don't have to do it in XML)
		setOrientation(LinearLayout.VERTICAL);

		// Inflate the widget
		inflate(context, R.layout.widget_calendar_picker, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Retrieve Views
		mPreviousMonthTextView = Ui.findView(this, R.id.previous_month);
		mCurrentMonthTextView = Ui.findView(this, R.id.current_month);
		mNextMonthTextView = Ui.findView(this, R.id.next_month);
		mDaysOfWeekView = Ui.findView(this, R.id.days_of_week);
		mMonthView = Ui.findView(this, R.id.month);

		// Configure Views
		mPreviousMonthTextView.setTextColor(mHighlightColor);
		mCurrentMonthTextView.setTextColor(mBaseColor);
		mNextMonthTextView.setTextColor(mHighlightColor);

		mPreviousMonthTextView.setOnClickListener(mOnClickListener);
		mNextMonthTextView.setOnClickListener(mOnClickListener);

		// TODO: Come up with better max text size (based on size of entire thing?)
		mDaysOfWeekView.setTextColor(mBaseColor);
		mDaysOfWeekView.setMaxTextSize(mPreviousMonthTextView.getTextSize());

		mMonthView.setCalendarState(mState);
		mMonthView.setTextColor(mBaseColor);
		mMonthView.setTextSecondaryColor(mSecondaryColor);
		mMonthView.setHighlightColor(mHighlightColor);
		mMonthView.setHighlightInverseColor(mHighlightInverseColor);
		mMonthView.setTodayColor(mTodayColor);
		mMonthView.setMaxTextSize(mPreviousMonthTextView.getTextSize());
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		// Restore without calling setters, to avoid notifications firing
		mState.mDisplayYearMonth = ss.displayMonthYear;
		mState.mStartDate = ss.startDate;
		mState.mEndDate = ss.endDate;
		mState.mMinSelectableDate = ss.minSelectableDate;
		mState.mMaxSelectableDate = ss.maxSelectableDate;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Wait until here to start manipulating sub-Views; that way we can
		// restore the instance state properly first.
		syncViewsWithState();

		// Measure some Views so we can properly setup Drawables next to them
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);

				// Carets' width is calculated based on the height
				int caretHeight = (int) mPreviousMonthTextView.getTextSize();
				int caretWidth = (int) Math.floor(caretHeight / 1.5);
				int caretPadding = caretHeight / 3;
				int totalHeight = mPreviousMonthTextView.getHeight();
				int topBotPadding = (totalHeight - caretHeight) / 2;

				mPreviousMonthCaret = new CaretDrawable(Direction.LEFT, mHighlightColor);
				mPreviousMonthCaret.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mPreviousMonthTextView.setCompoundDrawables(mPreviousMonthCaret, null, null, null);
				mPreviousMonthTextView.setCompoundDrawablePadding(caretPadding);

				mNextMonthCaret = new CaretDrawable(Direction.RIGHT, mHighlightColor);
				mNextMonthCaret.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mNextMonthTextView.setCompoundDrawables(null, null, mNextMonthCaret, null);
				mNextMonthTextView.setCompoundDrawablePadding(caretPadding);

				syncDisplayMonthCarets();

				// We changed some bounds, need to redraw
				return false;
			}
		});
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.displayMonthYear = mState.mDisplayYearMonth;
		ss.startDate = mState.mStartDate;
		ss.endDate = mState.mEndDate;
		ss.minSelectableDate = mState.mMinSelectableDate;
		ss.maxSelectableDate = mState.mMaxSelectableDate;

		return ss;
	}

	//////////////////////////////////////////////////////////////////////////
	// Outside control

	public void setDateChangedListener(DateSelectionChangedListener listener) {
		mListener = listener;
	}

	public void setSelectedDates(LocalDate startDate, LocalDate endDate) {
		mState.setSelectedDates(startDate, endDate);
	}

	/**
	 * Defines the selectable date range.
	 *  
	 * @param minDate the minimum selectable date or null for no minimum
	 * @param maxDate the maximum selectable date or null for no maximum
	 */
	public void setSelectableDateRange(LocalDate minDate, LocalDate maxDate) {
		mState.setSelectableDateRange(minDate, maxDate);
	}

	public LocalDate getStartDate() {
		return mState.mStartDate;
	}

	public LocalDate getEndDate() {
		return mState.mEndDate;
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	// While this is easier, try to avoid calling it unless you really need to
	// sync *all* Views at once
	private void syncViewsWithState() {
		syncDisplayMonthViews();
		syncDateSelectionViews();
	}

	private void syncDisplayMonthViews() {
		// Update month view
		mMonthView.notifyDisplayYearMonthChanged();

		// Update header
		mPreviousMonthTextView.setText(mState.mDisplayYearMonth.minusMonths(1).monthOfYear().getAsText());
		mCurrentMonthTextView.setText(mState.mDisplayYearMonth.monthOfYear().getAsText());
		mNextMonthTextView.setText(mState.mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText());

		syncDisplayMonthCarets();
	}

	private void syncDisplayMonthCarets() {
		// Show carets based on min/max selectable dates
		if (mPreviousMonthCaret != null && mNextMonthCaret != null) {
			mPreviousMonthCaret.setAlpha(mState.canDisplayYearMonth(mState.mDisplayYearMonth.minusMonths(1)) ? 255 : 0);
			mNextMonthCaret.setAlpha(mState.canDisplayYearMonth(mState.mDisplayYearMonth.plusMonths(1)) ? 255 : 0);
		}
	}

	private void syncDateSelectionViews() {
		mMonthView.notifySelectedDatesChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			YearMonth yearMonth = null;
			if (v == mPreviousMonthTextView) {
				yearMonth = mState.mDisplayYearMonth.minusMonths(1);
			}
			else if (v == mNextMonthTextView) {
				yearMonth = mState.mDisplayYearMonth.plusMonths(1);
			}

			if (yearMonth != null && mState.canDisplayYearMonth(yearMonth)) {
				mState.setDisplayYearMonth(yearMonth);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DateSelectionChangedListener {
		public void onDateSelectionChanged(LocalDate start, LocalDate end);
	}

	//////////////////////////////////////////////////////////////////////////
	// CalendarPicker State class
	//
	// We keep one set of settings; this should be shared between all classes
	// that need it (e.g. the MonthView).
	//
	// It is in charge of notifying all related Views whenever something
	// important changes.

	protected final class CalendarState {

		private YearMonth mDisplayYearMonth;

		private LocalDate mStartDate;
		private LocalDate mEndDate;

		private LocalDate mMinSelectableDate;
		private LocalDate mMaxSelectableDate;

		// We keep track of what has changed since last notification; make
		// sure to call syncChangedFields() after changing any set of fields
		//
		// We keep track of it this way so that we can ensure a valid set of
		// fields but only have to update Views if necessary
		private boolean mDisplayYearMonthDirty;
		private boolean mDatesDirty;

		public CalendarState() {
			// Default to displaying current year month
			mDisplayYearMonth = YearMonth.now();
		}

		public void setDisplayYearMonth(YearMonth yearMonth) {
			mDisplayYearMonth = yearMonth;
			mDisplayYearMonthDirty = true;
			validateAndSyncState();
		}

		public YearMonth getDisplayYearMonth() {
			return mDisplayYearMonth;
		}

		public void setSelectedDates(LocalDate startDate, LocalDate endDate) {
			if (startDate == null && endDate != null) {
				throw new IllegalArgumentException("Can't set an end date without a start date!  end=" + endDate);
			}
			else if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
				throw new IllegalArgumentException("Can't set an end date BEFORE a start date!  start=" + startDate
						+ " end=" + endDate);
			}

			if (!JodaUtils.isEqual(startDate, mStartDate) || !JodaUtils.isEqual(endDate, mEndDate)) {
				mStartDate = startDate;
				mEndDate = endDate;
				mDatesDirty = true;
				validateAndSyncState();
			}
		}

		public LocalDate getStartDate() {
			return mStartDate;
		}

		public LocalDate getEndDate() {
			return mEndDate;
		}

		public void setSelectableDateRange(LocalDate minDate, LocalDate maxDate) {
			if (minDate != null && maxDate != null && JodaUtils.daysBetween(minDate, maxDate) <= 1) {
				throw new IllegalArgumentException("Selectable date range must be > 1 day; got " + minDate + " to "
						+ maxDate);
			}

			if (!JodaUtils.isEqual(minDate, mMinSelectableDate) || !JodaUtils.isEqual(maxDate, mMaxSelectableDate)) {
				mMinSelectableDate = minDate;
				mMaxSelectableDate = maxDate;
				mDisplayYearMonthDirty = true;
				validateAndSyncState();
			}
		}

		/**
		 * @return Whether the YearMonth is within the selectable range (and is therefore viewable)
		 */
		public boolean canDisplayYearMonth(YearMonth yearMonth) {
			Interval interval = yearMonth.toInterval();
			return (mMinSelectableDate == null || !interval.isBefore(mMinSelectableDate.toDateTimeAtStartOfDay()))
					&& (mMaxSelectableDate == null || !interval.isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay()));
		}

		private void validateAndSyncState() {
			// Ensure nothing is set before the min selectable date
			if (mMinSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isBefore(mMinSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting year month to match min date");
					mDisplayYearMonth = new YearMonth(mMinSelectableDate.getYear(), mMinSelectableDate.getMonthOfYear());
					mDisplayYearMonthDirty = true;
				}

				if (mStartDate != null && mStartDate.isBefore(mMinSelectableDate)) {
					Log.w("Start date (" + mStartDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting start date to min date");
					mStartDate = mMinSelectableDate;
					mDatesDirty = true;
				}

				if (mEndDate != null && mEndDate.isBefore(mMinSelectableDate)) {
					Log.w("End date (" + mEndDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting end date to one day after start date (" + mStartDate.plusDays(1) + ")");
					mEndDate = mStartDate.plusDays(1);
					mDatesDirty = true;
				}
			}

			// Ensure nothing is set after the max selectable date
			if (mMaxSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting year month to match max date");
					mDisplayYearMonth = new YearMonth(mMaxSelectableDate.getYear(), mMaxSelectableDate.getMonthOfYear());
					mDisplayYearMonthDirty = true;
				}

				if (mEndDate != null && mEndDate.isAfter(mMaxSelectableDate)) {
					Log.w("End date (" + mEndDate
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting end date to max date");
					mEndDate = mMaxSelectableDate;
					mDatesDirty = true;
				}

				if (mStartDate != null && mStartDate.isAfter(mMaxSelectableDate)) {
					if (mEndDate != null) {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to one day before end date (" + mEndDate.minusDays(1) + ")");
						mStartDate = mEndDate.minusDays(1);
						mDatesDirty = true;
					}
					else {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to max date (" + mMaxSelectableDate + ") (no end date)");
						mStartDate = mMaxSelectableDate;
						mDatesDirty = true;
					}
				}
			}

			// Now that we're internally consistent, sync whatever fields may have changed (either from
			// the validation process or explicit changes before it)
			if (mDisplayYearMonthDirty) {
				syncDisplayMonthViews();
				mDisplayYearMonthDirty = false;
			}

			if (mDatesDirty) {
				syncDateSelectionViews();
				mDatesDirty = false;

				// TODO: Should we always notify, or only when it was changed by user interaction?
				if (mListener != null) {
					mListener.onDateSelectionChanged(mStartDate, mEndDate);
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saved State

	private static class SavedState extends BaseSavedState {
		YearMonth displayMonthYear;
		LocalDate startDate;
		LocalDate endDate;
		LocalDate minSelectableDate;
		LocalDate maxSelectableDate;

		private SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeString(displayMonthYear.toString());
			JodaUtils.writeLocalDate(out, startDate);
			JodaUtils.writeLocalDate(out, endDate);
			JodaUtils.writeLocalDate(out, minSelectableDate);
			JodaUtils.writeLocalDate(out, maxSelectableDate);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		private SavedState(Parcel in) {
			super(in);

			displayMonthYear = YearMonth.parse(in.readString());
			startDate = JodaUtils.readLocalDate(in);
			endDate = JodaUtils.readLocalDate(in);
			minSelectableDate = JodaUtils.readLocalDate(in);
			maxSelectableDate = JodaUtils.readLocalDate(in);
		}
	}
}
