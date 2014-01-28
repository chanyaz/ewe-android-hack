package com.expedia.bookings.widget;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.joda.time.YearMonth;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
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
 * - Use setSelectedDates() to change which dates are currently selected.
 * - Use setSelectableDateRange() to select the minimum/maximum selectable dates.
 * - Use setMaxSelectableDateRange() to select the longest duration one can select.
 * - Use a style derived from "V2.Widget.CalendarPicker" to theme it.
 * 
 * Notes:
 * - CalendarPicker does not work with layout_width="wrap_content" because it has no
 *   minimum width.  Either use "match_parent" or specify a pixel width.
 * 
 * TODO: Scale all views based on size of CalendarPicker itself
 */
@TargetApi(11)
public class CalendarPicker extends LinearLayout {

	// Constants
	// The base value to scale by log4(x)
	private static final int DURATION_WEEK_MULTIPLIER = 300;
	// We use log base 4 because we typically scale by 4 weeks at a time (only sometimes 5)
	private static final double DURATION_WEEK_LOG_BASE = Math.log(4);

	// State
	private CalendarState mState = new CalendarState();

	// Styles - loaded at start, not modifiable
	private int mBaseColor;
	private int mSecondaryColor;
	private int mHighlightColor;
	private int mHighlightInverseColor;
	private int mTodayColor;
	private int mInvalidColor;

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

	// Animation
	private boolean mAttachedToWindow;
	private float mTranslationWeekTarget = 0;
	private Animator mMonthAnimator;

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

			case R.styleable.CalendarPicker_calendarInvalidDaysColor:
				mInvalidColor = ta.getColor(attr, mInvalidColor);
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
		mMonthView.setInvalidDayColor(mInvalidColor);
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
		mState.mMaxSelectableDateRange = ss.maxSelectableDateRange;
		mState.updateLastState();
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

		mAttachedToWindow = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mAttachedToWindow = false;
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
		ss.maxSelectableDateRange = mState.mMaxSelectableDateRange;

		return ss;
	}

	//////////////////////////////////////////////////////////////////////////
	// Outside control

	public void setDateChangedListener(DateSelectionChangedListener listener) {
		mListener = listener;
	}

	public void setSelectedDates(LocalDate startDate, LocalDate endDate) {
		mState.setDisplayYearMonth(new YearMonth(startDate));
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

	public void setMaxSelectableDateRange(int numDays) {
		mState.setMaxSelectableDateRange(numDays);
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
		// Animate the month view changing if we're attached (that means we're past the setup phase)
		if (mAttachedToWindow) {
			animateMonth(mState.mLastState.mDisplayYearMonth, mState.mDisplayYearMonth);
		}
		else {
			mMonthView.notifyDisplayYearMonthChanged();
		}

		// Update header
		Context context = getContext();

		String prevMonth = mState.mDisplayYearMonth.minusMonths(1).monthOfYear().getAsText();
		mPreviousMonthTextView.setText(prevMonth);
		mPreviousMonthTextView.setContentDescription(context.getString(R.string.cd_month_previous_TEMPLATE, prevMonth));

		String currMonth = mState.mDisplayYearMonth.monthOfYear().getAsText();
		mCurrentMonthTextView.setText(mState.mDisplayYearMonth.monthOfYear().getAsText());
		mCurrentMonthTextView.setContentDescription(context.getString(R.string.cd_month_current_TEMPLATE, currMonth));

		String nextMonth = mState.mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText();
		mNextMonthTextView.setText(mState.mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText());
		mNextMonthTextView.setContentDescription(context.getString(R.string.cd_month_next_TEMPLATE, nextMonth));

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
	// Month animation

	/**
	 * This animates the MonthView from one month to another.
	 * 
	 * It gets a bit more complex if the MonthView is already animating.  In that case,
	 * it stops the current animation and begins a new one *from wherever the MonthView
	 * is* to the next month.  The duration is scaled based on how far it now has to travel.
	 */
	private void animateMonth(YearMonth fromMonth, YearMonth toMonth) {
		if (mMonthAnimator != null && mMonthAnimator.isRunning()) {
			mMonthAnimator.cancel();
		}

		// We need to calculate *how many weeks* are translated going fromMonth --> toMonth
		// This has been determined to be the (number of days / 7) (+ 1 IF days of week swap)
		LocalDate fromMonthFirstDay = fromMonth.toLocalDate(1);
		LocalDate toMonthFirstDay = toMonth.toLocalDate(1);
		int fromDayOfWeek = JodaUtils.getDayOfWeekNormalized(fromMonthFirstDay);
		int toDayOfWeek = JodaUtils.getDayOfWeekNormalized(toMonthFirstDay);
		int translationWeeks = Weeks.weeksBetween(fromMonthFirstDay, toMonthFirstDay).getWeeks();
		if (translationWeeks < 0 && fromDayOfWeek < toDayOfWeek) {
			translationWeeks--;
		}
		else if (translationWeeks > 0 && fromDayOfWeek > toDayOfWeek) {
			translationWeeks++;
		}

		mTranslationWeekTarget += translationWeeks;

		float currentShift = mMonthView.getTranslationWeeks();

		mMonthAnimator = ObjectAnimator.ofFloat(mMonthView, "translationWeeks", mTranslationWeekTarget);
		mMonthAnimator.addListener(mMonthAnimatorListener);

		// We use a logarithmic scale so that the further you go, the less duration we add to the total
		double durationBase = Math.log(Math.abs(mTranslationWeekTarget - currentShift) + 1) / DURATION_WEEK_LOG_BASE;
		int duration = (int) Math.round(DURATION_WEEK_MULTIPLIER * durationBase);
		mMonthAnimator.setDuration(duration);

		mMonthAnimator.start();
	}

	// Once the animation is done we want to re-center the MonthView, but only if
	// has actually finished (and the animation wasn't just cancelled midway through)
	private AnimatorListener mMonthAnimatorListener = new AnimatorListenerAdapter() {

		private boolean mActuallyEnding;

		@Override
		public void onAnimationStart(Animator animation) {
			mActuallyEnding = true;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mActuallyEnding) {
				mMonthView.notifyDisplayYearMonthChanged();
				mTranslationWeekTarget = 0;
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mActuallyEnding = false;
		}
	};

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

		private int mMaxSelectableDateRange;

		// We keep track of what has changed so that we don't do unnecessary View updates
		private CalendarState mLastState;

		public CalendarState() {
			// Default to displaying current year month
			mDisplayYearMonth = YearMonth.now();
		}

		public void setDisplayYearMonth(YearMonth yearMonth) {
			mDisplayYearMonth = yearMonth;
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
				validateAndSyncState();
			}
		}

		/**
		 * @return true if the date is selectable (within min/max date ranges)
		 */
		public boolean canSelectDate(LocalDate date) {
			return (mMinSelectableDate == null || !date.isBefore(mMinSelectableDate))
					&& (mMaxSelectableDate == null || !date.isAfter(mMaxSelectableDate));
		}

		/**
		 * @return Whether the YearMonth is within the selectable range (and is therefore viewable)
		 */
		public boolean canDisplayYearMonth(YearMonth yearMonth) {
			Interval interval = yearMonth.toInterval();
			return (mMinSelectableDate == null || !interval.isBefore(mMinSelectableDate.toDateTimeAtStartOfDay()))
					&& (mMaxSelectableDate == null || !interval.isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay()));
		}

		public void setMaxSelectableDateRange(int maxRange) {
			if (maxRange != mMaxSelectableDateRange) {
				mMaxSelectableDateRange = maxRange;
				validateAndSyncState();
			}
		}

		private void validateAndSyncState() {
			// Ensure nothing is set before the min selectable date
			if (mMinSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isBefore(mMinSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting year month to match min date");
					mDisplayYearMonth = new YearMonth(mMinSelectableDate.getYear(), mMinSelectableDate.getMonthOfYear());
				}

				if (mStartDate != null && mStartDate.isBefore(mMinSelectableDate)) {
					Log.v("Start date (" + mStartDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting start date to min date");
					mStartDate = mMinSelectableDate;
				}

				if (mEndDate != null && mEndDate.isBefore(mMinSelectableDate)) {
					Log.w("End date (" + mEndDate
							+ ") is BEFORE min selectable date (" + mMinSelectableDate
							+ "); setting end date to one day after start date (" + mStartDate.plusDays(1) + ")");
					mEndDate = mStartDate.plusDays(1);
				}
			}

			// Ensure nothing is set after the max selectable date
			if (mMaxSelectableDate != null) {
				if (mDisplayYearMonth.toInterval().isAfter(mMaxSelectableDate.toDateTimeAtStartOfDay())) {
					Log.w("Display year month (" + mDisplayYearMonth
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting year month to match max date");
					mDisplayYearMonth = new YearMonth(mMaxSelectableDate.getYear(), mMaxSelectableDate.getMonthOfYear());
				}

				if (mEndDate != null && mEndDate.isAfter(mMaxSelectableDate)) {
					Log.v("End date (" + mEndDate
							+ ") is AFTER max selectable date (" + mMaxSelectableDate
							+ "); setting end date to max date");
					mEndDate = mMaxSelectableDate;
				}

				if (mStartDate != null && mStartDate.isAfter(mMaxSelectableDate)) {
					if (mEndDate != null) {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to one day before end date (" + mEndDate.minusDays(1) + ")");
						mStartDate = mEndDate.minusDays(1);
					}
					else {
						Log.w("Start date (" + mStartDate
								+ ") is AFTER max selectable date (" + mMaxSelectableDate
								+ "); setting start date to max date (" + mMaxSelectableDate + ") (no end date)");
						mStartDate = mMaxSelectableDate;
					}
				}
			}

			// Ensure our date range falls within the maximum
			if (mStartDate != null && mEndDate != null
					&& JodaUtils.daysBetween(mStartDate, mEndDate) > mMaxSelectableDateRange) {
				// We need to determine whether to move the START or the END to match the selectable range
				// This is trickier than it sounds; we need to match the expectations of the user.
				if (mLastState.mEndDate == null) {
					// If END added, shift that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting end date to match range (reason: end was added)");
					mEndDate = mStartDate.plusDays(mMaxSelectableDateRange);
				}
				else if (JodaUtils.isEqual(mLastState.mStartDate, mEndDate)) {
					// If END == last.START, then we've reversed the dates; apply changes to START
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting start date to match range (reason: start/end swapped)");
					mStartDate = mEndDate.minusDays(mMaxSelectableDateRange);
				}
				else if (!JodaUtils.isEqual(mLastState.mEndDate, mEndDate)) {
					// If END has changed, then move that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting end date to match range (reason: end was changed)");
					mEndDate = mStartDate.plusDays(mMaxSelectableDateRange);
				}
				else {
					// If START has changed, then move that
					Log.v("Date range out of max (" + mMaxSelectableDateRange
							+ "); shifting start date to match range (reason: start was changed)");
					mStartDate = mEndDate.minusDays(mMaxSelectableDateRange);
				}
			}

			// Now that we're internally consistent, sync whatever fields may have changed
			if (mLastState == null) {
				mLastState = new CalendarState();
			}

			if (!mLastState.mDisplayYearMonth.equals(mDisplayYearMonth)
					|| !JodaUtils.isEqual(mLastState.mMinSelectableDate, mMinSelectableDate)
					|| !JodaUtils.isEqual(mLastState.mMaxSelectableDate, mMaxSelectableDate)) {
				syncDisplayMonthViews();
			}

			if (!JodaUtils.isEqual(mLastState.mStartDate, mStartDate)
					|| !JodaUtils.isEqual(mLastState.mEndDate, mEndDate)) {
				syncDateSelectionViews();

				// TODO: Should we always notify, or only when it was changed by user interaction?
				if (mListener != null) {
					mListener.onDateSelectionChanged(mStartDate, mEndDate);
				}
			}

			updateLastState();
		}

		protected void updateLastState() {
			if (mLastState == null) {
				mLastState = new CalendarState();
			}

			mLastState.mDisplayYearMonth = mDisplayYearMonth;
			mLastState.mStartDate = mStartDate;
			mLastState.mEndDate = mEndDate;
			mLastState.mMinSelectableDate = mMinSelectableDate;
			mLastState.mMaxSelectableDate = mMaxSelectableDate;
			mLastState.mMaxSelectableDateRange = mMaxSelectableDateRange;
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
		int maxSelectableDateRange;

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
			out.writeInt(maxSelectableDateRange);
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
			maxSelectableDateRange = in.readInt();
		}
	}
}
