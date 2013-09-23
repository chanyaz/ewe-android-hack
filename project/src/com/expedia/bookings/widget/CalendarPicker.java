package com.expedia.bookings.widget;

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
import com.mobiata.android.util.Ui;

/**
 * All configuration happens in the style/attributes.  If you want to setup the code
 * to work with dynamic configuration be my guest.
 * 
 * Notes:
 * - CalendarPicker does not work with layout_width="wrap_content" because it has no
 *   minimum width.  Either use "match_parent" or specify a pixel width.
 * 
 * TODO: Scale all views based on size of CalendarPicker itself
 */
public class CalendarPicker extends LinearLayout {

	// State
	private YearMonth mDisplayYearMonth;
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	// Styles - loaded at start, not modifiable
	private int mBaseColor;
	private int mHighlightColor;
	private int mHighlightInverseColor;
	private int mTodayColor;

	// Subviews
	private TextView mPreviousMonthTextView;
	private TextView mCurrentMonthTextView;
	private TextView mNextMonthTextView;
	private DaysOfWeekView mDaysOfWeekView;
	private MonthView mMonthView;

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

		// Default to showing current year/month
		mDisplayYearMonth = YearMonth.now();
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

		mMonthView.setDateSelectionListener(mMonthListener);
		mMonthView.setTextColor(mBaseColor);
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

		mDisplayYearMonth = ss.displayMonthYear;
		mStartDate = ss.startDate;
		mEndDate = ss.endDate;

		mMonthView.setDisplayYearMonth(mDisplayYearMonth);
		mMonthView.setDateSelection(mStartDate, mEndDate);
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

				CaretDrawable drawableLeft = new CaretDrawable(Direction.LEFT, mHighlightColor);
				drawableLeft.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mPreviousMonthTextView.setCompoundDrawables(drawableLeft, null, null, null);
				mPreviousMonthTextView.setCompoundDrawablePadding(caretPadding);

				CaretDrawable drawableRight = new CaretDrawable(Direction.RIGHT, mHighlightColor);
				drawableRight.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mNextMonthTextView.setCompoundDrawables(null, null, drawableRight, null);
				mNextMonthTextView.setCompoundDrawablePadding(caretPadding);

				// We changed some bounds, need to redraw
				return false;
			}
		});
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.displayMonthYear = mDisplayYearMonth;
		ss.startDate = mStartDate;
		ss.endDate = mEndDate;

		return ss;
	}

	//////////////////////////////////////////////////////////////////////////
	// Outside control

	public void setDateChangedListener(DateSelectionChangedListener listener) {
		mListener = listener;
	}

	public LocalDate getStartDate() {
		return mStartDate;
	}

	public LocalDate getEndDate() {
		return mEndDate;
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	private void setDisplayYearMonth(YearMonth yearMonth) {
		mDisplayYearMonth = yearMonth;

		syncViewsWithState();
	}

	private void syncViewsWithState() {
		// Update header
		mPreviousMonthTextView.setText(mDisplayYearMonth.minusMonths(1).monthOfYear().getAsText());
		mCurrentMonthTextView.setText(mDisplayYearMonth.monthOfYear().getAsText());
		mNextMonthTextView.setText(mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText());

		// Update month view
		mMonthView.setDisplayYearMonth(mDisplayYearMonth);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == mPreviousMonthTextView) {
				setDisplayYearMonth(mDisplayYearMonth.minusMonths(1));
			}
			else if (v == mNextMonthTextView) {
				setDisplayYearMonth(mDisplayYearMonth.plusMonths(1));
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DateSelectionChangedListener {
		public void onDateSelectionChanged(LocalDate start, LocalDate end);
	}

	//////////////////////////////////////////////////////////////////////////
	// DateSelectionChangedListener

	private DateSelectionChangedListener mMonthListener = new DateSelectionChangedListener() {
		@Override
		public void onDateSelectionChanged(LocalDate start, LocalDate end) {
			mStartDate = start;
			mEndDate = end;

			if (mListener != null) {
				mListener.onDateSelectionChanged(start, end);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Saved State

	private static class SavedState extends BaseSavedState {
		YearMonth displayMonthYear;
		LocalDate startDate;
		LocalDate endDate;

		private SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeString(displayMonthYear.toString());
			JodaUtils.writeLocalDate(out, startDate);
			JodaUtils.writeLocalDate(out, endDate);
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
		}
	}
}
